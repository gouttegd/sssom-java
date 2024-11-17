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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

@TestInstance(Lifecycle.PER_CLASS)
public class SSSOMTOwlApplicationTest {

    private final static String UBERON_0000105 = "http://purl.obolibrary.org/obo/UBERON_0000105";
    private final static String UBERON_0014405 = "http://purl.obolibrary.org/obo/UBERON_0014405";
    private final static String UBERON_6000002 = "http://purl.obolibrary.org/obo/UBERON_6000002";

    private SSSOMTOwlApplication app;
    private List<String> arguments = new ArrayList<String>();
    private Map<String, String> keyedArguments = new HashMap<String, String>();

    public SSSOMTOwlApplicationTest() {
        try {
            InputStream input = new GZIPInputStream(
                    new FileInputStream(new File("src/test/resources/owl/uberon.ofn.gz")));
            OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(input);
            OWLReasonerFactory factory = new ElkReasonerFactory();
            app = new SSSOMTOwlApplication(ontology, factory);
            PrefixManager pfxMgr = new PrefixManager();
            pfxMgr.add("UBERON", "http://purl.obolibrary.org/obo/UBERON_");
            pfxMgr.add("ORGENT", "https://example.org/entities/");
            app.onInit(pfxMgr);
        } catch ( Exception e ) {
            Assertions.fail(e);
        }
    }

    @BeforeEach
    void reset() {
        arguments.clear();
        keyedArguments.clear();
    }

    @Test
    void testClassExists() {
        Assertions.assertTrue(app.classExists("http://purl.obolibrary.org/obo/UBERON_0001062"));
        Assertions.assertFalse(app.classExists("http://purl.obolibrary.org/obo/UBERON_doesnotexist"));
        Assertions.assertFalse(app.classExists("http://purl.obolibrary.org/obo/UBERON_0000032"));
    }

    @Test
    void testSubclassesOf() {
        Assertions.assertTrue(app.getSubClassesOf(UBERON_0000105).contains(UBERON_0014405));
        Assertions.assertFalse(app.getSubClassesOf(UBERON_0000105).contains(UBERON_6000002));
    }

    @Test
    void testGetSubjectLabel() {
        Mapping m = new Mapping();
        m.setSubjectId(UBERON_0000105);
        m.setSubjectLabel("mapping own label");

        Assertions.assertEquals("mapping own label", app.getFormatter().getTransformer("%subject_label").transform(m));

        m.setSubjectLabel(null);
        Assertions.assertEquals("life cycle stage", app.getFormatter().getTransformer("%subject_label").transform(m));

        m.setSubjectId("http://purl.obolibrary.org/UBERON_doesnotexist");
        Assertions.assertEquals("Unknown subject", app.getFormatter().getTransformer("%subject_label").transform(m));
    }

    @Test
    void testGetObjectLabel() {
        Mapping m = new Mapping();
        m.setObjectId(UBERON_0000105);
        m.setObjectLabel("mapping own label");

        Assertions.assertEquals("mapping own label", app.getFormatter().getTransformer("%object_label").transform(m));

        m.setObjectLabel(null);
        Assertions.assertEquals("life cycle stage", app.getFormatter().getTransformer("%object_label").transform(m));

        m.setObjectId("http://purl.obolibrary.org/UBERON_doesnotexist");
        Assertions.assertEquals("Unknown object", app.getFormatter().getTransformer("%object_label").transform(m));
    }

