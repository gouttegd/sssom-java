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

package org.incenp.obofoundry.sssom;

import java.util.HashSet;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Generate bridging axioms for cross-species mappings.
 */
public class CrossSpeciesBridgeGenerator {

    public static String CROSS_SPECIES_EXACT_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesExactMatch";
    public static String CROSS_SPECIES_BROAD_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesBroadMatch";
    public static String CROSS_SPECIES_NARROW_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesNarrowMatch";
    public static String CROSS_SPECIES_RELATED_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesRelatedMatch";
    public static String OBOFOUNDRY_UNIQUE_LABEL = "http://purl.obolibrary.org/obo/IAO_0000589";

    private static final IRI partOfIRI = IRI.create("http://purl.obolibrary.org/obo/BFO_0000050");

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLClassExpression partOfTaxon;
    private String taxonName;

    public CrossSpeciesBridgeGenerator(OWLOntology ontology, IRI taxonIRI) {
        this(ontology, taxonIRI, null);
    }

    public CrossSpeciesBridgeGenerator(OWLOntology ontology, IRI taxonIRI, String taxonName) {
        this.ontology = ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.taxonName = taxonName;

        partOfTaxon = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(partOfIRI),
                factory.getOWLClass(taxonIRI));
    }

    public Set<OWLAxiom> generateAxioms(Mapping mapping) {
        OWLClass subject = factory.getOWLClass(IRI.create(mapping.getSubjectId()));
        OWLClass object = factory.getOWLClass(IRI.create(mapping.getObjectId()));

        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        if ( mapping.getPredicateId().equals(CROSS_SPECIES_EXACT_MATCH) ) {
            OWLClassExpression oAndPartOfTaxon = factory.getOWLObjectIntersectionOf(object, partOfTaxon);
            axioms.add(factory.getOWLEquivalentClassesAxiom(subject, oAndPartOfTaxon));

            if ( taxonName != null ) {
                OWLAxiom uniqueLabelAxiom = generateOBOUniqueLabelAnnotation(subject.getIRI());
                if ( uniqueLabelAxiom != null ) {
                    axioms.add(uniqueLabelAxiom);
                }
            }
        }

        // TODO: Implement other types of mapping
        return axioms;
    }

    private OWLAxiom generateOBOUniqueLabelAnnotation(IRI subject) {
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(subject) ) {
            if ( ax.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI()) ) {
                String label = ax.getValue().asLiteral().get().getLiteral();

                return factory.getOWLAnnotationAssertionAxiom(
                        factory.getOWLAnnotationProperty(IRI.create(OBOFOUNDRY_UNIQUE_LABEL)), subject,
                        factory.getOWLLiteral(String.format("%s (%s)", label, taxonName)));
            }
        }

        return null;
    }
}
