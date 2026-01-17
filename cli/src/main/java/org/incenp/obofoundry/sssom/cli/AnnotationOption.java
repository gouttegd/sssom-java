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
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.StringSlot;

/**
 * Represents the value of the <code>--annotate</code> option.
 */
public class AnnotationOption {

    Slot<MappingSet> slot;
    String value;
    boolean append;

    /**
     * Creates a new annotation option value.
     * 
     * @param slot   The slot a value should be assigned to.
     * @param value  The value to assign.
     * @param append Only meaningful for multi-valued slots; if <code>true</code>,
     *               the new value will be appended to the existing values (if any),
     *               otherwise existing values will be overwritten.
     */
    public AnnotationOption(Slot<MappingSet> slot, String value, boolean append) {
        this.slot = slot;
        this.value = value;
        this.append = append;
    }

    /**
     * Applies the option, that is, assigns the value to the target set.
     * 
     * @param mappingSet The mapping set that should be annotated.
     */
    public void apply(MappingSet mappingSet) {
        if ( !append && slot instanceof StringSlot && ((StringSlot<MappingSet>) slot).isMultivalued() ) {
            slot.setValue(mappingSet, (Object) null);
        }
        slot.setValue(mappingSet, value);
    }
}
