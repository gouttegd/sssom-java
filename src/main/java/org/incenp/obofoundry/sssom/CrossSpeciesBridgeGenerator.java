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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Generate bridging axioms for cross-species mappings.
 */
public class CrossSpeciesBridgeGenerator {

    public static String CROSS_SPECIES_EXACT_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesExactMatch";
    public static String CROSS_SPECIES_BROAD_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesBroadMatch";
    public static String CROSS_SPECIES_NARROW_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesNarrowMatch";
    public static String CROSS_SPECIES_RELATED_MATCH = "https://w3id.org/semapv/vocab/crossSpeciesRelatedMatch";

    private static final IRI partOfIRI = IRI.create("http://purl.obolibrary.org/obo/BFO_0000050");

    IMappingFilter predicateFilter;
    List<IMappingTransformer<OWLAxiom>> axiomGenerators;

    public CrossSpeciesBridgeGenerator(OWLOntology ontology, IRI taxonIRI) {
        this(ontology, taxonIRI, null);
    }

    public CrossSpeciesBridgeGenerator(OWLOntology ontology, IRI taxonIRI, String taxonName) {
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

        OWLClassExpression partOfTaxon = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(partOfIRI),
                factory.getOWLClass(taxonIRI));
        
        predicateFilter = (mapping) -> mapping.getPredicateId().equals(CROSS_SPECIES_EXACT_MATCH);
        axiomGenerators = new ArrayList<IMappingTransformer<OWLAxiom>>();
        axiomGenerators.add(new EquivalentAxiomGenerator(ontology, partOfTaxon));
        if ( taxonName != null ) {
            axiomGenerators.add(new UniqueLabelGenerator(ontology, String.format("%%s (%s)", taxonName)));
        }
    }

    public Set<OWLAxiom> generateAxioms(Mapping mapping) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        if ( predicateFilter.filter(mapping) ) {
            for ( IMappingTransformer<OWLAxiom> t : axiomGenerators ) {
                OWLAxiom ax = t.transform(mapping);
                if ( ax != null ) {
                    axioms.add(ax);
                }
            }
        }

        return axioms;
    }
}
