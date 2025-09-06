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

package org.incenp.obofoundry.sssom.slots;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SlotHelperTest {

    private MappingSet getSampleMappingSet() {
        // @formatter:off
        return MappingSet.builder()
                .comment("A comment")
                .license("A license")
                .publicationDate(LocalDate.of(2023, 9, 13))
                .mappingTool("A mapping tool")
                .build();
        // @formatter:off
    }

    @Test
    void testMappingSetVisitor() {
        MappingSet ms = getSampleMappingSet();
        
        MappingSetTestVisitor visitor = new MappingSetTestVisitor();
        SlotHelper.getMappingSetHelper().visitSlots(ms, visitor, false);
        Assertions.assertEquals("license=A license", visitor.visitedValues.get(0));
        Assertions.assertEquals("mapping_tool=A mapping tool", visitor.visitedValues.get(1));
        Assertions.assertEquals("publication_date=2023-09-13", visitor.visitedValues.get(2));
        Assertions.assertEquals("comment=A comment", visitor.visitedValues.get(3));
    }
    
    @Test
    void testVisitingOrder() {
        MappingSet ms = getSampleMappingSet();
        SlotHelper<MappingSet> helper = SlotHelper.getMappingSetHelper(true);
        helper.setAlphabeticalOrder();

        MappingSetTestVisitor visitor = new MappingSetTestVisitor();
        helper.visitSlots(ms, visitor, false);
        Assertions.assertEquals("comment=A comment", visitor.visitedValues.get(0));
        Assertions.assertEquals("license=A license", visitor.visitedValues.get(1));
        Assertions.assertEquals("mapping_tool=A mapping tool", visitor.visitedValues.get(2));
        Assertions.assertEquals("publication_date=2023-09-13", visitor.visitedValues.get(3));
    }

    @Test
    void testForceVisitingOrder() {
        MappingSet ms = getSampleMappingSet();
        SlotHelper<MappingSet> helper = SlotHelper.getMappingSetHelper(true);
        helper.setSlots(Arrays.asList("publication_date", "mapping_tool", "license", "comment"), true);

        MappingSetTestVisitor visitor = new MappingSetTestVisitor();
        helper.visitSlots(ms, visitor);

        Assertions.assertEquals("publication_date=2023-09-13", visitor.visitedValues.get(0));
        Assertions.assertEquals("mapping_tool=A mapping tool", visitor.visitedValues.get(1));
        Assertions.assertEquals("license=A license", visitor.visitedValues.get(2));
        Assertions.assertEquals("comment=A comment", visitor.visitedValues.get(3));

        List<String> visitedSlots = helper.getSlotNames();
        Assertions.assertEquals("publication_date", visitedSlots.get(0));
        Assertions.assertEquals("mapping_tool", visitedSlots.get(1));
        Assertions.assertEquals("license", visitedSlots.get(2));
        Assertions.assertEquals("comment", visitedSlots.get(3));
    }

    @Test
    void testExpandIdentifiers() {
        MappingSet ms = getSampleMappingSet();
        ms.getCreatorId(true).add("ORGPID:0000-0000-0001-1234");
        ms.setMappingToolId("ORGENT:1234");

        PrefixManager pm = new PrefixManager();
        pm.add("ORGPID", "https://example.org/people/");
        pm.add("ORGENT", "https://example.org/entities/");

        SlotHelper.getMappingSetHelper().expandIdentifiers(ms, pm);
        Assertions.assertEquals("https://example.org/people/0000-0000-0001-1234", ms.getCreatorId().get(0));
        Assertions.assertEquals("https://example.org/entities/1234", ms.getMappingToolId());
    }

    @Test
    void testSlotLists() {
        Collection<String> slots = SlotHelper.getMappingSlotList("mapping,-predicate_id,license");
        Assertions.assertTrue(slots.contains("subject_id"));
        Assertions.assertFalse(slots.contains("predicate_id"));
        Assertions.assertTrue(slots.contains("object_id"));
        Assertions.assertTrue(slots.contains("license"));
        Assertions.assertEquals(3, slots.size());
    }
    
    @Test
    void testGetSlotsForVersion() {
        List<Slot<MappingSet>> slots = SlotHelper.getMappingSetHelper().getSlots(Version.SSSOM_1_0);
        Set<String> slotNames = new HashSet<>();
        for (Slot<MappingSet> slot : slots) {
            slotNames.add(slot.getName());
        }

        Assertions.assertTrue(slotNames.contains("subject_type"));
        Assertions.assertFalse(slotNames.contains("mapping_tool_id"));
    }

    private class MappingSetTestVisitor extends SlotVisitorBase<MappingSet> {
        List<String> visitedValues = new ArrayList<String>();

        @Override
        public void visit(Slot<MappingSet> slot, MappingSet mappingSet, Object value) {
            visitedValues.add(String.format("%s=%s", slot.getName(), value.toString()));
        }
    }
}
