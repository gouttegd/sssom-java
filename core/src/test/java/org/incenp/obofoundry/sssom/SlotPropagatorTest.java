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

package org.incenp.obofoundry.sssom;

import java.util.ArrayList;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SlotPropagatorTest {

    @Test
    void testAlwaysReplacePolicy() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("sample mapping tool");
        ms.getMappings().get(1).setMappingTool("another mapping tool");

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.AlwaysReplace);
        Set<String> propagated = sp.propagate(ms);

        Assertions.assertEquals(1, propagated.size());
        Assertions.assertTrue(propagated.contains("mapping_tool"));

        for ( Mapping mapping : ms.getMappings() ) {
            Assertions.assertEquals("sample mapping tool", mapping.getMappingTool());
        }

        Assertions.assertNull(ms.getMappingTool());
    }

    @Test
    void testReplaceIfUnsetPolicy() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("sample mapping tool");
        ms.getMappings().get(1).setMappingTool("another mapping tool");

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.ReplaceIfUnset);
        Set<String> propagated = sp.propagate(ms);

        Assertions.assertEquals(1, propagated.size());
        Assertions.assertTrue(propagated.contains("mapping_tool"));

        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(0).getMappingTool());
        Assertions.assertEquals("another mapping tool", ms.getMappings().get(1).getMappingTool());
        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(2).getMappingTool());
        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(3).getMappingTool());

        Assertions.assertNull(ms.getMappingTool());
    }

    @Test
    void testNeverReplacePolicy() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("sample mapping tool");
        ms.getMappings().get(1).setMappingTool("another mapping tool");

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.NeverReplace);
        Set<String> propagated = sp.propagate(ms);

        Assertions.assertTrue(propagated.isEmpty());

        Assertions.assertNull(ms.getMappings().get(0).getMappingTool());
        Assertions.assertEquals("another mapping tool", ms.getMappings().get(1).getMappingTool());
        Assertions.assertNull(ms.getMappings().get(2).getMappingTool());
        Assertions.assertNull(ms.getMappings().get(3).getMappingTool());

        Assertions.assertEquals("sample mapping tool", ms.getMappingTool());
    }

    @Test
    void testPreserveValuesUponPropagating() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("sample mapping tool");
        ms.getMappings().get(1).setMappingTool("another mapping tool");

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.AlwaysReplace);
        Set<String> propagated = sp.propagate(ms, true);

        Assertions.assertEquals(1, propagated.size());
        Assertions.assertTrue(propagated.contains("mapping_tool"));

        for ( Mapping mapping : ms.getMappings() ) {
            Assertions.assertEquals("sample mapping tool", mapping.getMappingTool());
        }

        Assertions.assertEquals("sample mapping tool", ms.getMappingTool());
    }

    @Test
    void testNonPropagatableSlots() {
        MappingSet ms = getSampleSet();
        ms.setComment("mapping set level comment");
        ms.setMappingTool("sample mapping tool");

        SlotPropagator sp = new SlotPropagator();
        Set<String> propagated = sp.propagate(ms);

        Assertions.assertTrue(propagated.contains("mapping_tool"));
        Assertions.assertFalse(propagated.contains("comment"));

        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(0).getMappingTool());
        Assertions.assertNull(ms.getMappings().get(0).getComment());
    }

    @Test
    void testPropagateListValue() {
        ArrayList<String> preprocessings = new ArrayList<String>();
        preprocessings.add("http://example.org/preprocessing1");
        SlotPropagator sp = new SlotPropagator();

        MappingSet ms = getSampleSet();
        ms.setObjectPreprocessing(preprocessings);
        Set<String> propagated = sp.propagate(ms);

        Assertions.assertTrue(propagated.contains("object_preprocessing"));

        Assertions.assertNotNull(ms.getMappings().get(0).getObjectPreprocessing());
        Assertions.assertEquals(1, ms.getMappings().get(0).getObjectPreprocessing().size());
        Assertions.assertEquals("http://example.org/preprocessing1",
                ms.getMappings().get(0).getObjectPreprocessing().get(0));
    }

    @Test
    void testCondense() {
        MappingSet ms = getSampleSet();
        for ( Mapping m : ms.getMappings() ) {
            m.setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator();
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertEquals(1, condensed.size());
        Assertions.assertTrue(condensed.contains("mapping_tool"));
        Assertions.assertEquals("sample mapping tool", ms.getMappingTool());
    }

    @Test
    void testCondenseListValue() {
        MappingSet ms = getSampleSet();
        for ( Mapping m : ms.getMappings() ) {
            ArrayList<String> preprocessings = new ArrayList<String>();
            preprocessings.add("http://example.org/preprocessing1");
            m.setObjectPreprocessing(preprocessings);
        }

        SlotPropagator sp = new SlotPropagator();
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertEquals(1, condensed.size());
        Assertions.assertTrue(condensed.contains("object_preprocessing"));
        Assertions.assertEquals("http://example.org/preprocessing1", ms.getObjectPreprocessing().get(0));
    }

    @Test
    void testDontCondenseIfValuesAreDifferent() {
        MappingSet ms = getSampleSet();
        for ( int i = 0; i < ms.getMappings().size(); i++ ) {
            // All mappings have the same value except the last one
            ms.getMappings().get(i).setMappingTool(i == 3 ? "another mapping tool" : "sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator();
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertTrue(condensed.isEmpty());
        Assertions.assertNull(ms.getMappingTool());
        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(0).getMappingTool());
    }

    @Test
    void testDontCondenseIfSomeValuesAreNull() {
        MappingSet ms = getSampleSet();
        for ( int i = 0; i < ms.getMappings().size() - 1; i++ ) {
            // All mappings have the same value, except the last one who is null
            ms.getMappings().get(i).setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator();
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertTrue(condensed.isEmpty());
        Assertions.assertNull(ms.getMappingTool());
        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(0).getMappingTool());
    }

    @Test
    void testMappingValuesAreDeletedUponCondensing() {
        MappingSet ms = getSampleSet();
        for ( Mapping m : ms.getMappings() ) {
            m.setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator();
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertTrue(condensed.contains("mapping_tool"));
        Assertions.assertEquals("sample mapping tool", ms.getMappingTool());
        for ( Mapping m : ms.getMappings() ) {
            Assertions.assertNull(m.getMappingTool());
        }
    }

    @Test
    void testPreserveMappingValuesUponCondensing() {
        MappingSet ms = getSampleSet();
        for ( Mapping m : ms.getMappings() ) {
            m.setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator();
        Set<String> condensed = sp.condense(ms, true);

        Assertions.assertTrue(condensed.contains("mapping_tool"));
        Assertions.assertEquals("sample mapping tool", ms.getMappingTool());
        for ( Mapping m : ms.getMappings() ) {
            Assertions.assertEquals("sample mapping tool", m.getMappingTool());
        }
    }

    @Test
    void testCondenseReplaceIfUnsetPolicy() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("another mapping tool");
        for ( Mapping m : ms.getMappings() ) {
            m.setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.ReplaceIfUnset);
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertTrue(condensed.isEmpty());
        Assertions.assertEquals("another mapping tool", ms.getMappingTool());
        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(0).getMappingTool());
    }

    @Test
    void testCondenseNeverReplacePolicy() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("another mapping tool");
        for ( Mapping m : ms.getMappings() ) {
            m.setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.NeverReplace);
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertTrue(condensed.isEmpty());
        Assertions.assertEquals("another mapping tool", ms.getMappingTool());
        Assertions.assertEquals("sample mapping tool", ms.getMappings().get(0).getMappingTool());
    }

    @Test
    void testCondenseAlreadyCondensed() {
        MappingSet ms = getSampleSet();
        ms.setMappingTool("sample mapping tool");
        for ( Mapping m : ms.getMappings() ) {
            m.setMappingTool("sample mapping tool");
        }

        SlotPropagator sp = new SlotPropagator(PropagationPolicy.NeverReplace);
        Set<String> condensed = sp.condense(ms, false);

        Assertions.assertTrue(condensed.contains("mapping_tool"));
        Assertions.assertEquals("sample mapping tool", ms.getMappingTool());
        Assertions.assertNull(ms.getMappings().get(0).getMappingTool());
    }

    private MappingSet getSampleSet() {
        // @formatter:off
        MappingSet ms = MappingSet.builder()
                .mappingSetTitle("sample set")
                .mappings(new ArrayList<Mapping>())
                .build();
        // @formatter:on

        for ( int i = 0; i < 4; i++ ) {
            Mapping m = new Mapping();
            m.setSubjectId(String.format("http://example.org/subject%d", i));
            m.setObjectId(String.format("http://example.org/object%d", i));
            m.setPredicateId(CommonPredicate.SKOS_EXACT_MATCH.toString());
            ms.getMappings().add(m);
        }

        return ms;
    }
}
