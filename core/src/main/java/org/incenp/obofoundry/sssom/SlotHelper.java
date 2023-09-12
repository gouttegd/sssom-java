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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

/**
 * A class to facilitate the manipulation of SSSOM slots. This class is mostly
 * intended to hide all the hideous hacks relying on Java reflection.
 * 
 * @param <T> The SSSOM object (Mapping or MappingSet) this class is intended to
 *            ease the manipulation of.
 */
public class SlotHelper<T> {

    private static SlotHelper<Mapping> defaultMappingHelper;
    private static SlotHelper<MappingSet> defaultMappingSetHelper;

    private List<Slot<T>> slots = new ArrayList<Slot<T>>();
    private Map<String, Slot<T>> slotsByName = new HashMap<String, Slot<T>>();

    private SlotHelper(Class<T> type) {
        for ( Field f : type.getDeclaredFields() ) {
            if ( f.getName().equals("mappings") ) { // We never visit that slot.
                continue;
            }
            Slot<T> slot = new Slot<T>(type, f.getName());
            slots.add(slot);
            slotsByName.put(slot.getName(), slot);
        }
    }

    /**
     * Gets the default helper object to manipulate {@link Mapping} slots. The
     * default helper is set to visit all slots in the default order.
     * 
     * @return The default slot helper for {@link Mapping} objects.
     */
    public static SlotHelper<Mapping> getMappingHelper() {
        return getMappingHelper(false);
    }

    /**
     * Gets a helper object to manipulate {@link Mapping} slots. Use this method to
     * get a distinct helper from the default one. Settings of the returned object
     * can then be modified without altering the default helper.
     * 
     * @param newHelper {@code true} to return a distinct helper object, or
     *                  {@code false} to return the default helper.
     * @return A helper for {@link Mapping} objects.
     */
    public static SlotHelper<Mapping> getMappingHelper(boolean newHelper) {
        if ( newHelper ) {
            return new SlotHelper<Mapping>(Mapping.class);
        } else if ( defaultMappingHelper == null ) {
            defaultMappingHelper = new SlotHelper<Mapping>(Mapping.class);
        }
        return defaultMappingHelper;
    }

    /**
     * Gets the default helper object to manipulate {@link MappingSet} slots. The
     * default helper is set to visit all slots in the default order.
     * 
     * @return The default slot helper for {@link MappingSet} objects.
     */
    public static SlotHelper<MappingSet> getMappingSetHelper() {
        return getMappingSetHelper(false);
    }

    /**
     * Gets a helper object to manipulate {@link MappingSet} slots. Use this method
     * to get a distinct helper from the default one. Settings of the returned
     * object can then be modified without altering the default helper.
     * 
     * @param newHelper {@code true} to return a distinct helper object, or
     *                  {@code false} to return the default helper.
     * @return A helper for {@link MappingSet} objects.
     */
    public static SlotHelper<MappingSet> getMappingSetHelper(boolean newHelper) {
        if ( newHelper ) {
            return new SlotHelper<MappingSet>(MappingSet.class);
        } else if ( defaultMappingSetHelper == null ) {
            defaultMappingSetHelper = new SlotHelper<MappingSet>(MappingSet.class);
        }
        return defaultMappingSetHelper;
    }

    /**
     * Configures this helper to visit slots in alphabetical order.
     */
    public void setAlphabeticalOrder() {
        slots.sort((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.getName(), s2.getName()));
    }

    /**
     * Explicitly sets the list of slots to visit.
     * 
     * @param slotNames  The names of the slots that this object should visit.
     * @param forceOrder If {@code true}, slots will be visited in the order they
     *                   appear in the list; otherwise, the order will remain
     *                   unchanged.
     */
    public void setSlots(List<String> slotNames, boolean forceOrder) {
        if ( forceOrder ) {
            slots.clear();
            for ( String slotName : slotNames ) {
                Slot<T> slot = slotsByName.get(slotName);
                if ( slot == null ) {
                    throw new IllegalArgumentException(String.format("Invalid slot name: %s", slotName));
                }
                slots.add(slot);
            }
        } else {
            slots.removeIf(slot -> !slotNames.contains(slot.getName()));
        }
    }

