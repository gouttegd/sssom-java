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

package org.incenp.obofoundry.sssom.slots;

import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SlotTest {

    @Test
    void testPropagatableSlotForVersion() {
        SlotHelper<MappingSet> helper = SlotHelper.getMappingSetHelper();
        Slot<MappingSet> slot;

        // A slot that has always been propagatable
        slot = helper.getSlotByName("subject_type");
        Assertions.assertTrue(slot.isPropagatable());
        Assertions.assertTrue(slot.isPropagatable(Version.SSSOM_1_0));
        Assertions.assertTrue(slot.isPropagatable(Version.SSSOM_1_1));
        Assertions.assertFalse(slot.isPropagatable(Version.UNKNOWN));

        // A slot that has been added to MappingSet in 1.1
        slot = helper.getSlotByName("curation_rule");
        Assertions.assertTrue(slot.isPropagatable());
        Assertions.assertFalse(slot.isPropagatable(Version.SSSOM_1_0));
        Assertions.assertTrue(slot.isPropagatable(Version.SSSOM_1_1));
        Assertions.assertFalse(slot.isPropagatable(Version.UNKNOWN));

        // A slot that has never been propagatable
        slot = helper.getSlotByName("mapping_set_version");
        Assertions.assertFalse(slot.isPropagatable());
        Assertions.assertFalse(slot.isPropagatable(Version.SSSOM_1_0));
        Assertions.assertFalse(slot.isPropagatable(Version.SSSOM_1_1));
        Assertions.assertFalse(slot.isPropagatable(Version.UNKNOWN));

        // A slot that has been added to MappingSet in 1.1, but not propagatable
        slot = helper.getSlotByName("sssom_version");
        Assertions.assertFalse(slot.isPropagatable());
        Assertions.assertFalse(slot.isPropagatable(Version.SSSOM_1_0));
        Assertions.assertFalse(slot.isPropagatable(Version.SSSOM_1_1));
        Assertions.assertFalse(slot.isPropagatable(Version.UNKNOWN));
    }

    @Test
    void testCondensationDiscouraged() {
        SlotHelper<MappingSet> helper = SlotHelper.getMappingSetHelper();
        Slot<MappingSet> slot;

        slot = helper.getSlotByName("subject_type");
        Assertions.assertTrue(slot.isPropagatable());
        Assertions.assertFalse(slot.isCondensationDiscouraged());

        slot = helper.getSlotByName("predicate_id");
        Assertions.assertTrue(slot.isPropagatable());
        Assertions.assertTrue(slot.isCondensationDiscouraged());
    }
}
