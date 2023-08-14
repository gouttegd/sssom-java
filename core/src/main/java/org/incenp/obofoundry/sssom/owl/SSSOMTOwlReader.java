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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.MappingFormatter;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReaderBase;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
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
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * A parser to read mapping processing rules in the SSSOM Transform language,
 * where the rules are intended to produce OWL axioms for injection into an
 * ontology.
 * <p>
 * This parser recognises the following instructions:
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
public class SSSOMTOwlReader extends SSSOMTransformReaderBase<OWLAxiom> {

    private static final Pattern curiePattern = Pattern.compile("[A-Za-z0-9_]+:[A-Za-z0-9_]+");

    /*
     * Common expression patterns that are handled through specialised generators.
     * 
     * Equivalence: %subject_id EquivalentTo %object_id and (REL:1234 some FIL:1234)
     * 
     * Subclass: %subject_id SubclassOf %object_id
     */
    private static final Pattern equivPattern = Pattern.compile(
            "%(subject|object)_id EquivalentTo: %(subject|object)_id( and \\((<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+) some (<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+)\\))?");
    private static final Pattern subclassPattern = Pattern
            .compile("%(subject|object)_id SubClassOf: %(subject|object)_id");

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private ManchesterOWLSyntaxParser manParser;
    private MappingFormatter formatter;
    CustomEntityChecker entityChecker;

    /**
     * Creates a new instance.
     * 
     * @param filename The name of the SSSOM/T file to read the rules from.
     * @param ontology A helper ontology. This does not need to be the ontology the
     *                 axioms will be injected into afterwards, but it must contain
     *                 declaration axioms for all the object and annotation
     *                 properties that may be used in {@code create_axiom}
     *                 instructions.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     file.
     */
    public SSSOMTOwlReader(String filename, OWLOntology ontology) throws IOException {
        super(filename);

        this.ontology = ontology;
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        entityChecker = new CustomEntityChecker(ontology);

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        manParser = new ManchesterOWLSyntaxParserImpl(() -> config, factory);
        manParser.setOWLEntityChecker(entityChecker);
    }

    @Override
    protected IMappingTransformer<OWLAxiom> parseGeneratingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        if ( name.equals("direct") ) {
            return new DirectAxiomGenerator(ontology);
        } else if ( name.equals("annotate_subject") ) {
            if ( arguments.size() != 2 ) {
                throw new SSSOMTransformError(String.format(
                        "Invalid number of arguments for annotate_subject function (expected 2, found %d)",
                        arguments.size()));
            }
            return new AnnotationAxiomGenerator(ontology, IRI.create(arguments.get(0)),
                    getFormatter().getTransformer(arguments.get(1)));
        } else if ( name.equals("annotate_object") ) {
            if ( arguments.size() != 2 ) {
                throw new SSSOMTransformError(String.format(
                        "Invalid number of arguments for annotate_object function (expected 2, found %d)",
                        arguments.size()));
            }
            return new AnnotationAxiomGenerator(ontology, IRI.create(arguments.get(0)),
                    getFormatter().getTransformer(arguments.get(1)), true);
        } else if ( name.equals("create_axiom") ) {
            if ( arguments.size() != 1 ) {
                throw new SSSOMTransformError(String.format(
                        "Invalid number of arguments for owl function (expected 1, found %d", arguments.size()));
            }

            String text = expandEmbeddedIdentifiers(arguments.get(0));

            // First look if the expression is a "common" one, to avoid if possible
            // mobilising a full Manchester syntax parser.
            IMappingTransformer<OWLAxiom> transformer = recogniseCommonPattern(text);
            if ( transformer == null ) {
                // No luck. Parse the Manchester expression once on a dummy mapping, so that any
                // syntax error is at least detected immediately instead of waiting until we try
                // to apply it to actual mappings.
                try {
                    testParse(text);
                    transformer = (mapping) -> this.parseForMapping(mapping, text);
                } catch ( OWLParserException e ) {
                    throw new SSSOMTransformError(String.format("Cannot parse Manchester expression \"%\"", text));
                }
            }

            return transformer;
        }

        throw new SSSOMTransformError(String.format("Unrecognized function: %s", name));
    }

    @Override
    protected void parseHeaderAction(String name, List<String> arguments) {
        if ( name.equals("declare_class") ) {
            arguments.forEach((c) -> entityChecker.classNames.add(c));
        } else if ( name.equals("declare_object_property") ) {
            arguments.forEach((c) -> entityChecker.objectPropertyNames.add(c));
        }
    }

    /*
     * Initialise the formatter object to substitute %-prefixed placeholders within
     * a string with values from a mapping.
     */
    private MappingFormatter getFormatter() {
        if ( formatter == null ) {
            formatter = new MappingFormatter();
            formatter.addSubstitution("%subject_label", (mapping) -> mapping.getSubjectLabel());
            formatter.addSubstitution("%object_label", (mapping) -> mapping.getObjectLabel());
            formatter.addSubstitution("%subject_curie",
                    (mapping) -> prefixManager.shortenIdentifier(mapping.getSubjectId()));
            formatter.addSubstitution("%object_curie",
                    (mapping) -> prefixManager.shortenIdentifier(mapping.getObjectId()));
            formatter.addSubstitution("%subject_id", (mapping) -> String.format("<%s>", mapping.getSubjectId()));
            formatter.addSubstitution("%object_id", (mapping) -> String.format("<%s>", mapping.getObjectId()));
        }
        return formatter;
    }

    /*
     * Expand any CURIE that may lurk inside a string.
     */
    private String expandEmbeddedIdentifiers(String source) {
        Matcher curieFinder = curiePattern.matcher(source);
        Set<String> curies = new HashSet<String>();
        while ( curieFinder.find() ) {
            curies.add(curieFinder.group());
        }

        for ( String curie : curies ) {
            String iri = prefixManager.expandIdentifier(curie);
            if ( !iri.equals(curie) ) {
                source = source.replace(curie, String.format("<%s>", iri));
            }
        }

        return source;
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
            return IRI.create(prefixManager.expandIdentifier(text));
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

        manParser.setStringToParse(getFormatter().format(text, mapping));
        return manParser.parseAxiom();
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
