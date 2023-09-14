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

import java.time.LocalDate;
import java.util.List;

import org.incenp.obofoundry.sssom.model.MappingSet;
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
        
        List<String> visitedValues = SlotHelper.getMappingSetHelper().visitSlots(ms, new MappingSetTestVisitor());
        Assertions.assertEquals("license=A license", visitedValues.get(0));
        Assertions.assertEquals("mapping_tool=A mapping tool", visitedValues.get(1));
        Assertions.assertEquals("publication_date=2023-09-13", visitedValues.get(2));
        Assertions.assertEquals("comment=A comment", visitedValues.get(3));
    }
    
    @Test
    void testVisitingOrder() {
        MappingSet ms = getSampleMappingSet();
        SlotHelper<MappingSet> helper = SlotHelper.getMappingSetHelper(true);
        helper.setAlphabeticalOrder();
        
        List<String> visitedValues = helper.visitSlots(ms, new MappingSetTestVisitor());
        Assertions.assertEquals("comment=A comment", visitedValues.get(0));
        Assertions.assertEquals("license=A license", visitedValues.get(1));
        Assertions.assertEquals("mapping_tool=A mapping tool", visitedValues.get(2));
        Assertions.assertEquals("publication_date=2023-09-13", visitedValues.get(3));
    }
    
    private class MappingSetTestVisitor extends SlotVisitorBase<MappingSet, String> {
        @Override
        protected String getDefault(Slot<MappingSet> slot, MappingSet mappingSet, Object value) {
            return String.format("%s=%s", slot.getName(), value.toString());
        }
    }
}
