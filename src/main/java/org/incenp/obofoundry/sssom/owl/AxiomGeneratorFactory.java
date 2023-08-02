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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.IMappingTransformerFactory;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
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
 * A factory to create mapping-based OWL axiom generators. This class creates
 * generators that will themselves create OWL axioms from SSSOM mappings. The
 * axiom generators are built from statements in Manchester syntax.
 */
public class AxiomGeneratorFactory implements IMappingTransformerFactory<OWLAxiom> {

    private static final Pattern curiePattern = Pattern.compile("[A-Za-z0-9_]+:[A-Za-z0-9_]+");

    /*
     * Common expression patterns that are handled through specialised generators.
     * 
     * Annotation: %subject_id Annotation PROP:1234 "value"
     * 
     * Equivalence: %subject_id EquivalentTo %object_id and (REL:1234 some FIL:1234)
     * 
     * Subclass: %subject_id SubclassOf %object_id
     */
    private static final Pattern annotPattern = Pattern
            .compile("%(subject|object)_id Annotation (<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+) (\"[^\"]+\"|'[^']+')");
    private static final Pattern equivPattern = Pattern.compile(
            "%(subject|object)_id EquivalentTo %(subject|object)_id( and \\((<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+) some (<[^>]+>|[A-Za-z0-9_]+:[A-Za-z0-9_]+)\\))?");
    private static final Pattern subclassPattern = Pattern
            .compile("%(subject|object)_id SubclassOf %(subject|object)_id");

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private ManchesterOWLSyntaxParser manParser;
    private PrefixManager prefixManager;

    /**
     * Creates a new instance.
     * 
     * @param ontology A helper ontology. This does not need to be the ontology the
     *                 axioms will be injected to afterwards, but it must contain
     *                 declaration axioms for all the object and annotation
     *                 properties that may be used by the axioms the generator will
     *                 need to create.
     */
    public AxiomGeneratorFactory(OWLOntology ontology) {
        this.ontology = ontology;
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        manParser = new ManchesterOWLSyntaxParserImpl(() -> config, factory);
    }

    @Override
    public IMappingTransformer<OWLAxiom> create(String text, PrefixManager prefixManager) throws SSSOMTransformError {
        this.prefixManager = prefixManager;

        // Direct mode?
        if ( text.equalsIgnoreCase("direct") ) {
            return new DirectAxiomGenerator(ontology);
        }

        String expandedText = expandEmbeddedIdentifiers(text);

        // First look if the expression is a "common" one, to avoid if possible
        // mobilising a full Manchester syntax parser.
        IMappingTransformer<OWLAxiom> transformer = recogniseCommonPattern(text);

        if ( transformer == null ) {
            // No luck. Parse the Manchester expression once, so that any syntax error is
            // detected immediately instead of waiting until we try to apply it to mappings.
            try {
                testParse(expandedText);
                transformer = (mapping) -> this.parseForMapping(mapping, expandedText);
            } catch ( OWLParserException e ) {
                throw new SSSOMTransformError(String.format("Cannot parse Manchester expression \"%s\"", expandedText));
            }
        }

        return transformer;
    }

    /**
     * Creates an axiom from a mapping and an expression in Manchester syntax. The
     * expression may contain placeholders representing the value of some fields
     * from the mapping (see
     * {@link #substituteMappingVariables(String, Mapping, PrefixManager)} for a
     * list of recognised placeholders).
     * 
     * @param mapping The mapping to create an axiom from.
     * @param text    A representation of the axiom to create in Manchester syntax.
     * @return The generated axiom.
     */
    public OWLAxiom parseForMapping(Mapping mapping, String text) {
        // Make sure the classes for the subject and object exist in the helper
        // ontology. This is needed for the parser to recognise them.
        Set<OWLAxiom> tmpAxioms = ensureClassesExist(mapping);

        manParser.setDefaultOntology(ontology);
        manParser.setStringToParse(substituteMappingVariables(text, mapping, prefixManager));
        OWLAxiom parsedAxiom = manParser.parseAxiom();

        // Remove any axiom we may have had to add
        if ( tmpAxioms.size() > 0 ) {
            ontology.getOWLOntologyManager().removeAxioms(ontology, tmpAxioms);
        }

        return parsedAxiom;
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

    /**
     * Replace placeholders in a string by values derived from a mapping.
     * <p>
     * The following placeholders are recognised:
     * <ul>
     * <li>{@code %subject_id} will be replaced with the subject’s IRI;
     * <li>{@code %subject_curie} will be replaced by the shortened version of the
     * subject’s IRI;
     * <li>{@code %subject_label} will be replaced by the the subject label;
     * <li>similar placeholders for the object.
     * </ul>
     * 
     * @param source        The string in which placeholders should be replaced.
     * @param mapping       The mapping to get the replacing values from.
     * @param prefixManager A prefix manager. This is needed to generate the
     *                      shortened identifiers for the {@code %subject_curie} and
     *                      {@code object_curie} placeholders. May be {@code null},
     *                      in which case those placeholders will not be replaced.
     * @return A string with the placeholders replaced by the corresponding values.
     */
    public static String substituteMappingVariables(String source, Mapping mapping, PrefixManager prefixManager) {
        if ( source.contains("%subject_label") && mapping.getSubjectLabel() != null ) {
            source = source.replace("%subject_label", mapping.getSubjectLabel());
        }
        if ( source.contains("%object_label") && mapping.getObjectLabel() != null ) {
            source = source.replace("%object_label", mapping.getObjectLabel());
        }
        if ( source.contains("%subject_curie") && prefixManager != null ) {
            source = source.replace("%subject_curie", prefixManager.shortenIdentifier(mapping.getSubjectId()));
        }
        if ( source.contains("%object_curie") && prefixManager != null ) {
            source = source.replace("%object_curie", prefixManager.shortenIdentifier(mapping.getObjectId()));
        }

        source = source.replace("%subject_id", String.format("<%s>", mapping.getSubjectId()));
        source = source.replace("%object_id", String.format("<%s>", mapping.getObjectId()));

        return source;
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
     * Try some patterns on the expression to see if it can be handled by custom
     * generators (more efficient than the default generator, which involves parsing
     * the expression with a full-blown Manchester syntax parser for each mapping).
     * 
     * It also allows to deal with annotation axioms.
     */
    private IMappingTransformer<OWLAxiom> recogniseCommonPattern(String text) {
        Matcher m = annotPattern.matcher(text);
        if ( m.matches() ) {
            String value = m.group(3);
            value = value.substring(1, value.length() - 1);
            return new AnnotationAxiomGenerator(ontology, extractIRI(m.group(2)), value, prefixManager,
                    m.group(1).equals("object"));
        }

        m = subclassPattern.matcher(text);
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
}
