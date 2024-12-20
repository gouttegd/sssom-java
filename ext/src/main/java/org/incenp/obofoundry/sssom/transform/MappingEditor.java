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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.slots.ISimpleSlotVisitor;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;

/**
 * A mapping transformer that applies arbitrary changes to a mapping.
 * <p>
 * There are three ways to specify a change to apply. The simplest one is to use
 * {@link #addSimpleAssign(String, String)}, when the value to assign is static
 * and therefore fully known at the time the function is called:
 * 
 * <pre>
 * // Set a mapping justification
 * editor.addSimpleAssign("mapping_justification", "semapv:ManualMappingCuration");
 * </pre>
 * 
 * <p>
 * If the value to assign is to be generated from the mapping the change must be
 * applied to, then use {@link #addDelayedAssign(String, IMappingTransformer)}.
 * For example, to set the object label to a value derived from the subject
 * label:
 * 
 * <pre>
 * editor.addDelayedAssign("object_label", (mapping) -> String.format("same as %s", mapping -> getSubjectLabel()));
 * </pre>
 * 
 * <p>
 * Lastly, use {@link #addReplacement(String, String, String)} to change the
 * value of a slot by searching and replacing a pattern within the original
 * value:
 * 
 * <pre>
 * editor.addReplacement("subject_id", "https://meshb.nlm.nih.gov/record/ui[?]ui=", "http://id.nlm.nih.gov/mesh/");
 * </pre>
 */
public class MappingEditor implements IMappingTransformer<Mapping>, ISimpleSlotVisitor<Mapping, Void> {

    private static HashMap<String, Slot<Mapping>> slotsDict = new HashMap<String, Slot<Mapping>>();

    private HashMap<String, IMappingTransformer<Object>> values = new HashMap<String, IMappingTransformer<Object>>();
    private SlotHelper<Mapping> slotHelper = null;
    private PrefixManager pm;
    private Mapping sourceMapping;

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
     * {@link #addSimpleAssign(String, String)}.
     * 
     * @param prefixManager The prefix manager to use.
     */
    public MappingEditor(PrefixManager prefixManager) {
        pm = prefixManager;
    }

    @Override
    public Mapping transform(Mapping mapping) {
        Mapping edited = mapping.toBuilder().build();
        sourceMapping = mapping;
        getHelper().visitSlots(edited, this, true);
        return edited;
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping object, Object value) {
        if ( values.containsKey(slot.getName()) ) {
            slot.setValue(object, values.get(slot.getName()).transform(sourceMapping));
        }
        return null;
    }

    /**
     * Adds a change to be applied by this transformer. This is a simple change
     * where the value to assign is already fully known when the method is called:
     * the value does not depend on the mapping the change is applied to.
     * 
     * @param slotName The name of the mapping slot to change.
     * @param value    The new value of the slot. May be {@code null} or an empty
     *                 string to remove an existing value. For multi-value slots,
     *                 the values must be separated by a {@code |} character.
     * @exception IllegalArgumentException If {@code slotName} is not a valid slot
     *                                     name, or if the value is invalid.
     */
    public void addSimpleAssign(String slotName, String value) {
        Slot<Mapping> slot = slotsDict.get(slotName);
        if ( slot == null ) {
            throw new IllegalArgumentException(String.format("Invalid slot name: %s", slotName));
        }

        if ( value == null || value.isEmpty() ) {
            if ( slotName.equals("subject_id") || slotName.equals("object_id") || slotName.equals("predicate_id") ) {
                throw new IllegalArgumentException(String.format("Cannot set slot \"%s\" to nothing", slotName));
            }
            values.put(slotName, (mapping) -> null);
            return;
        }

        Object parsedValue = parseValue(slot, value);
        if ( parsedValue == null ) {
            throw new IllegalArgumentException(String.format("Invalid value \"%s\" for slot \"%s\"", value, slotName));
        }

        Object setValue = parsedValue;
        values.put(slotName, (mapping) -> setValue);

        // Reset slotHelper so that the list of visited slots is up-to-date
        slotHelper = null;
    }

    /**
     * Adds a change to be applied by transformer, when the new value to assign will
     * only be known at the time the change is applied because it may depend on the
     * mapping the change is applied to.
     * <p>
     * Of note, because the new value is not known at the time the change is
     * registered, we cannot perform any check on the value. We only check whether
     * the given slot name is a valid one.
     * 
     * @param slotName The name of the mapping slot to change.
     * @param value    The transformer that will derive the new value to assign from
     *                 a mapping.
     * @exception IllegalArgumentException If {@code slotName} is not a valid slot
     *                                     name.
     */
    public void addDelayedAssign(String slotName, IMappingTransformer<String> value) {
        Slot<Mapping> slot = slotsDict.get(slotName);
        if ( slot == null ) {
            throw new IllegalArgumentException(String.format("Invalid slot name: %s", slotName));
        }

        IMappingTransformer<Object> transformer = (mapping) -> parseValue(slot, value.transform(mapping));
        values.put(slotName, transformer);
        slotHelper = null;
    }

    /**
     * Adds a replacement operation to be applied by this transformer. The new value
     * of the edited slot is computed by finding all occurrences of the specified
     * pattern in the original slot value, and replacing them with the specified
     * replacement value.
     * 
     * @param slotName    The name of the mapping slot to change.
     * @param pattern     The pattern to replace in the slot's original value.
     * @param replacement The value the pattern should be replaced by.
     * @exception IllegalArgumentException If {@code slotName} is not a valid slot
     *                                     name, if the pattern is not a valid
     *                                     regular expression, or if the slot type
     *                                     is unsupported (only string and
     *                                     list-of-strings slots are supported for
     *                                     now).
     */
    public void addReplacement(String slotName, String pattern, String replacement) {
        Slot<Mapping> slot = slotsDict.get(slotName);
        if ( slot == null ) {
            throw new IllegalArgumentException(String.format("Invalid slot name: %s", slotName));
        }

        Pattern compiledPattern;
        try {
            compiledPattern = Pattern.compile(pattern);
        } catch ( PatternSyntaxException pse ) {
            throw new IllegalArgumentException(String.format("Invalid regular expression: %s", pse.getMessage()));
        }

        Class<?> type = slot.getType();
        if ( type == String.class ) {
            IMappingTransformer<Object> t = (mapping) -> {
                Matcher m = compiledPattern.matcher((String) slot.getValue(mapping));
                return m.replaceAll(replacement);
            };
            values.put(slotName, t);
        } else if ( type == List.class ) {
            IMappingTransformer<Object> t = (mapping) -> {
                @SuppressWarnings("unchecked")
                List<String> oldList = (List<String>) slot.getValue(mapping);
                ArrayList<String> newList = new ArrayList<String>();
                for ( String oldValue : oldList ) {
                    Matcher m = compiledPattern.matcher(oldValue);
                    newList.add(m.replaceAll(replacement));
                }
                return newList;
            };
            values.put(slotName, t);
        } else {
            throw new IllegalArgumentException(
                    String.format("Replacement operation not supported for slot '%s'", slotName));
        }

        // Reset slotHelper so that the list of visited slots is up-to-date
        slotHelper = null;
    }

    /*
     * Shared logic of simple and delayed assign operations.
     */
    private Object parseValue(Slot<Mapping> slot, String value) {
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

        return parsedValue;
    }

    private SlotHelper<Mapping> getHelper() {
        if ( slotHelper == null ) {
            slotHelper = SlotHelper.getMappingHelper(true);
            slotHelper.setSlots(values.keySet());
        }
        return slotHelper;
    }
}
