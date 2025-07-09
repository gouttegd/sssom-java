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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;

/**
 * Common logic used by both the {@link MappingSetSlotExtractor} and the
 * {@link MappingSlotExtractor} classes.
 * 
 * @param <T> The type of object from which to extract a slot value
 *            ({@code MappingSet} or {@code Mapping}).
 */
public class SlotExtractor<T> extends SlotVisitorBase<T> {

    private Object extractedValue;
    protected Slot<T> slot;
    protected int itemNo;
    
    /**
     * Creates a new instance.
     * <p>
     * If the slot is a multi-valued slot, this will extract the first value.
     * 
     * @param slot The slot to extract.
     */
    public SlotExtractor(Slot<T> slot) {
        this.slot = slot;
        itemNo = 0;
    }

    /**
     * Creates a new instance.
     * 
     * @param slot   The slot to extract.
     * @param itemNo The 0-based index of the value to extract, or (if negative) the
     *               1-based index starting from the last value. This is only
     *               meaningful for a multi-valued slot.
     */
    public SlotExtractor(Slot<T> slot, int itemNo) {
        this.slot = slot;
        this.itemNo = itemNo;
    }

    /**
     * Performs the actual extraction.
     * 
     * @param object The object from which to extract the slot value.
     * @return The extract value, or {@code null} if the object does not have a
     *         value at the specified location.
     */
    public Object getSlotValue(T object) {
        extractedValue = null;
        Object value = slot.getValue(object);
        if ( value != null ) {
            slot.accept(this, object, value);
        }

        return extractedValue;
    }

    /**
     * Returns the type of the extracted value.
     * 
     * @return The type of the value that would be returned by
     *         {@link #getSlotValue(Object)}.
     */
    public Class<?> getType() {
        if ( slot instanceof StringSlot ) {
            // Always return "String", even if the slot is multi-valued and is therefore
            // typed as a "List".
            return String.class;
        }
        return slot.getType();
    }

    @Override
    public void visit(Slot<T> slot, T object, Object value) {
        extractedValue = value;
    }

    @Override
    public void visit(StringSlot<T> slot, T object, List<String> values) {
        int len = values.size();
        int n = itemNo >= 0 ? itemNo : len + itemNo;
        if ( n >= 0 && n < len ) {
            if ( len > 1 ) {
                ArrayList<String> tmp = new ArrayList<>(values);
                tmp.sort(Comparator.naturalOrder());
                values = tmp;
            }
            extractedValue = values.get(n);
        }
    }
}
