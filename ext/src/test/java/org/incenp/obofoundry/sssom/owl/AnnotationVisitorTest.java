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
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class AnnotationVisitorTest {

    private static final String IRI_BASE = "https://example.org/";
    private static final String SSSOM_BASE = "https://w3id.org/sssom/";

    private OWLDataFactory factory = OWLManager.getOWLDataFactory();

    @Test
    void testNoResetAfterAnnotate() {
        AnnotationVisitor<Mapping> visitor = new AnnotationVisitor<>(factory);
        Mapping mapping = getTestMapping();

        SlotHelper.getMappingHelper().visitSlots(mapping, visitor);
        visitor.annotate(getTestAxiom(), false);

        mapping.setComment(null);
        SlotHelper.getMappingHelper().visitSlots(mapping, visitor);
        OWLAxiom secondAxiom = visitor.annotate(getTestAxiom(), false);
        Assertions.assertNotNull(getAnnotation(secondAxiom, SSSOM_BASE + "comment"));

        visitor.reset();
        SlotHelper.getMappingHelper().visitSlots(mapping, visitor);
        secondAxiom = visitor.annotate(getTestAxiom(), false);
        Assertions.assertNull(getAnnotation(secondAxiom, SSSOM_BASE + "comment"));
    }

    @Test
    void testResetAfterAnnotate() {
        AnnotationVisitor<Mapping> visitor = new AnnotationVisitor<>(factory);
        Mapping mapping = getTestMapping();

        SlotHelper.getMappingHelper().visitSlots(mapping, visitor);
        visitor.annotate(getTestAxiom(), true);

        mapping.setComment(null);
        SlotHelper.getMappingHelper().visitSlots(mapping, visitor);
        OWLAxiom secondAxiom = visitor.annotate(getTestAxiom(), false);
        Assertions.assertNull(getAnnotation(secondAxiom, SSSOM_BASE + "comment"));
    }

    @Test
    void testSlotToIRITranslation() {
        AnnotationVisitor<Mapping> visitor = new AnnotationVisitor<>(factory);
        OWLAxiom annotatedAxiom = getAnnotatedAxiom(getTestMapping(), visitor);

        Assertions.assertNotNull(getAnnotation(annotatedAxiom, SSSOM_BASE + "author_id"));
        Assertions.assertNull(getAnnotation(annotatedAxiom, "http://purl.org/pav/authoredBy"));

        visitor = new AnnotationVisitor<>(factory, new StandardMapMetadataTransformer<Mapping>());
        annotatedAxiom = getAnnotatedAxiom(getTestMapping(), visitor);
        Assertions.assertNull(getAnnotation(annotatedAxiom, SSSOM_BASE + "author_id"));
        Assertions.assertNotNull(getAnnotation(annotatedAxiom, "http://purl.org/pav/authoredBy"));
    }

    @Test
    void testRenderingOfURISlots() {
        AnnotationVisitor<Mapping> visitor = new AnnotationVisitor<>(factory);
        OWLAxiom annotatedAxiom = getAnnotatedAxiom(getTestMapping(), visitor);

        OWLAnnotationValue value = getAnnotation(annotatedAxiom, SSSOM_BASE + "license");
        Assertions.assertNotNull(value);
        Assertions.assertEquals(IRI_BASE + "license", value.asLiteral().get().getLiteral());
        Assertions.assertEquals(OWL2Datatype.XSD_ANY_URI.getIRI().toString(),
                value.asLiteral().get().getDatatype().toStringID());

        visitor.renderURISlotsAsResources(true);
        annotatedAxiom = getAnnotatedAxiom(getTestMapping(), visitor);
        value = getAnnotation(annotatedAxiom, SSSOM_BASE + "license");
        Assertions.assertNotNull(value);
        Assertions.assertTrue(value.isIRI());
    }

    @Test
    void testAnnotateWithExtensionSlots() {
        AnnotationVisitor<Mapping> visitor = new AnnotationVisitor<>(factory);
        OWLAxiom annotatedAxiom = getAnnotatedAxiom(getTestMapping(), visitor);

        OWLAnnotationValue value = getAnnotation(annotatedAxiom, IRI_BASE + "extensions/foo");
        Assertions.assertNotNull(value);
        Assertions.assertEquals("foo value", value.asLiteral().get().getLiteral());

        visitor.renderExtensionSlots(false);
        annotatedAxiom = getAnnotatedAxiom(getTestMapping(), visitor);
        value = getAnnotation(annotatedAxiom, IRI_BASE + "extensions/foo");
        Assertions.assertNull(value);
    }

    private OWLAxiom getTestAxiom() {
        return factory.getOWLDeclarationAxiom(factory.getOWLClass(IRI.create(IRI_BASE + "entities/0001")));
    }

    private Mapping getTestMapping() {
        Mapping m = Mapping.builder().subjectId(IRI_BASE + "entities/0001")
                .objectId(IRI_BASE + "entities/0002")
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

    private OWLAxiom getAnnotatedAxiom(Mapping mapping, AnnotationVisitor<Mapping> visitor) {
        SlotHelper.getMappingHelper().visitSlots(mapping, visitor);
        return visitor.annotate(getTestAxiom(), true);
    }

    private OWLAnnotationValue getAnnotation(OWLAxiom axiom, String property) {
        for ( OWLAnnotation annot : axiom.getAnnotations(factory.getOWLAnnotationProperty(IRI.create(property))) ) {
            return annot.getValue();
        }
        return null;
    }
}
