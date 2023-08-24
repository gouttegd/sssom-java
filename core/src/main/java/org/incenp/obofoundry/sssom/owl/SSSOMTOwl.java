/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.owl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.MappingFormatter;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplicationBase;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.incenp.obofoundry.sssom.transform.VariableManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * A specialised application of the SSSOM/Transform language to read mapping
 * processing rules that produce OWL axioms for injection into an ontology.
 * <p>
 * This application recognises the following actions:
 * <ul>
 * <li>{@code stop()} to stop any further processing for the current mapping;
 * <li>{@code invert()} to invert the current mapping;
 * <li>{@code direct()} to generate axioms based on the serialisation rules
 * defined by the SSSOM standard;
 * <li>{@code annotate_subject(property, value)} to generate an annotation
 * assertion axiom on the subject;
 * <li>{@code annotate_object(property, value)} to do the same, but on the
 * object;
 * <li>{@code create_axiom(expression)} to generate an arbitrary axiom from an
 * expression in Manchester syntax.
 * </ul>
 * <p>
 * The {@code value} and {@code expression} arguments may contain placeholders
 * representing the value of some fields from the mapping. The following
 * placeholders are recognised:
 * <ul>
 * <li>{@code %subject_id} will be replaced with the subject’s IRI;
 * <li>{@code %subject_curie} will be replaced by the shortened version of the
 * subject’s IRI;
 * <li>{@code %subject_label} will be replaced by the subject’s label;
 * <li>similar placeholders for the object side ({@code %object_id}, etc.).
 * </ul>
 */
public class SSSOMTOwl extends SSSOMTransformApplicationBase<OWLAxiom> {

    /*
     * Common expression patterns that are handled through specialised generators.
     * 
     * Equivalence: %subject_id EquivalentTo %object_id and (REL:1234 some FIL:1234)
     * 
     * Subclass: %subject_id SubclassOf %object_id
     */
    private static final Pattern equivPattern = Pattern.compile(
            "%(subject|object)_id EquivalentTo: %(subject|object)_id( and \\((<[^>]+>) some (<[^>]+>)\\))?");
    private static final Pattern subclassPattern = Pattern
            .compile("%(subject|object)_id SubClassOf: %(subject|object)_id");

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasonerFactory reasonerFactory;
    private OWLReasoner reasoner;
    private ManchesterOWLSyntaxParser manParser;
    private MappingFormatter formatter;
    CustomEntityChecker entityChecker;

    private VariableManager varManager;

