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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.cli;

import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationOptionTest {

    @Test
    void testSettingNewValue() {
        MappingSet ms = new MappingSet();
        getOption("mapping_tool", "tool", false).apply(ms);

        Assertions.assertEquals("tool", ms.getMappingTool());
    }

    @Test
    void testClearingValue() {
        MappingSet ms = new MappingSet();
        ms.setMappingTool("my tool");
        getOption("mapping_tool", null, false).apply(ms);

        Assertions.assertNull(ms.getMappingTool());
    }

    @Test
    void testAddingValueToList() {
        MappingSet ms = new MappingSet();
        ms.getCreatorId(true).add("first creator");
        getOption("creator_id", "second creator", true).apply(ms);

        Assertions.assertEquals(2, ms.getCreatorId().size());
    }

    @Test
    void testClearingList() {
        MappingSet ms = new MappingSet();
        ms.getCreatorId(true).add("first creator");
        getOption("creator_id", null, false).apply(ms);

        Assertions.assertNull(ms.getCreatorId());
    }

    @Test
    void testResettingList() {
        MappingSet ms = new MappingSet();
        ms.getCreatorId(true).add("first creator");
        getOption("creator_id", "new first creator", false).apply(ms);

        Assertions.assertEquals(1, ms.getCreatorId().size());
        Assertions.assertEquals("new first creator", ms.getCreatorId().get(0));
    }

    private AnnotationOption getOption(String name, String value, boolean append) {
        return new AnnotationOption(SlotHelper.getMappingSetHelper().getSlotByName(name), value, append);
    }
}
