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

package org.incenp.obofoundry.sssom.transform;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SSSOMTransformApplicationTest {

    SSSOMTransformApplication<Void> application;
    List<String> arguments;
    Map<String, String> keyedArguments;

    SSSOMTransformApplicationTest() {
        application = new SSSOMTransformApplication<Void>();
        application.onInit(new PrefixManager());

        arguments = new ArrayList<String>();
        keyedArguments = new HashMap<String, String>();
    }

    @Test
    void testUnknownFunctions() {
        try {
            Assertions.assertFalse(application.onDirectiveAction("unknown", arguments, keyedArguments));
            Assertions.assertNull(application.onFilter("unknown", arguments, keyedArguments));
            Assertions.assertNull(application.onCallback("unknown", arguments, keyedArguments));
            Assertions.assertNull(application.onPreprocessingAction("unknown", arguments, keyedArguments));
            Assertions.assertNull(application.onGeneratingAction("unknown", arguments, keyedArguments));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testSetvarDirective() {
        arguments.add("MY_VAR");
        arguments.add("Default value");

        try {
            Assertions.assertTrue(application.onDirectiveAction("set_var", arguments, keyedArguments));
            Assertions.assertTrue(application.getVariableManager().hasVariable("MY_VAR"));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }

        arguments.add("NEW_VAR");
        Assertions.assertThrows(SSSOMTransformError.class,
                () -> application.onDirectiveAction("set_var", arguments, keyedArguments),
                "Invalid call for function set_var");
    }

    @Test
    void testSetvarCallback() {
        arguments.add("MY_VAR");
        arguments.add("Non-default value");

        try {
            Object o = application.onCallback("set_var", arguments, keyedArguments);
            Assertions.assertInstanceOf(IMappingProcessorCallback.class, o);
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }

        arguments.clear();
        Assertions.assertThrows(SSSOMTransformError.class,
                () -> application.onCallback("set_var", arguments, keyedArguments),
                "Invalid call for function set_var");
    }

    @Test
    void testStopPreprocessor() {
        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("stop", arguments, keyedArguments);
            Assertions.assertInstanceOf(SSSOMTStopFunction.class, o);
            Assertions.assertNull(o.transform(new Mapping()));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testInvertPreprocessor() {
        Mapping m = new Mapping();
        m.setSubjectId("https://example.org/entities/0001");
        m.setObjectId("https://example.com/entities/0011");
        m.setPredicateId("http://www.w3.org/2004/02/skos/core#exactMatch");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("invert", arguments, keyedArguments);
            Assertions.assertInstanceOf(SSSOMTInvertFunction.class, o);

            Mapping inverted = o.transform(m);
            Assertions.assertEquals(m.getSubjectId(), inverted.getObjectId());
            Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#exactMatch", inverted.getPredicateId());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }

        m.setAuthorId(new ArrayList<String>());
        m.getAuthorId().add("http://www.w3.org/2004/02/skos/core#closeMatch");
        arguments.add("%{author_id|list_item(1)}");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("invert", arguments, keyedArguments);
            Assertions.assertInstanceOf(SSSOMTInvertFunction.class, o);

            Mapping inverted = o.transform(m);
            Assertions.assertEquals(m.getSubjectId(), inverted.getObjectId());
            Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#closeMatch", inverted.getPredicateId());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testAssignPreprocessor() {
        arguments.add("object_label");
        arguments.add("New label");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("assign", arguments, keyedArguments);
            Assertions.assertInstanceOf(MappingEditor.class, o);

            Mapping m = o.transform(new Mapping());
            Assertions.assertEquals("New label", m.getObjectLabel());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }

        arguments.clear();
        arguments.add("invalid");
        arguments.add("value");

        Assertions.assertThrows(SSSOMTransformError.class,
                () -> application.onPreprocessingAction("assign", arguments, keyedArguments),
                "Invalid argument for function assign: Invalid slot name: invalid");

        arguments.clear();
        arguments.add("no value");

        Assertions.assertThrows(SSSOMTransformError.class,
                () -> application.onPreprocessingAction("assign", arguments, keyedArguments),
                "Invalid call for function assign");

        arguments.clear();
        arguments.add("subject_id");
        arguments.add("");

        Assertions.assertThrows(SSSOMTransformError.class,
                () -> application.onPreprocessingAction("assign", arguments, keyedArguments),
                "Invalid argument for function assign: Cannot set slot \"subject_id\" to nothing");

        arguments.clear();
        arguments.add("author_label");
        arguments.add("First author|Second author");
        arguments.add("mapping_date");
        arguments.add("2024-11-01");
        arguments.add("confidence");
        arguments.add("0.5");
        arguments.add("subject_type");
        arguments.add("owl class");
        arguments.add("mapping_cardinality");
        arguments.add("1:1");
        arguments.add("predicate_modifier");
        arguments.add("Not");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("assign", arguments, keyedArguments);

            Mapping m = o.transform(new Mapping());
            Assertions.assertEquals(2, m.getAuthorLabel().size());
            Assertions.assertEquals("First author", m.getAuthorLabel().get(0));
            Assertions.assertEquals("Second author", m.getAuthorLabel().get(1));

            Assertions.assertEquals(LocalDate.of(2024, 11, 1), m.getMappingDate());
            Assertions.assertEquals(0.5, m.getConfidence());
            Assertions.assertEquals(EntityType.OWL_CLASS, m.getSubjectType());
            Assertions.assertEquals(MappingCardinality.ONE_TO_ONE, m.getMappingCardinality());
            Assertions.assertEquals(PredicateModifier.NOT, m.getPredicateModifier());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testDelayedAssign() {
        arguments.add("object_label");
        arguments.add("same as %{subject_label}");
        arguments.add("comment");
        arguments.add("%{subject_id|short}");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("assign", arguments, keyedArguments);

            Mapping m = Mapping.builder().subjectLabel("subject").subjectId("https://example.org/entities/0001")
                    .build();
            application.getPrefixManager().add("ORGENT", "https://example.org/entities/");

            Mapping edited = o.transform(m);
            Assertions.assertEquals("same as subject", edited.getObjectLabel());
            Assertions.assertEquals("ORGENT:0001", edited.getComment());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testReplacePreprocessor() {
        arguments.add("object_label");
        arguments.add("Old");
        arguments.add("New");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("replace", arguments, keyedArguments);
            Assertions.assertInstanceOf(MappingEditor.class, o);

            Mapping m = o.transform(Mapping.builder().objectLabel("Old label").build());
            Assertions.assertEquals("New label", m.getObjectLabel());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }

        arguments.clear();
        arguments.add("author_label");
        arguments.add("Old");
        arguments.add("New");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("replace", arguments, keyedArguments);

            List<String> authorLabels = new ArrayList<String>();
            authorLabels.add("A. U. Thor");
            authorLabels.add("Old author");
            Mapping m = o.transform(Mapping.builder().authorLabel(authorLabels).build());
            Assertions.assertEquals("A. U. Thor", m.getAuthorLabel().get(0));
            Assertions.assertEquals("New author", m.getAuthorLabel().get(1));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testEditPreprocessor() {
        arguments.add("object_label=New label");

        try {
            IMappingTransformer<Mapping> o = application.onPreprocessingAction("edit", arguments, keyedArguments);
            Assertions.assertInstanceOf(MappingEditor.class, o);

            Mapping m = o.transform(new Mapping());
            Assertions.assertEquals("New label", m.getObjectLabel());
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }
}
