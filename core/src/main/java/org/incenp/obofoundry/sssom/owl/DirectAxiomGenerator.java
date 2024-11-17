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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A class to generate OWL axioms “directly”, that is without any external input
 * outside of the mappings themselves. The axioms are derived based on the “OWL
 * reification” rules set forth in the SSSOM specification.
 *
 * @see <a href=
 *      "https://mapping-commons.github.io/sssom/spec-formats-owl/">SSSOM
 *      specification</a>
 */
public class DirectAxiomGenerator implements IMappingTransformer<OWLAxiom> {

    private final static Set<String> ANNOTATION_PREDICATES = new HashSet<String>();
    private final static String OWL_EQUIVALENT_CLASS = "http://www.w3.org/2002/07/owl#equivalentClass";
    private final static String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";

    static {
        ANNOTATION_PREDICATES.add("http://www.geneontology.org/formats/oboInOwl#hasDbXref");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2000/01/rdf-schema#seeAlso");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#exactMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#closeMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#relatedMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#narrowMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#broadMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesExactMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesCloseMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesNarrowMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesBroadMatch");
    }

    private OWLDataFactory factory;
    private OWLOntology ontology;

    /**
     * Creates a new instance.
     * 
     * @param ontology The ontology to generate axioms for.
     */
    public DirectAxiomGenerator(OWLOntology ontology) {
        this.ontology = ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        OWLAxiom axiom = null;
        String predicate = mapping.getPredicateId();
        IRI subject = IRI.create(mapping.getSubjectId());
        IRI object = IRI.create(mapping.getObjectId());

        if ( predicate.equals(OWL_EQUIVALENT_CLASS) ) {
            axiom = factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(object), factory.getOWLClass(object));
        } else if ( predicate.equals(RDFS_SUBCLASS_OF) ) {
            axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLClass(object));
        } else if ( ANNOTATION_PREDICATES.contains(predicate) ) {
            axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(IRI.create(predicate)),
                    subject, object);
        } else {
            IRI predicateIRI = IRI.create(predicate);
            if ( ontology.containsAnnotationPropertyInSignature(predicateIRI) ) {
                axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(predicateIRI), subject,
                        object);
            } else if ( ontology.containsObjectPropertyInSignature(predicateIRI) ) {
                axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLObjectSomeValuesFrom(
                        factory.getOWLObjectProperty(predicateIRI), factory.getOWLClass(object)));
            }
        }

        return axiom;
    }
}
