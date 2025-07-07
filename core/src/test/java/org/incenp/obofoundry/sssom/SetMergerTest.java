/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SetMergerTest {

    @Test
    public void testMergingListValues() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        // Merging a dataset that contains values into a dataset that does not
        ms1.setCreatorId(null);
        ms2.setCreatorId(new ArrayList<String>());
        ms2.getCreatorId().add("https://example.org/people/0000-0000-0001-5678");
        ms2.getCreatorId().add("https://example.org/people/0000-0000-0001-9876");
        merger.merge(ms1, ms2);
        Assertions.assertArrayEquals(new String[] { "https://example.org/people/0000-0000-0001-5678",
                "https://example.org/people/0000-0000-0001-9876" }, ms1.getCreatorId().toArray());

        // Merging datasets that both contain values; check that duplicates are pruned
        ms1.setCreatorId(new ArrayList<String>());
        ms1.getCreatorId().add("https://example.org/people/0000-0000-0001-1234");
        ms1.getCreatorId().add("https://example.org/people/0000-0000-0001-5678");
        ms2.setCreatorId(new ArrayList<String>());
        ms2.getCreatorId().add("https://example.org/people/0000-0000-0001-5678");
        ms2.getCreatorId().add("https://example.org/people/0000-0000-0001-9876");
        merger.merge(ms1, ms2);
        Assertions.assertArrayEquals(new String[] { "https://example.org/people/0000-0000-0001-1234",
                "https://example.org/people/0000-0000-0001-5678", "https://example.org/people/0000-0000-0001-9876" },
                ms1.getCreatorId().toArray());
    }

    @Test
    public void testMergingMapValues() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        // Merging a dataset that contains values into a dataset that does not
        ms1.setCurieMap(null);
        merger.merge(ms1, ms2);
        Assertions.assertEquals("https://example.org/entities/", ms1.getCurieMap().get("ORGENT"));
        Assertions.assertEquals("https://example.com/entities/", ms1.getCurieMap().get("COMENT"));

        // Merging datasets that contains conflicting values; check that values from the
        // source set take precedence
        ms2.getCurieMap().put("COMENT", "https://example.net/entities/");
        merger.merge(ms1, ms2);
        Assertions.assertEquals("https://example.org/entities/", ms1.getCurieMap().get("ORGENT"));
        Assertions.assertEquals("https://example.net/entities/", ms1.getCurieMap().get("COMENT"));
    }

    @Test
    public void testMergingExtensionValues() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        // Merging a dataset that contains values into a dataset that does not
        ms2.setExtensions(new HashMap<String, ExtensionValue>());
        ms2.getExtensions().put("https://example.org/properties/fooProperty", new ExtensionValue("Foo A"));
        merger.merge(ms1, ms2);
        Assertions.assertEquals("Foo A",
                ms1.getExtensions().get("https://example.org/properties/fooProperty").asString());

        // Merging datasets that both contain values; in case of conflicts, values from
        // the source must take precedence
        ms1.getExtensions().put("https://example.org/properties/barProperty", new ExtensionValue(111));
        ms2.getExtensions().put("https://example.org/properties/fooProperty", new ExtensionValue("Foo B"));
        ms2.getExtensions().put("https://example.org/properties/bazProperty", new ExtensionValue("Baz A"));
        merger.merge(ms1, ms2);
        Assertions.assertEquals("Foo B",
                ms1.getExtensions().get("https://example.org/properties/fooProperty").asString());
        Assertions.assertEquals(111, ms1.getExtensions().get("https://example.org/properties/barProperty").asInteger());
        Assertions.assertEquals("Baz A",
                ms1.getExtensions().get("https://example.org/properties/bazProperty").asString());
    }

    @Test
    public void testMergingExtensionDefinitions() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        // Merging a dataset with definitions into a dataset that does not
        ms2.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms2.getExtensionDefinitions()
                .add(new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));
        merger.merge(ms1, ms2);
        containsExtensionDefinition(ms1.getExtensionDefinitions(),
                new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));

        // Merging datasets that both contain definitions (without conflicts)
        ms2.getExtensionDefinitions().add(new ExtensionDefinition("ext_bar",
                "https://example.org/properties/barProperty", "http://www.w3.org/2001/XMLSchema#integer"));
        merger.merge(ms1, ms2);
        Assertions.assertEquals(2, ms1.getExtensionDefinitions().size());
        containsExtensionDefinition(ms1.getExtensionDefinitions(),
                new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));
        containsExtensionDefinition(ms1.getExtensionDefinitions(), new ExtensionDefinition("ext_bar",
                "https://example.org/properties/barProperty", "http://www.w3.org/2001/XMLSchema#integer"));
    }

    @Test
    public void testMergingExtensionDefinitionsWithSlotNameClash() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        ms1.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms1.getExtensionDefinitions()
                .add(new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));

        // Set 2 contains a definition with the same slot name but a different property
        ms2.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms2.getExtensionDefinitions()
                .add(new ExtensionDefinition("ext_foo", "https://example.com/properties/fooProperty"));

        merger.merge(ms1, ms2);
        Assertions.assertEquals(2, ms1.getExtensionDefinitions().size());
        containsExtensionDefinition(ms1.getExtensionDefinitions(),
                new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));
        containsExtensionDefinition(ms1.getExtensionDefinitions(),
                new ExtensionDefinition("ext_foo_2", "https://example.com/properties/fooProperty"));
    }

    @Test
    public void testMergingExtensionDefinitionsWithPropertyClash() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        ms1.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms1.getExtensionDefinitions()
                .add(new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));

        // Set 2 contains a definition with the same property but a different slot name;
        // the slot name from the second set should take precedence
        ms2.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms2.getExtensionDefinitions().add(new ExtensionDefinition("foo", "https://example.org/properties/fooProperty"));

        merger.merge(ms1, ms2);
        Assertions.assertEquals(1, ms1.getExtensionDefinitions().size());
        containsExtensionDefinition(ms1.getExtensionDefinitions(),
                new ExtensionDefinition("foo", "https://example.org/properties/fooProperty"));
    }

    @Test
    public void testMergingExtensionDefinitionsWithTypeClash() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();
        SetMerger merger = new SetMerger();

        ms1.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms1.getExtensionDefinitions().add(new ExtensionDefinition("ext_foo",
                "https://example.org/properties/fooProperty", "http://www.w3.org/2001/XMLSchema#integer"));

        // Set 2 contains a definition with the same property but a different type; type
        // should be coerced to xsd:string
        ms2.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms2.getExtensionDefinitions().add(new ExtensionDefinition("ext_foo",
                "https://example.org/properties/fooProperty", "http://www.w3.org/2001/XMLSchema#date"));

        merger.merge(ms1, ms2);
        Assertions.assertEquals(1, ms1.getExtensionDefinitions().size());
        containsExtensionDefinition(ms1.getExtensionDefinitions(),
                new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));
    }

    @Test
    public void testMergingScalarSlots() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();

        ms1.setLicense("https://creativecommons.org/licenses/by/4.0/");
        ms2.setLicense("https://creativecommons.org/licenses/by/3.0/");

        SetMerger merger = new SetMerger();
        merger.setMergeOptions(EnumSet.of(MergeOption.MERGE_SCALARS));
        merger.merge(ms1, ms2);
        Assertions.assertEquals("https://creativecommons.org/licenses/by/3.0/", ms1.getLicense());
    }

    @Test
    public void testMergingMappingsOnly() {
        MappingSet ms1 = getSampleSet();
        MappingSet ms2 = getSampleSet();

        ms1.getCreatorId(true).add("https://example.org/people/0000-0000-0001-1234");
        ms2.getCreatorId(true).add("https://example.org/people/0000-0000-0001-5678");
        ms2.setLicense("https://creativecommons.org/licenses/by/4.0/");

        SetMerger merger = new SetMerger();
        merger.setMergeOptions(EnumSet.of(MergeOption.MERGE_MAPPINGS));
        merger.merge(ms1, ms2);

        // Metadata should not be merged
        Assertions.assertEquals(1, ms1.getCreatorId().size());
        Assertions.assertEquals("https://example.org/people/0000-0000-0001-1234", ms1.getCreatorId().get(0));
        Assertions.assertNull(ms1.getLicense());

        // But mappings should be
        Assertions.assertEquals(2, ms1.getMappings().size());
    }

    private void containsExtensionDefinition(List<ExtensionDefinition> actual, ExtensionDefinition expected) {
        boolean ok = false;
        for ( ExtensionDefinition ext : actual ) {
            if ( ext.getSlotName().equals(expected.getSlotName()) && ext.getProperty().equals(expected.getProperty())
                    && ext.getEffectiveType().equals(expected.getEffectiveType())
                    && ext.getTypeHint().equals(expected.getTypeHint()) ) {
                ok = true;
            }
        }

        Assertions.assertTrue(ok);
    }

    private MappingSet getSampleSet() {
        // @formatter:off
        MappingSet ms = MappingSet.builder()
                .mappings(new ArrayList<Mapping>())
                .curieMap(new HashMap<String,String>())
                .mappingSetId("https://example.org/sets/sample-set")
                .build();
        ms.getMappings().add(Mapping.builder()
                .subjectId("https://example.org/entities/0001")
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("https://example.com/entities/0011")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        // @formatter:off
        
        ms.getCurieMap().put("ORGENT", "https://example.org/entities/");
        ms.getCurieMap().put("COMENT", "https://example.com/entities/");
        
        return ms;
    }
}
