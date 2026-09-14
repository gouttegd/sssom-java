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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.owl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class AnnotatedAxiomGeneratorTest {

    private static final String IRI_BASE = "https://example.org/";
    private static final String SSSOM_BASE = "https://w3id.org/sssom/";

    private OWLOntology ontology;
    private OWLDataFactory factory;

    public AnnotatedAxiomGeneratorTest() {
        try {
            ontology = OWLManager.createOWLOntologyManager().createOntology();
            factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        } catch (OWLOntologyCreationException e) {
            Assertions.fail(e);
        }
    }

    @Test
    void testRenderExtensionSlots() {
        AnnotatedAxiomGenerator gen = new AnnotatedAxiomGenerator(ontology);
        OWLAxiom axiom = gen.transform(getTestMapping());

        Assertions.assertNotNull(getAnnotation(axiom, IRI_BASE + "extensions/foo"));

        gen.annotateWithExtensions(false);
        axiom = gen.transform(getTestMapping());
        Assertions.assertNull(getAnnotation(axiom, IRI_BASE + "extensions/foo"));
    }

    @Test
    void testRenderMetadataOnly() {
        AnnotatedAxiomGenerator gen = new AnnotatedAxiomGenerator(ontology, true);
        OWLAxiom axiom = gen.transform(getTestMapping());

        Assertions.assertNull(getAnnotation(axiom, "http://www.w3.org/2002/07/owl#annotatedSource"));
        Assertions.assertNull(getAnnotation(axiom, "http://www.w3.org/2002/07/owl#annotatedTarget"));
        Assertions.assertNull(getAnnotation(axiom, "http://www.w3.org/2002/07/owl#annotatedProperty"));
        Assertions.assertNotNull(getAnnotation(axiom, SSSOM_BASE + "confidence"));
    }

    @Test
    void testRenderSelectedSlots() {
        AnnotatedAxiomGenerator gen = new AnnotatedAxiomGenerator(ontology,
                SlotHelper.getMappingSlotList("subject_id"));
        OWLAxiom axiom = gen.transform(getTestMapping());

        Assertions.assertNotNull(getAnnotation(axiom, "http://www.w3.org/2002/07/owl#annotatedSource"));
        Assertions.assertNull(getAnnotation(axiom, "http://www.w3.org/2002/07/owl#annotatedTarget"));
    }

    private Mapping getTestMapping() {
        Mapping m = Mapping.builder().subjectId(IRI_BASE + "entities/0001")
                .objectId(IRI_BASE + "entities/0002")
                .predicateId(IRI_BASE + "predicate/0001")
                .subjectType(EntityType.OWL_CLASS)
                .authorId(new ArrayList<>())
                .confidence(0.8)
                .mappingDate(LocalDate.of(2026, 9, 14))
                .comment("Some comment.")
                .license(IRI_BASE + "license")
                .extensions(new HashMap<>())
                .build();

        m.getAuthorId().add(IRI_BASE + "people/0001");
        m.getAuthorId().add(IRI_BASE + "people/0002");
        m.getExtensions().put(IRI_BASE + "extensions/foo", new ExtensionValue("foo value"));

        return m;
    }

    private OWLAnnotationValue getAnnotation(OWLAxiom axiom, String property) {
        for ( OWLAnnotation annot : axiom.getAnnotations(factory.getOWLAnnotationProperty(IRI.create(property))) ) {
            return annot.getValue();
        }
        return null;
    }
}