    /**
     * Excludes the specified slots from being visited.
     * 
     * @param slotNames The names of the slots that this object should not visit.
     */
    public void excludeSlots(List<String> slotNames) {
        slots.removeIf(slot -> slotNames.contains(slot.getName()));
    }

    /**
     * Gets the current list of slots set to be visited. They are listed in the
     * order they would be visited.
     * 
     * @return The current slot lists.
     */
    public List<Slot<T>> getSlots() {
        return slots;
    }

    /**
     * Visits the slots of a given object.
     * 
     * @param <V>     The type of object the visitor should return for each slot.
     * @param object  The object whose slots should be visited.
     * @param visitor The visitor to use.
     * @return A list of all values returned by the visitor for each slot.
     */
    public <V> List<V> visitSlots(T object, SlotVisitor<T, V> visitor) {
        return visitSlots(object, visitor, false);
    }

    /**
     * Visits the slots of a given object.
     * 
     * @param <V>       The type of objects the visitor should return for each slot.
     * @param object    The object whose slots should be visited.
     * @param visitor   The visitor to use.
     * @param visitNull If {@code true}, slots with a {@code null} value will be
     *                  visited as well.
     * @return A list of all values returned by the visitor for each slot.
     */
    public <V> List<V> visitSlots(T object, SlotVisitor<T, V> visitor, boolean visitNull) {
        List<V> visited = new ArrayList<V>();
        for ( Slot<T> slot : slots ) {
            Object slotValue = slot.getValue(object);
            if ( slotValue == null && !visitNull ) {
                continue;
            }

            Type fieldType = slot.getType();
            V value = null;
            if ( fieldType.equals(String.class) ) {
                String text = String.class.cast(slotValue);
                value = visitor.visit(slot, object, text);
            } else if ( fieldType.equals(List.class) ) {
                @SuppressWarnings("unchecked")
                List<String> list = List.class.cast(slotValue);
                value = visitor.visit(slot, object, list);
            } else if ( fieldType.equals(Map.class) ) {
                @SuppressWarnings("unchecked")
                Map<String, String> map = Map.class.cast(slotValue);
                value = visitor.visit(slot, object, map);
            } else if ( fieldType.equals(Double.class) ) {
                Double dbl = Double.class.cast(slotValue);
                value = visitor.visit(slot, object, dbl);
            } else if ( fieldType.equals(LocalDate.class) ) {
                LocalDate date = LocalDate.class.cast(slotValue);
                value = visitor.visit(slot, object, date);
            } else if ( fieldType.equals(EntityType.class) ) {
                EntityType et = EntityType.class.cast(slotValue);
                value = visitor.visit(slot, object, et);
            } else if ( fieldType.equals(MappingCardinality.class) ) {
                MappingCardinality mc = MappingCardinality.class.cast(slotValue);
                value = visitor.visit(slot, object, mc);
            } else if ( fieldType.equals(PredicateModifier.class) ) {
                PredicateModifier pm = PredicateModifier.class.cast(slotValue);
                value = visitor.visit(slot, object, pm);
            }

            if ( value != null ) {
                visited.add(value);
            }
        }

        return visited;
    }

    /**
     * Expands identifiers in all slots holding entity references.
     * 
     * @param object        The object whose entity reference slots should be
     *                      visited.
     * @param prefixManager The prefix manager to use to expand the short
     *                      identifiers.
     */
    public void expandIdentifiers(T object, PrefixManager prefixManager) {
        for ( Slot<T> slot : slots ) {
            if ( slot.isEntityReference() ) {
                Object slotValue = slot.getValue(object);
                if ( slotValue != null ) {
                    if ( slot.getType().equals(String.class) ) {
                        slot.setValue(object, prefixManager.expandIdentifier(String.class.cast(slotValue)));
                    } else if ( slot.getType().equals(List.class) ) {
                        @SuppressWarnings("unchecked")
                        List<String> slotValues = List.class.cast(slotValue);
                        prefixManager.expandIdentifiers(slotValues, true);
                    }
                }
            }
        }
    }
}
