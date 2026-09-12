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

import org.incenp.obofoundry.sssom.model.EntityType;
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
    private final static String OWL_EQUIVALENT_PROPERTY = "http://www.w3.org/2002/07/owl#equivalentProperty";
    private final static String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
    private final static String RDFS_SUBPROPERTY_OF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";

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
        EntityType predicateType = mapping.getPredicateType();
        IRI subject = IRI.create(mapping.getSubjectId());
        IRI object = IRI.create(mapping.getObjectId());

        /*
         * The type of axiom to generate is dictated by the type of the predicate, which
         * we obtain from (by order of precedence):
         * 
         * (1) the mapping itself, if it has a `predicate_type` slot set to either `owl
         * annotation property` or `owl object property`;
         * 
         * (2) some built-in knowledge for a handful of predicates (owl:equivalentClass,
         * rdfs:subClassOf, owl:equivalentProperty, rdfs:subPropertyOf, and the
         * predicates listed in ANNOTATION_PREDICATES);
         * 
         * (3) the helper ontology, it it declares an annotation or object property with
         * a matching IRI.
         */

        if ( predicateType == EntityType.OWL_ANNOTATION_PROPERTY ) {
            axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(IRI.create(predicate)),
                    subject, object);
        } else if ( predicateType == EntityType.OWL_OBJECT_PROPERTY ) {
            axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLObjectSomeValuesFrom(
                    factory.getOWLObjectProperty(IRI.create(predicate)), factory.getOWLClass(object)));
        } else if ( predicate.equals(OWL_EQUIVALENT_CLASS) ) {
            axiom = factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(object), factory.getOWLClass(object));
        } else if ( predicate.equals(RDFS_SUBCLASS_OF) ) {
            axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLClass(object));
        } else if ( predicate.equals(OWL_EQUIVALENT_PROPERTY) ) {
            EntityType subjectType = getPropertyType(subject, mapping.getSubjectType(), false);
            EntityType objectType = getPropertyType(object, mapping.getObjectType(), false);
            if ( subjectType == objectType ) {
                if ( subjectType == EntityType.OWL_OBJECT_PROPERTY ) {
                    axiom = factory.getOWLEquivalentObjectPropertiesAxiom(factory.getOWLObjectProperty(subject),
                            factory.getOWLObjectProperty(object));
                } else if ( subjectType == EntityType.OWL_DATA_PROPERTY ) {
                    axiom = factory.getOWLEquivalentDataPropertiesAxiom(factory.getOWLDataProperty(subject),
                            factory.getOWLDataProperty(object));
                }
            }
        } else if ( predicate.equals(RDFS_SUBPROPERTY_OF) ) {
            EntityType subjectType = getPropertyType(subject, mapping.getSubjectType(), true);
            EntityType objectType = getPropertyType(object, mapping.getObjectType(), true);
            if ( subjectType == objectType ) {
                if ( subjectType == EntityType.OWL_OBJECT_PROPERTY ) {
                    axiom = factory.getOWLSubObjectPropertyOfAxiom(factory.getOWLObjectProperty(subject),
                            factory.getOWLObjectProperty(object));
                } else if ( subjectType == EntityType.OWL_DATA_PROPERTY ) {
                    axiom = factory.getOWLSubDataPropertyOfAxiom(factory.getOWLDataProperty(subject),
                            factory.getOWLDataProperty(object));
                } else if ( subjectType == EntityType.OWL_ANNOTATION_PROPERTY ) {
                    axiom = factory.getOWLSubAnnotationPropertyOfAxiom(factory.getOWLAnnotationProperty(subject),
                            factory.getOWLAnnotationProperty(object));
                }
            }
        } else if ( ANNOTATION_PREDICATES.contains(predicate) ) {
            axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(IRI.create(predicate)),
                    subject, object);
        }

        if ( axiom == null ) {
            IRI predicateIRI = IRI.create(predicate);
            if ( ontology.containsAnnotationPropertyInSignature(predicateIRI) ) {
                axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(predicateIRI), subject,
                        object);
            } else if ( ontology.containsObjectPropertyInSignature(predicateIRI)
                    && isClass(subject, mapping.getSubjectType()) && isClass(object, mapping.getObjectType()) ) {
                axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLObjectSomeValuesFrom(
                        factory.getOWLObjectProperty(predicateIRI), factory.getOWLClass(object)));
            } else {
                // If we still don't know, assume the predicate is an annotation property
                axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(predicateIRI), subject,
                        object);
            }
        }

        return axiom;
    }

    private EntityType getPropertyType(IRI entityId, EntityType explicit, boolean allowAnnotationProperty) {
        if ( explicit != null ) {
            return explicit;
        } else if ( ontology.containsObjectPropertyInSignature(entityId) ) {
            return EntityType.OWL_OBJECT_PROPERTY;
        } else if ( allowAnnotationProperty && ontology.containsAnnotationPropertyInSignature(entityId) ) {
            return EntityType.OWL_ANNOTATION_PROPERTY;
        } else if ( ontology.containsDataPropertyInSignature(entityId) ) {
            return EntityType.OWL_DATA_PROPERTY;
        } else {
            return null;
        }
    }

    private boolean isClass(IRI entityId, EntityType explicit) {
        if ( explicit != null ) {
            return explicit == EntityType.OWL_CLASS || explicit == EntityType.RDFS_CLASS
                    || explicit == EntityType.RDFS_RESOURCE;
        } else {
            return ontology.containsClassInSignature(entityId);
        }
    }
}