    @Test
    void testSetvarFunction() {
        arguments.add("MY_VAR");
        arguments.add("default value");

        Mapping m = new Mapping();

        try {
            app.onDirectiveAction("set_var", arguments, keyedArguments);
            Assertions.assertEquals("default value", app.getFormatter().format("%MY_VAR", m));
            Assertions.assertEquals("default value", app.getFormatter().format("%{MY_VAR}", m));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }

        arguments.clear();
        arguments.add("MY_VAR");
        arguments.add("another value");
        arguments.add("%subject_id is_a UBERON:0000105");
        try {
            app.onDirectiveAction("set_var", arguments, keyedArguments);
            m.setSubjectId(UBERON_0014405);
            Assertions.assertEquals("another value", app.getFormatter().format("%{MY_VAR}", m));

            m.setSubjectId(UBERON_6000002);
            Assertions.assertEquals("default value", app.getFormatter().format("%{MY_VAR}", m));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testDeclareFunction() {
        arguments.add("https://example.org/entities/0001");
        keyedArguments.put("type", "object_property");

        try {
            app.onDirectiveAction("declare", arguments, keyedArguments);
            Assertions.assertNotNull(app.getEntityChecker().getOWLObjectProperty("https://example.org/entities/0001"));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testExistsFunction() {
        arguments.add("%{subject_id}");

        try {
            IMappingFilter f = app.onFilter("exists", arguments, keyedArguments);
            Assertions.assertInstanceOf(SSSOMTExistsFunction.class, f);
            Assertions.assertTrue(f.filter(Mapping.builder().subjectId(UBERON_0000105).build()));
            Assertions.assertFalse(f.filter(Mapping.builder().subjectId("does_not_exist").build()));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testIsAFunction() {
        arguments.add("%{subject_id}");
        arguments.add(UBERON_0000105);

        try {
            IMappingFilter f = app.onFilter("is_a", arguments, keyedArguments);
            Assertions.assertInstanceOf(SSSOMTIsAFunction.class, f);
            Assertions.assertTrue(f.filter(Mapping.builder().subjectId(UBERON_0014405).build()));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testCheckSubjectExistenceFunction() {
        try {
            IMappingTransformer<Mapping> p = app.onPreprocessingAction("check_subject_existence", arguments,
                    keyedArguments);
            Assertions.assertInstanceOf(SSSOMTCheckSubjectExistenceFunction.class, p);
            Assertions.assertNotNull(p.transform(Mapping.builder().subjectId(UBERON_0000105).build()));
            Assertions.assertNull(p.transform(Mapping.builder().subjectId("does_not_exist").build()));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testCheckObjectExistenceFunction() {
        try {
            IMappingTransformer<Mapping> p = app.onPreprocessingAction("check_object_existence", arguments,
                    keyedArguments);
            Assertions.assertInstanceOf(SSSOMTCheckObjectExistenceFunction.class, p);
            Assertions.assertNotNull(p.transform(Mapping.builder().objectId(UBERON_0000105).build()));
            Assertions.assertNull(p.transform(Mapping.builder().objectId("does_not_exist").build()));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testDirectFunction() {
        try {
            IMappingTransformer<OWLAxiom> g = app.onGeneratingAction("direct", arguments, keyedArguments);

            Mapping m = new Mapping();
            m.setSubjectId(UBERON_0000105);
            m.setObjectId("https://example.org/entities/0001");
            m.setPredicateId(CommonPredicate.OWL_EQUIVALENT_CLASS.toString());

            OWLAxiom ax = g.transform(m);
            Assertions.assertTrue(ax.isOfType(AxiomType.EQUIVALENT_CLASSES));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testAnnotateFunction() {
        arguments.add("%{subject_id}");
        arguments.add("http://www.geneontology.org/formats/oboInOwl#hasDbXref");
        arguments.add("%{object_id|short}");

        try {
            IMappingTransformer<OWLAxiom> g = app.onGeneratingAction("annotate", arguments, keyedArguments);
            Mapping m = new Mapping();
            m.setSubjectId(UBERON_0000105);
            m.setObjectId("https://example.org/entities/0001");

            OWLAxiom ax = g.transform(m);
            Assertions.assertTrue(ax.isOfType(AxiomType.ANNOTATION_ASSERTION));
            OWLAnnotationAssertionAxiom aax = (OWLAnnotationAssertionAxiom) ax;
            Assertions.assertEquals("ORGENT:0001", aax.getValue().asLiteral().get().getLiteral());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testCreateAxiomFunction() {
        arguments.add("<%{subject_id}> SubClassOf: <%{object_id}>");

        try {
            IMappingTransformer<OWLAxiom> g = app.onGeneratingAction("create_axiom", arguments, keyedArguments);
            Mapping m = new Mapping();
            m.setSubjectId(UBERON_0000105);
            m.setObjectId("https://example.org/entities/0001");

            OWLAxiom ax = g.transform(m);
            Assertions.assertTrue(ax.isOfType(AxiomType.SUBCLASS_OF));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }
}
