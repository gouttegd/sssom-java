/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.owl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@TestInstance(Lifecycle.PER_CLASS)
public class DirectAxiomGeneratorTest {

    private final static String CLASS_1 = "http://purl.obolibrary.org/obo/FBdv_00004450";
    private final static String CLASS_2 = "http://purl.obolibrary.org/obo/FBdv_00004886";
    private final static String CLASS_X = "http://purl.obolibrary.org/obo/FBdv_99999999";
    private final static String OBJ_PROPERTY_1 = "http://purl.obolibrary.org/obo/RO_0002012";
    private final static String OBJ_PROPERTY_2 = "http://purl.obolibrary.org/obo/RO_0002087";
    private final static String OBJ_PROPERTY_X = "http://purl.obolibrary.org/obo/RO_9999999";
    private final static String ANN_PROPERTY_1 = "http://purl.obolibrary.org/obo/IAO_0000115";
    private final static String ANN_PROPERTY_2 = "http://purl.obolibrary.org/obo/IAO_0000231";
    private final static String ANN_PROPERTY_X = "http://purl.obolibrary.org/obo/IAO_9999999";

    private DirectAxiomGenerator generator;

    public DirectAxiomGeneratorTest() {
        try {
            InputStream input = new FileInputStream(new File("src/test/resources/owl/fbdv.ofn"));
            OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(input);
            generator = new DirectAxiomGenerator(ontology);
        } catch ( FileNotFoundException | OWLOntologyCreationException e ) {
            Assertions.fail(e);
        }
    }

    // An explicitly specified predicate type always takes precedence.
    @Test
    void testExplicitPredicateType() {
        Mapping m = Mapping.builder().subjectId(CLASS_1).objectId(CLASS_2).build();
        m.setPredicateId(CommonPredicate.SKOS_EXACT_MATCH.toString());

        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);

        m.setPredicateType(EntityType.OWL_OBJECT_PROPERTY);
        assertAxiomType(AxiomType.SUBCLASS_OF, m);

