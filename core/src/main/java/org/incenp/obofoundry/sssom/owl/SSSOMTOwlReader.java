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
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
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
public class SSSOMTOwlReader extends SSSOMTransformReader<OWLAxiom> {

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

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        manParser = new ManchesterOWLSyntaxParserImpl(() -> config, factory);
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
        Set<OWLAxiom> tmpAxioms = ensureClassesExist(mapping);

        manParser.setDefaultOntology(ontology);
        manParser.setStringToParse(getFormatter().format(text, mapping));
        OWLAxiom parsedAxiom = manParser.parseAxiom();

        if ( tmpAxioms.size() > 0 ) {
            ontology.getOWLOntologyManager().removeAxioms(ontology, tmpAxioms);
        }

        return parsedAxiom;
    }

    /*
     * Checks whether a given class exists in the helper ontology, and create its
     * declaration axiom if it does not.
     */
    private void ensureClassExists(String entity, Set<OWLAxiom> addedAxioms) {
        IRI entityIRI = IRI.create(entity);
        if ( !ontology.containsClassInSignature(entityIRI) ) {
            OWLDeclarationAxiom ax = factory.getOWLDeclarationAxiom(factory.getOWLClass(entityIRI));
            ontology.getOWLOntologyManager().addAxiom(ontology, ax);
            addedAxioms.add(ax);
        }
    }

    /*
     * Checks whether the subject and object exist in the helper ontology, and add
     * the required declaration axioms if not. Returns the axioms that were added as
     * a (possibly empty) set.
     */
    private Set<OWLAxiom> ensureClassesExist(Mapping mapping) {
        Set<OWLAxiom> addedAxioms = new HashSet<OWLAxiom>();
        ensureClassExists(mapping.getSubjectId(), addedAxioms);
        ensureClassExists(mapping.getObjectId(), addedAxioms);
        return addedAxioms;
    }
}