    /**
     * Creates a new instance.
     * 
     * @param ontology A helper ontology. This does not need to be the ontology the
     *                 axioms will be injected into afterwards, but it must contain
     *                 declaration axioms for all the object and annotation
     *                 properties that may be used in {@code create_axiom}
     *                 instructions.
     */
    public SSSOMTOwl(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {

        this.ontology = ontology;
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.reasonerFactory = reasonerFactory;
        entityChecker = new CustomEntityChecker(ontology);

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        manParser = new ManchesterOWLSyntaxParserImpl(() -> config, factory);
        manParser.setOWLEntityChecker(entityChecker);

        formatter = new MappingFormatter();
        formatter.addSubstitution("%subject_label", (mapping) -> getSubjectLabel(mapping));
        formatter.addSubstitution("%object_label", (mapping) -> getObjectLabel(mapping));
        formatter.addSubstitution("%subject_id", (mapping) -> String.format("<%s>", mapping.getSubjectId()));
        formatter.addSubstitution("%object_id", (mapping) -> String.format("<%s>", mapping.getObjectId()));

        varManager = new VariableManager();
    }

    @Override
    public void onInit(PrefixManager pm) {
        formatter.addSubstitution("%subject_curie", (mapping) -> pm.shortenIdentifier(mapping.getSubjectId()));
        formatter.addSubstitution("%object_curie", (mapping) -> pm.shortenIdentifier(mapping.getObjectId()));

    }

    @Override
    public void onHeaderAction(String name, List<String> arguments) throws SSSOMTransformError {
        switch ( name ) {
        case "declare_class":
            arguments.forEach((c) -> entityChecker.classNames.add(c));
            return;

        case "declare_object_property":
            arguments.forEach((c) -> entityChecker.objectPropertyNames.add(c));
            return;

        case "set_var":
            checkArguments(name, 2, arguments);
            String varName = arguments.get(0);
            varManager.addVariable(varName, arguments.get(1));
            formatter.addSubstitution("%" + varName, (mapping) -> varManager.expandVariable(varName, mapping));
            return;

        case "set_var_if_subject_subclass_of":
            checkArguments(name, 3, arguments);
            try {
                varManager.setVariableValueForSubjects(arguments.get(0), arguments.get(1),
                        getSubclassesOf(arguments.get(2)));
            } catch ( IllegalArgumentException iae ) {
                throw new SSSOMTransformError(iae.getMessage());
            }
            return;

        case "set_var_if_object_subclass_of":
            checkArguments(name, 3, arguments);
            try {
                varManager.setVariableValueForObjects(arguments.get(0), arguments.get(1),
                        getSubclassesOf(arguments.get(2)));
            } catch ( IllegalArgumentException iae ) {
                throw new SSSOMTransformError(iae.getMessage());
            }
            return;
        }

        super.onHeaderAction(name, arguments);
    }

    @Override
    public IMappingTransformer<OWLAxiom> onGeneratingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        IMappingTransformer<OWLAxiom> transformer = null;

        switch ( name ) {
        case "direct":
            checkArguments(name, 0, arguments);
            transformer = new DirectAxiomGenerator(ontology);
            break;

        case "annotate_subject":
        case "annotate_object":
            checkArguments(name, 2, arguments);
            transformer = new AnnotationAxiomGenerator(ontology, IRI.create(arguments.get(0)),
                    formatter.getTransformer(arguments.get(1)), name.endsWith("_object"));
            break;

        case "create_axiom":
            checkArguments(name, 1, arguments);
            String text = arguments.get(0);

            // First look if the expression is a "common" one, to avoid if possible
            // mobilising a full Manchester syntax parser.
            transformer = recogniseCommonPattern(text);
            if ( transformer == null ) {
                // No luck. Parse the Manchester expression once on a dummy mapping, so that any
                // syntax error is at least detected immediately.
                try {
                    testParse(text);
                    transformer = (mapping) -> this.parseForMapping(mapping, text);
                } catch ( OWLParserException e ) {
                    throw new SSSOMTransformError(String.format("Cannot parse Manchester expression \"%\"", text));
                } catch ( IllegalArgumentException e ) {
                    throw new SSSOMTransformError(e.getMessage());
                }
            }
            break;
        }

        return transformer != null ? transformer : super.onGeneratingAction(name, arguments);
    }

    @Override
    public String getCurieExpansionFormat() {
        return "<%s>";
    }

    /*
     * Try some patterns on the expression provided as argument to create_axiom to
     * see whether it can be handled by custom generators (more efficient than the
     * default generator, which involves parsing the expression with a full-blown
     * Manchester syntax parser for each mapping).
     */
    private IMappingTransformer<OWLAxiom> recogniseCommonPattern(String text) {
        Matcher m = subclassPattern.matcher(text);
        if ( m.matches() ) {
            return new SubclassAxiomGenerator(ontology, m.group(1).equals("object"));
        }

        m = equivPattern.matcher(text);
        if ( m.matches() ) {
            OWLClassExpression expr = null;
            if ( m.group(3) != null ) {
                expr = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(extractIRI(m.group(4))),
                        factory.getOWLClass(extractIRI(m.group(5))));
            }
            return new EquivalentAxiomGenerator(ontology, expr, m.group(1).equals("object"));
        }