        m.setPredicateId(CommonPredicate.OWL_EQUIVALENT_CLASS.toString());
        m.setPredicateType(EntityType.OWL_ANNOTATION_PROPERTY);
        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);
    }

    // If the predicate is a class construct (owl:equivalentClass, rdfs:subClassOf),
    // this takes precedence over subject/object types.
    @Test
    void testPredicateIsOWLClassConstruct() {
        Mapping m = Mapping.builder().subjectId(OBJ_PROPERTY_1).objectId(OBJ_PROPERTY_2).build();
        m.setPredicateId(CommonPredicate.OWL_EQUIVALENT_CLASS.toString());

        // OBJ_PROPERTY_1 and OBJ_PROPERTY_2 are declared as object properties, but the
        // generated axiom is still a EquivalentClasses axiom.
        assertAxiomType(AxiomType.EQUIVALENT_CLASSES, m);

        // Even when subject/object types are explicitly specified in the mapping.
        m.setSubjectType(EntityType.OWL_OBJECT_PROPERTY);
        m.setObjectType(EntityType.OWL_OBJECT_PROPERTY);
        assertAxiomType(AxiomType.EQUIVALENT_CLASSES, m);

        // Likewise with rdfs:subClassOf
        m.setPredicateId(CommonPredicate.RDFS_SUBCLASS_OF.toString());
        assertAxiomType(AxiomType.SUBCLASS_OF, m);
    }

    // With no explicitly specified predicate type and no builtin knowledge of the
    // predicate, if the predicate is declared as the ontology as an annotation
    // property, this should yield an annotation axiom.
    @Test
    void testImplicitAnnotationPredicate() {
        Mapping m = Mapping.builder().subjectId(CLASS_1).objectId(CLASS_2).build();
        m.setPredicateId(ANN_PROPERTY_1);

        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);
    }

    // Likewise, if the predicate is declared as an object property, this should
    // yield an existential restriction axiom.
    @Test
    void testImplicitObjectPropertyPredicate() {
        Mapping m = Mapping.builder().subjectId(CLASS_1).objectId(CLASS_2).build();
        m.setPredicateId(OBJ_PROPERTY_1);

        assertAxiomType(AxiomType.SUBCLASS_OF, m);
    }

    // But not if we know (from the ontology or from the mapping) that either the
    // subject or the object is not a class.
    @Test
    void testImplicitObjectPropertyPredicateWithNoClasses() {
        Mapping m = Mapping.builder().subjectId(CLASS_1).objectId(OBJ_PROPERTY_1).build();
        m.setPredicateId(OBJ_PROPERTY_2);

        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);

        // Even if the object is declared as a class, an explicit object type saying it
        // is an object property takes precedence.
        m.setObjectId(CLASS_2);
        m.setObjectType(EntityType.OWL_OBJECT_PROPERTY);
        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);
    }

    // With no explicitly specified predicate type, no builtin knowledge, and no
    // knowledge coming from the ontology, this should yield by default an
    // annotation axiom.
    @Test
    void testAssumedAnnotationPredicate() {
        Mapping m = Mapping.builder().subjectId(CLASS_1).objectId(CLASS_2).build();
        m.setPredicateId(ANN_PROPERTY_X); // Unknown in the ontology

        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);
    }

    @Test
    void testPredicateIsEquivalentProperty() {
        Mapping m = Mapping.builder().subjectId(OBJ_PROPERTY_1).objectId(OBJ_PROPERTY_2).build();
        m.setPredicateId(CommonPredicate.OWL_EQUIVALENT_PROPERTY.toString());

        // Mappings between 2 declared OP -> EquivalentProperties axiom
        assertAxiomType(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, m);

        // Mappings between 1 declared OP and 1 declared class -> cannot yield an
        // EquivalentObjectProperties axiom, fallback to Annotation
        m.setSubjectId(CLASS_1);
        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);

        // Explicitly specified subject type takes precedence over what is declared in
        // the ontology
        m.setSubjectType(EntityType.OWL_OBJECT_PROPERTY);
        assertAxiomType(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, m);

        // Mappings between 2 undeclared entities -> cannot yield an
        // EquivalentObjectProperties axiom, fallback to Annotation
        m = Mapping.builder().subjectId(CLASS_X).objectId(OBJ_PROPERTY_X).build();
        m.setPredicateId(CommonPredicate.OWL_EQUIVALENT_PROPERTY.toString());
        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);

        // Explicitly specified data property types -> EquivalentDataProperties axiom
        m.setSubjectType(EntityType.OWL_DATA_PROPERTY);
        m.setObjectType(EntityType.OWL_DATA_PROPERTY);
        assertAxiomType(AxiomType.EQUIVALENT_DATA_PROPERTIES, m);
    }

    @Test
    void testPredicateIsSubPropertyOf() {
        Mapping m = Mapping.builder().subjectId(ANN_PROPERTY_1).objectId(ANN_PROPERTY_2).build();
        m.setPredicateId(CommonPredicate.RDFS_SUBPROPERTY_OF.toString());

        // Mapping between 2 declared AP -> SubAnnotationProperty axiom
        assertAxiomType(AxiomType.SUB_ANNOTATION_PROPERTY_OF, m);

        // Mappings between 1 declared AP and a declared OP -> cannot yield any kind of
        // Sub*Property axiom, fallback to Annotation
        m.setObjectId(OBJ_PROPERTY_2);
        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);

        // Explicitly specified subject type takes precedence over what is declared
        m.setSubjectType(EntityType.OWL_OBJECT_PROPERTY);
        assertAxiomType(AxiomType.SUB_OBJECT_PROPERTY, m);

        // Mapping with an undeclared entity -> cannot yield any kind of Sub*Property
        // axiom, fallback to Annotation
        m.setObjectId(OBJ_PROPERTY_X);
        assertAxiomType(AxiomType.ANNOTATION_ASSERTION, m);

        // Explicitly specified data property types -> SubDataPropertyOf axiom
        m.setSubjectType(EntityType.OWL_DATA_PROPERTY);
        m.setObjectType(EntityType.OWL_DATA_PROPERTY);
        assertAxiomType(AxiomType.SUB_DATA_PROPERTY, m);
    }

    private void assertAxiomType(AxiomType<?> expected, Mapping mapping) {
        AxiomType<?> actual = generator.transform(mapping).getAxiomType();
        Assertions.assertEquals(expected, actual);
    }
}
