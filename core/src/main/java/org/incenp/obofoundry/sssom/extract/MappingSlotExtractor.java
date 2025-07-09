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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.Slot;

/**
 * Extracts the value of a slot from a mapping object.
 * <p>
 * This is the extractor used by an expression of the form
 * {@code mapping(N).slot.SLOTNAME}.
 */
public class MappingSlotExtractor extends MappingValueExtractor {

    protected SlotExtractor<Mapping> slotExtractor;

    /**
     * Creates a new instance.
     * 
     * @param mappingNo The 0-based index of the mapping from which to extract a
     *                  value, or (if negative) the 1-based index starting from the
     *                  last mapping.
     * @param slot      The mapping slot to extract.
     * @param itemNo    The 0-based index of the value to extract, or (if negative)
     *                  the 1-based index starting from the last item. This is only
     *                  meaningful for a multi-valued slot.
     */
    public MappingSlotExtractor(int mappingNo, Slot<Mapping> slot, int itemNo) {
        super(mappingNo);
        slotExtractor = new SlotExtractor<Mapping>(slot, itemNo);
    }

    @Override
    protected Object extract(Mapping mapping) {
        return slotExtractor.getSlotValue(mapping);
    }

    @Override
    public Class<?> getType() {
        return slotExtractor.getType();
    }
}