        return null;
    }

    private IRI extractIRI(String text) {
        if ( text.charAt(0) == '<' ) {
            return IRI.create(text.substring(1, text.length() - 1));
        } else {
            return IRI.create(text);
        }
    }

    /*
     * Create a mapping with dummy IDs and try parsing the expression for that
     * mapping.
     */
    private void testParse(String text) {
        Mapping dummy = new Mapping();
        dummy.setSubjectId("http://example.org/EX_0001");
        dummy.setObjectId("http://example.org/EX_0002");

        parseForMapping(dummy, text);
    }

    /*
     * Creates an axiom from a mapping and an expression in Manchester syntax.
     */
    private OWLAxiom parseForMapping(Mapping mapping, String text) {
        // Ensure the parser will recognise the mapping's subject and object
        // (this is assuming mappings are between classes only).
        entityChecker.classNames.add(mapping.getSubjectId());
        entityChecker.classNames.add(mapping.getObjectId());

        manParser.setStringToParse(formatter.format(text, mapping));
        return manParser.parseAxiom();
    }

    private String getSubjectLabel(Mapping mapping) {
        String label = mapping.getSubjectLabel();
        if ( label == null ) {
            label = getLabelFromOntology(mapping.getSubjectId());
        }
        if ( label == null ) {
            label = "Unknown subject";
        }
        return label;
    }

    private String getObjectLabel(Mapping mapping) {
        String label = mapping.getObjectLabel();
        if ( label == null ) {
            label = getLabelFromOntology(mapping.getObjectId());
        }
        if ( label == null ) {
            label = "Unknown object";
        }
        return label;
    }

    private String getLabelFromOntology(String iri) {
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(IRI.create(iri)) ) {
            if ( ax.getProperty().isLabel() && ax.getValue().isLiteral() ) {
                return ax.getValue().asLiteral().get().getLiteral();
            }
        }
        return null;
    }

    private Set<String> getSubclassesOf(String root) {
        Set<String> classes = new HashSet<String>();
        classes.add(root);

        if ( reasoner == null ) {
            reasoner = reasonerFactory.createReasoner(ontology);
        }

        for ( OWLClass c : reasoner.getSubClasses(factory.getOWLClass(IRI.create(root)), false).getFlattened() ) {
            if ( !c.isBottomEntity() ) {
                classes.add(c.getIRI().toString());
            }
        }

        return classes;
    }

    /*
     * Custom entity checker to ensure the Manchester parser will recognise any
     * entity defined in the helper ontology.
     */
    class CustomEntityChecker implements OWLEntityChecker, OWLEntityVisitor {

        private Set<String> classNames = new HashSet<String>();
        private Set<String> objectPropertyNames = new HashSet<String>();
        private Set<String> dataPropertyNames = new HashSet<String>();
        private Set<String> individualNames = new HashSet<String>();
        private Set<String> datatypeNames = new HashSet<String>();
        private Set<String> annotationPropertyNames = new HashSet<String>();

        CustomEntityChecker(OWLOntology ontology) {
            for ( OWLDeclarationAxiom d : ontology.getAxioms(AxiomType.DECLARATION, Imports.INCLUDED) ) {
                d.getEntity().accept(this);
            }
        }

        private String getUnquotedIRI(String name) {
            int len = name.length();
            if ( len > 0 && name.charAt(0) == '<' ) {
                return name.substring(1, len - 1);
            }
            return name;
        }

        @Override
        public OWLClass getOWLClass(String name) {
            name = getUnquotedIRI(name);
            if ( classNames.contains(name) ) {
                return factory.getOWLClass(IRI.create(name));
            }
            return null;
        }

        @Override
        public OWLObjectProperty getOWLObjectProperty(String name) {
            name = getUnquotedIRI(name);
            if ( objectPropertyNames.contains(name) ) {
                return factory.getOWLObjectProperty(IRI.create(name));
            }
            return null;
        }

        @Override
        public OWLDataProperty getOWLDataProperty(String name) {
            name = getUnquotedIRI(name);
            if ( dataPropertyNames.contains(name) ) {
                return factory.getOWLDataProperty(IRI.create(name));
            }
            return null;
        }

        @Override
        public OWLNamedIndividual getOWLIndividual(String name) {
            name = getUnquotedIRI(name);
            if ( individualNames.contains(name) ) {
                return factory.getOWLNamedIndividual(IRI.create(name));
            }
            return null;
        }

        @Override
        public OWLDatatype getOWLDatatype(String name) {
            name = getUnquotedIRI(name);
            if ( datatypeNames.contains(name) ) {
                return factory.getOWLDatatype(IRI.create(name));
            }
            return null;
        }

        @Override
        public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
            name = getUnquotedIRI(name);
            if ( annotationPropertyNames.contains(name) ) {
                return factory.getOWLAnnotationProperty(IRI.create(name));
            }
            return null;
        }

        @Override
        public void visit(OWLClass cls) {
            classNames.add(cls.getIRI().toString());

        }

        @Override
        public void visit(OWLObjectProperty property) {
            objectPropertyNames.add(property.getIRI().toString());
        }

        @Override
        public void visit(OWLDataProperty property) {
            dataPropertyNames.add(property.getIRI().toString());
        }

        @Override
        public void visit(OWLNamedIndividual individual) {
            individualNames.add(individual.getIRI().toString());
        }

        @Override
        public void visit(OWLDatatype datatype) {
            datatypeNames.add(datatype.getIRI().toString());
        }

        @Override
        public void visit(OWLAnnotationProperty property) {
            annotationPropertyNames.add(property.getIRI().toString());
        }
    }
}
