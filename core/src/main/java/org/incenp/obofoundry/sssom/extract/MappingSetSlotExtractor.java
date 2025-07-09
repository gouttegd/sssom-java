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

package org.incenp.obofoundry.sssom.extract;

import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.Slot;

/**
 * Extracts the value of a slot from a mapping set object.
 * <p>
 * This is the extractor used by an expression of the form
 * {@code set.slot.SLOTNAME}.
 */
public class MappingSetSlotExtractor implements IValueExtractor {

    protected SlotExtractor<MappingSet> slotExtractor;

    /**
     * Creates a new instance.
     * 
     * @param slot   The mapping set slot to extract.
     * @param itemNo The 0-based index of the value to extract. This is only
     *               meaningful if {@code slot} represents a multi-valued slot. A
     *               negative value is interpreted as an 1-based index starting from
     *               the last item.
     */
    public MappingSetSlotExtractor(Slot<MappingSet> slot, int itemNo) {
        slotExtractor = new SlotExtractor<MappingSet>(slot, itemNo);
    }

    @Override
    public Object extract(MappingSet ms) {
        return slotExtractor.getSlotValue(ms);
    }

    @Override
    public Class<?> getType() {
        return slotExtractor.getType();
    }
}
