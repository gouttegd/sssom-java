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
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.IMetadataTransformer;
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
import org.semanticweb.owlapi.model.OWLLiteral;
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
            "%(subject|object)_id +EquivalentTo: +%(subject|object)_id( +and +\\((<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+) +some +(<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+)\\))?");
    private static final Pattern subclassPattern = Pattern
            .compile("%(subject|object)_id +SubClassOf: +%(subject|object)_id");

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasonerFactory reasonerFactory;
    private OWLReasoner reasoner;
    private ManchesterOWLSyntaxParser manParser;
    private MappingFormatter formatter;
    private CustomEntityChecker entityChecker;
    private VariableManager varManager;
    private OWLLiteral falseValue = null;

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

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        manParser = new ManchesterOWLSyntaxParserImpl(() -> config, factory);

        formatter = new MappingFormatter();
        formatter.addSubstitution("%subject_label", (mapping) -> getSubjectLabel(mapping));
        formatter.addSubstitution("%object_label", (mapping) -> getObjectLabel(mapping));
        formatter.addSubstitution("%subject_id", (mapping) -> String.format("<%s>", mapping.getSubjectId()));
        formatter.addSubstitution("%object_id", (mapping) -> String.format("<%s>", mapping.getObjectId()));

        varManager = new VariableManager();
    }

    /**
     * Gets the entity checker from this application. This is mostly intended so
     * that downstream code can know about any class or object property that has
     * been declared using the {@code declare_class} and
     * {@code declare_object_property} header functions in a SSSOM/T file.
     * 
     * @return The entity checker used by this application when parsing Manchester
     *         syntax expressions.
     */
    public OWLEntityChecker getEntityChecker() {
        return entityChecker;
    }

    @Override
    public void onInit(PrefixManager pm) {
        formatter.addSubstitution("%subject_curie", (mapping) -> pm.shortenIdentifier(mapping.getSubjectId()));
        formatter.addSubstitution("%object_curie", (mapping) -> pm.shortenIdentifier(mapping.getObjectId()));

        entityChecker = new CustomEntityChecker(ontology, pm);
        manParser.setOWLEntityChecker(entityChecker);
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
            if ( arguments.size() < 2 || arguments.size() > 3 ) {
                throw new SSSOMTransformError(
                        String.format("Invalid number of arguments for function set_var: expected 2 or 3, found %d",
                                arguments.size()));
            }
            String varName = arguments.get(0);
            String varValue = arguments.get(1);
            IMappingFilter filter = null;
            if ( arguments.size() == 3 ) {
                // Condition is of the form "(%subject_id|%object_id) is_a <ID>"
                String condition = arguments.get(2);
                String[] parts = condition.split(" ", 3);
                if ( parts.length != 3 || !parts[1].equals("is_a")
                        || (!parts[0].equals("%subject_id") && !parts[0].equals("%object_id")) ) {
                    throw new SSSOMTransformError(String.format("Invalid condition for set_var: %s", condition));
                }

                Set<String> targetIds = getSubclassesOf(entityChecker.getUnquotedIRI(parts[2]));
                if ( parts[0].equals("%subject_id") ) {
                    filter = (mapping) -> targetIds.contains(mapping.getSubjectId());
                } else if ( parts[0].equals("%object_id") ) {
                    filter = (mapping) -> targetIds.contains(mapping.getObjectId());
                }
            }
            varManager.addVariable(varName, varValue, filter);
            formatter.addSubstitution("%" + varName, (mapping) -> varManager.expandVariable(varName, mapping));
            return;
        }

        super.onHeaderAction(name, arguments);
    }

    @Override
    public IMappingTransformer<Mapping> onPreprocessingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        switch ( name ) {
        case "check_subject_existence":
            return (mapping) -> checkExistence(mapping.getSubjectId()) ? mapping : null;

        case "check_object_existence":
            return (mapping) -> checkExistence(mapping.getObjectId()) ? mapping : null;
        }
        return super.onPreprocessingAction(name, arguments);
    }

    @Override
    public IMappingTransformer<OWLAxiom> onGeneratingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        IMappingTransformer<OWLAxiom> transformer = null;

        switch ( name ) {
        case "direct":
            checkArguments(name, 0, arguments);
            transformer = new AnnotatedAxiomGenerator(ontology);
            break;

        case "annotate_subject":
        case "annotate_object":
            checkArguments(name, 2, arguments, true);
            transformer = new AnnotationAxiomGenerator(ontology, IRI.create(arguments.get(0)),
                    formatter.getTransformer(arguments.get(1)), name.endsWith("_object"));
            if ( arguments.size() == 3 ) {
                transformer = createAnnotatedTransformer(transformer, arguments.get(2));
            }
            break;

        case "create_axiom":
            checkArguments(name, 1, arguments, true);
            String text = arguments.get(0);

            // First look if the expression is a "common" one, to avoid if possible
            // mobilising a full Manchester syntax parser.
            try {
                transformer = recogniseCommonPattern(text);
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMTransformError(e.getMessage());
            }

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

            if ( arguments.size() == 2 ) {
                transformer = createAnnotatedTransformer(transformer, arguments.get(1));
            }
            break;
        }

        return transformer != null ? transformer : super.onGeneratingAction(name, arguments);
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
            boolean invert = m.group(1).equals("object");
            if ( m.group(3) == null ) {
                return new EquivalentAxiomGenerator(ontology, (OWLClassExpression) null, invert);
            }
            String relation = entityChecker.getUnquotedIRI(m.group(4));
            String filler = entityChecker.getUnquotedIRI(m.group(5));
            if ( relation.charAt(0) == '%' || filler.charAt(0) == '%' ) {
                IMappingTransformer<String> relGenerator = getVariableGenerator(relation);
                IMappingTransformer<String> fillerGenerator = getVariableGenerator(filler);
                IMappingTransformer<OWLClassExpression> exprGenerator = (mapping) -> factory.getOWLObjectSomeValuesFrom(
                        factory.getOWLObjectProperty(IRI.create(relGenerator.transform(mapping))),
                        factory.getOWLClass(IRI.create(fillerGenerator.transform(mapping))));
                return new EquivalentAxiomGenerator(ontology, exprGenerator, invert);
            } else {
                OWLClassExpression expr = factory.getOWLObjectSomeValuesFrom(
                        entityChecker.getOWLObjectProperty(relation), entityChecker.getOWLClass(filler));
                return new EquivalentAxiomGenerator(ontology, expr, invert);
            }
        }

        return null;
    }

    /*
     * If name is a registered variable, gets a transformer to expand the variable
     * value at runtime.
     */
    private IMappingTransformer<String> getVariableGenerator(String name) {
        if ( name.charAt(0) == '%' ) {
            return varManager.getTransformer(name.substring(1));
        } else {
            return (mapping) -> name;
        }
    }

    /*
     * Creates a mapping with dummy IDs and try parsing the expression for that
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

    /*
     * Gets the label of the mapping subject; try to obtain it from the helper
     * ontology if the mapping itself has no subject label.
     */
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

    /*
     * Same but for the object.
     */
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

    /*
     * Extract a label from the helper ontology.
     */
    private String getLabelFromOntology(String iri) {
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(IRI.create(iri)) ) {
            if ( ax.getProperty().isLabel() && ax.getValue().isLiteral() ) {
                return ax.getValue().asLiteral().get().getLiteral();
            }
        }
        return null;
    }

    /*
     * Gets a list of all subclasses of the provided root class.
     */
    private Set<String> getSubclassesOf(String root) {
        Set<String> classes = new HashSet<String>();
        classes.add(root.toString());

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
     * Check that a given class exists and is not obsolete.
     */
    private boolean checkExistence(String cls) {
        if ( falseValue == null ) {
            falseValue = factory.getOWLLiteral(false);
        }

        IRI clsIRI = IRI.create(cls);
        if ( ontology.containsClassInSignature(clsIRI) ) {
            for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(clsIRI) ) {
                if ( ax.getProperty().isDeprecated() ) {
                    if ( ax.getValue().asLiteral().or(falseValue).parseBoolean() ) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private IMappingTransformer<OWLAxiom> createAnnotatedTransformer(IMappingTransformer<OWLAxiom> inner, String spec)
            throws SSSOMTransformError {
        IMetadataTransformer<Mapping, IRI> transformer = null;
        String[] items = spec.replaceAll("( |\r|\n|\r)", "").split(":", 2);
        if ( items[0].equalsIgnoreCase("direct") ) {
            transformer = new DirectMetadataTransformer();
        } else {
            throw new SSSOMTransformError(String.format("Unknown metadata transformer: %s", items[0]));
        }

        if ( items.length == 1 ) {
            return new AnnotatedAxiomGenerator(ontology, inner, transformer);
        } else {
            return new AnnotatedAxiomGenerator(ontology, inner, transformer, SlotHelper.getMappingSlotList(items[1]));
        }
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
        private OWLDataFactory factory;
        private PrefixManager prefixManager;

        CustomEntityChecker(OWLOntology ontology, PrefixManager prefixManager) {
            this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
            this.prefixManager = prefixManager;
            for ( OWLDeclarationAxiom d : ontology.getAxioms(AxiomType.DECLARATION, Imports.INCLUDED) ) {
                d.getEntity().accept(this);
            }
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

        private String getUnquotedIRI(String name) {
            int len = name.length();
            if ( len > 1 && name.charAt(0) == '<' ) {
                name = name.substring(1, len - 1);
            } else {
                name = prefixManager.expandIdentifier(name);
            }

            return name;
        }
    }
}
