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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.owl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

@TestInstance(Lifecycle.PER_CLASS)
public class XrefExtractorTest {

    private static final String TREAT_XREFS_AS_GENUS_DIFFERENTIA = "http://www.geneontology.org/formats/oboInOwl#treat-xrefs-as-genus-differentia";

    private OWLOntology ontology;

    public XrefExtractorTest() {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        try {
            InputStream input = new FileInputStream(new File("src/test/resources/owl/fbdv.ofn"));
            ontology = mgr.loadOntologyFromOntologyDocument(input);
        } catch ( Exception e ) {
            Assertions.fail(e);
        }

        // Add a treat-xrefs-as... annotation
        OWLDataFactory fac = mgr.getOWLDataFactory();
        OWLAnnotation annot = fac.getOWLAnnotation(
                fac.getOWLAnnotationProperty(IRI.create(TREAT_XREFS_AS_GENUS_DIFFERENTIA)),
                fac.getOWLLiteral("UBERON part_of NCBITaxon:7227"));
        AddOntologyAnnotation change = new AddOntologyAnnotation(ontology, annot);
        mgr.applyChange(change);
    }

    @Test
    void testExtractDefaultXrefs() {
        XrefExtractor extractor = new XrefExtractor();
        Map<String, String> prefixMap = new HashMap<String, String>();
        prefixMap.put("UBERON", "http://purl.obolibrary.org/obo/UBERON_");
        extractor.setPrefixMap(prefixMap);

        extractor.fillPrefixToPredicateMap(ontology);

        MappingSet ms = extractor.extract(ontology);
        for ( Mapping m : ms.getMappings() ) {
            Assertions
                    .assertTrue(m.getPredicateId().equals(CommonPredicate.SEMAPV_CROSS_SPECIES_EXACT_MATCH.toString()));
            Assertions.assertTrue(m.getSubjectId().startsWith("http://purl.obolibrary.org/obo/FBdv_"));
            Assertions.assertTrue(m.getObjectId().startsWith("http://purl.obolibrary.org/obo/UBERON_"));
        }
    }

    @Test
    void testIgnoreUnknownPrefixes() {
        XrefExtractor extractor = new XrefExtractor();
        extractor.fillPrefixToPredicateMap(ontology);

        MappingSet ms = extractor.extract(ontology);

        Assertions.assertTrue(ms.getMappings().isEmpty());
    }

    @Test
    void testExtractAll() {
        XrefExtractor extractor = new XrefExtractor();
        Map<String, String> prefixMap = new HashMap<String, String>();
        prefixMap.put("UBERON", "http://purl.obolibrary.org/obo/UBERON_");
        prefixMap.put("GO", "http://purl.obolibrary.org/obo/GO_");
        extractor.setPrefixMap(prefixMap);

        MappingSet ms = extractor.extract(ontology, false, true);
        Assertions.assertEquals(7, ms.getMappings().size());
        for ( Mapping m : ms.getMappings() ) {
            Assertions.assertTrue(m.getPredicateId().equals("http://www.geneontology.org/formats/oboInOwl#hasDbXref"));
            Assertions.assertTrue(m.getSubjectId().startsWith("http://purl.obolibrary.org/obo/FBdv_"));
        }
    }
}
