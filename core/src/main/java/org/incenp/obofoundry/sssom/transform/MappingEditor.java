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

package org.incenp.obofoundry.sssom.transform;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SimpleSlotVisitor;
import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

/**
 * A mapping transformer that applies arbitrary changes to a mapping.
 * <p>
 * Changes to be applied must be specified by one or several calls to
 * {@link #addEdit(String, String)}:
 * 
 * <pre>
 * MappingEditor editor = new MappingEditor();
 * // set a mapping justification
 * editor.addEdit("mapping_justification", "semapv:ManualMappingCuration");
 * // remove subject source
 * editor.addEdit("subject_source", null);
 * </pre>
 */
public class MappingEditor implements IMappingTransformer<Mapping>, SimpleSlotVisitor<Mapping, Void> {

    private static HashMap<String, Slot<Mapping>> slotsDict = new HashMap<String, Slot<Mapping>>();

    private HashMap<String, Object> values = new HashMap<String, Object>();
    private SlotHelper<Mapping> slotHelper = null;
    private PrefixManager pm;

    static {
        for ( Slot<Mapping> slot : SlotHelper.getMappingHelper().getSlots() ) {
            slotsDict.put(slot.getName(), slot);
        }
    }

    /**
     * Creates a new instance with the default SSSOM prefix manager.
     */
    public MappingEditor() {
        pm = new PrefixManager();
    }

    /**
     * Creates a new instance with the specified prefix manager. The prefix manager
     * is used to expand short identifiers in arguments to
     * {@link #addEdit(String, String)}.
     * 
     * @param prefixManager The prefix manager to use.
     */
    public MappingEditor(PrefixManager prefixManager) {
        pm = prefixManager;
    }

    @Override
    public Mapping transform(Mapping mapping) {
        Mapping edited = mapping.toBuilder().build();
        getHelper().visitSlots(edited, this, true);
        return edited;
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping object, Object value) {
        if ( values.containsKey(slot.getName()) ) {
            slot.setValue(object, values.get(slot.getName()));
        }
        return null;
    }

    /**
     * Adds a change to be applied by this transformer.
     * 
     * @param slotName The name of the mapping slot to change.
     * @param value    The new value of the slot. May be {@code null} or an empty
     *                 string to remove an existing value. For multi-value slots,
     *                 the values must be separated by a {@code |} character.
     */
    public void addEdit(String slotName, String value) {
        Slot<Mapping> slot = slotsDict.get(slotName);
        if ( slot == null ) {
            throw new IllegalArgumentException(String.format("Invalid slot name: %s", slotName));
        }

        if ( value == null || value.isEmpty() ) {
            values.put(slotName, null);
            return;
        }

        Object parsedValue = null;
        Class<?> type = slot.getType();
        if ( type == String.class ) {
            parsedValue = slot.isEntityReference() ? pm.expandIdentifier(value) : value;
        } else if ( type == List.class ) {
            List<String> listValue = new ArrayList<String>();
            for ( String item : value.split("\\|") ) {
                listValue.add(slot.isEntityReference() ? pm.expandIdentifier(item.trim()) : item.trim());
            }
            parsedValue = listValue;
        } else if ( type == LocalDate.class ) {
            try {
                parsedValue = LocalDate.parse(value);
            } catch ( DateTimeParseException e ) {
            }
        } else if ( type == Double.class ) {
            try {
                parsedValue = Double.valueOf(value);
            } catch ( NumberFormatException e ) {
            }
        } else if ( type == EntityType.class ) {
            parsedValue = EntityType.fromString(value);
        } else if ( type == MappingCardinality.class ) {
            parsedValue = MappingCardinality.fromString(value);
        } else if ( type == PredicateModifier.class ) {
            parsedValue = PredicateModifier.fromString(value);
        }

        if ( parsedValue == null ) {
            throw new IllegalArgumentException(String.format("Invalid value \"%s\" for slot \"%s\"", value, slotName));
        }

        values.put(slotName, parsedValue);

        // Reset slotHelper so that the list of visited slots is up-to-date
        slotHelper = null;
    }

    private SlotHelper<Mapping> getHelper() {
        if ( slotHelper == null ) {
            slotHelper = SlotHelper.getMappingHelper(true);
            slotHelper.setSlots(values.keySet());
        }
        return slotHelper;
    }
}
