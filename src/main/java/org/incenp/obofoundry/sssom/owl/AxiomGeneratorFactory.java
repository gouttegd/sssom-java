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

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.IMappingTransformerFactory;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
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
    public IMappingTransformer<OWLAxiom> create(String text, PrefixManager prefixManager) {
        this.prefixManager = prefixManager;

        return (mapping) -> this.parseForMapping(mapping, text);
    }

    /**
     * Creates an axiom from a mapping and an expression in Manchester syntax. The
     * expression may contain the following placeholders:
     * <ul>
     * <li>{@code %subject_id} will be replaced with the subject’s IRI;
     * <li>{@code %subject_curie} will be replaced by the shortened version of the
     * subject’s IRI;
     * <li>{@code %subject_label} will be replaced by the the subject label;
     * <li>similar placeholders for the object.
     * </ul>
     * 
     * @param mapping The mapping to create an axiom from.
     * @param text    A representation of the axiom to create in Manchester syntax.
     * @return The generated axiom.
     */
    public OWLAxiom parseForMapping(Mapping mapping, String text) {
        /*
         * Make sure the classes for the subject and object exist in the helper
         * ontology. This is needed for the parser to recognise them.
         */
        ensureClassExists(mapping.getSubjectId());
        ensureClassExists(mapping.getObjectId());

        manParser.setDefaultOntology(ontology);
        manParser.setStringToParse(substituteMappingVariables(text, mapping));

        return manParser.parseAxiom();
    }

    /*
     * Check whether a given class exists in the helper ontology, and create its
     * declaration axiom if it does not.
     */
    private void ensureClassExists(String entity) {
        IRI entityIRI = IRI.create(entity);
        if ( !ontology.containsClassInSignature(entityIRI) ) {
            OWLDeclarationAxiom ax = factory.getOWLDeclarationAxiom(factory.getOWLClass(entityIRI));
            ontology.getOWLOntologyManager().addAxiom(ontology, ax);
        }
    }

    /*
     * Replace placeholders within a string by values obtained from the mapping.
     */
    private String substituteMappingVariables(String source, Mapping mapping) {
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
}
