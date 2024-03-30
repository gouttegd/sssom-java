/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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

import java.util.ArrayList;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OWLHelperTest {

    private static final String IRI_BASE = "https://example.org/";

    private OWLOntology ontology = null;

    @BeforeEach
    private void getTestOntology() {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = mgr.getOWLDataFactory();
        ontology = null;
        try {
            ontology = mgr.createOntology(IRI.create(IRI_BASE + "test.owl"));
        } catch ( OWLOntologyCreationException e ) {
            Assertions.fail(e);
            return;
        }

        OWLAnnotationProperty labelProp = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());

        IRI noLabel = IRI.create(IRI_BASE + "no_label");
        mgr.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(noLabel)));

        IRI neutralLabelOnly = IRI.create(IRI_BASE + "neutral_label_only");
        mgr.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(neutralLabelOnly)));
        mgr.addAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(labelProp, neutralLabelOnly,
                factory.getOWLLiteral("neutral label")));

        IRI englishLabelOnly = IRI.create(IRI_BASE + "english_label_only");
        mgr.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(englishLabelOnly)));
        mgr.addAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(labelProp, englishLabelOnly,
                factory.getOWLLiteral("english label", "en")));

        IRI neutralAndEnglishLabel = IRI.create(IRI_BASE + "neutral_and_english_labels");
        mgr.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(neutralAndEnglishLabel)));
        mgr.addAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(labelProp, neutralAndEnglishLabel,
                factory.getOWLLiteral("neutral label")));
        mgr.addAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(labelProp, neutralAndEnglishLabel,
                factory.getOWLLiteral("english label", "en")));
    }

    @Test
    void testGetLabelWithNoLanguage() {
        testGetLabel("neutral_label_only", null, false, "neutral label");
        testGetLabel("english_label_only", null, false, "english label");
        testGetLabel("neutral_and_english_labels", null, false, "neutral label");
    }

    @Test
    void testGetLabelWithEmptyLanguageLax() {
        testGetLabel("neutral_label_only", "", false, "neutral label");
        testGetLabel("english_label_only", "", false, null);
        testGetLabel("neutral_and_english_labels", "", false, "neutral label");
    }

    @Test
    void testGetLabelWithEmptyLanguageStrict() {
        testGetLabel("neutral_label_only", "", true, "neutral label");
        testGetLabel("english_label_only", "", true, null);
        testGetLabel("neutral_and_english_labels", "", true, "neutral label");
    }

    @Test
    void testGetEnglishLabelLax() {
        testGetLabel("neutral_label_only", "en", false, "neutral label");
        testGetLabel("english_label_only", "en", false, "english label");
        testGetLabel("neutral_and_english_labels", "en", true, "english label");
    }

    @Test
    void testGetEnglishLabelStrict() {
        testGetLabel("neutral_label_only", "en", true, null);
        testGetLabel("english_label_only", "en", true, "english label");
        testGetLabel("neutral_and_english_labels", "en", true, "english label");
    }

    @Test
    void testGetFrenchLabelLax() {
        testGetLabel("neutral_label_only", "fr", false, "neutral label");
        testGetLabel("english_label_only", "fr", false, null);
        testGetLabel("neutral_and_english_labels", "fr", false, "neutral label");
    }

    @Test
    void testGetFrenchLabelStrict() {
        testGetLabel("neutral_label_only", "fr", true, null);
        testGetLabel("english_label_only", "fr", true, null);
        testGetLabel("neutral_and_english_labels", "fr", true, null);
    }

    @Test
    void testUpdateMappingSet() {
        MappingSet ms = MappingSet.builder().mappings(new ArrayList<Mapping>()).build();
        Mapping m = Mapping.builder().subjectId(IRI_BASE + "neutral_label_only")
                .objectId(IRI_BASE + "english_label_only").build();
        ms.getMappings().add(m);

        OWLHelper.updateMappingSet(ms, ontology, null, false);
        Assertions.assertEquals(m.getSubjectLabel(), "neutral label");
        Assertions.assertEquals(m.getObjectLabel(), "english label");
        Assertions.assertEquals(m.getSubjectSource(), IRI_BASE + "test.owl");
        Assertions.assertEquals(m.getObjectSource(), IRI_BASE + "test.owl");
    }

    private void testGetLabel(String id, String language, boolean strict, String expected) {
        String actual = OWLHelper.getLabel(ontology, IRI.create(IRI_BASE + id), language, strict);
        if ( expected != null ) {
            Assertions.assertEquals(expected, actual);
        } else {
            Assertions.assertNull(actual);
        }
    }
}
