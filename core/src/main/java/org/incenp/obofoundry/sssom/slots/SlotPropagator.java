/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.PropagationPolicy;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A helper class to implement the propagation of slots from the mapping set
 * level to the level of individual mappings and the other way round.
 * <p>
 * Some of the SSSOM metadata slots exist both on the MappingSet class and on
 * the Mapping class. Among them, some represent a different metadata depending
 * on whether they are on the mapping set or on a mapping. For example, the
 * {@code creator_id} slot on a mapping set represents the creators of the
 * entire set, while that same slot on an individual mapping represents the
 * creator of that particular mapping (which may be different from the creators
 * of the set).
 * <p>
 * But there are also metadata slots that are actually intended to represent a
 * common value for all mappings in the set, rather than a value for the set
 * itself. For example, the {@code mapping_tool} slot, when used on a set,
 * indicates the mapping tool used by all mappings in the set. A mapping set
 * that has a {@code mapping_tool} value should be considered as a mapping set
 * in which all mappings have that same value in their {@code mapping_tool}
 * slots.
 * <p>
 * To implement this behaviour, this class defines two operations on a mapping
 * set:
 * <ul>
 * <li><em>propagation</em>, in which the value of any “propagatable” slot on
 * the mapping set is explicitly assigned to the corresponding slot in all the
 * mappings of that set;
 * <li><em>condensation</em>, which does the opposite: if all the mappings in a
 * set have the same value for a given ”propagatable” slot, that value is
 * assigned to the corresponding slot on the mapping set.
 * </ul>
 */
public class SlotPropagator {
    private static Set<String> slots = new HashSet<String>();

    static {
        // Prepare the list of propagatable slots.
        for ( Slot<MappingSet> slot : SlotHelper.getMappingSetHelper().getSlots() ) {
            if ( slot.isPropagatable() ) {
                slots.add(slot.getName());
            }
        }
    }

    private PropagationPolicy policy;

    /**
     * Creates a new instance using the default propagation policy (always replace).
     */
    public SlotPropagator() {
        this.policy = PropagationPolicy.AlwaysReplace;
    }

    /**
     * Creates a new instance with the specified propagation policy.
     * 
     * @param policy The default propagation policy to use.
     */
    public SlotPropagator(PropagationPolicy policy) {
        this.policy = policy;
    }

    /**
     * Sets the propagation policy to use. The new policy will be used in all
     * subsequent calls to {@link #propagate(MappingSet)} and
     * {@link #condense(MappingSet, boolean)}.
     * 
     * @param policy The new policy to use.
     */
    public void setStrategy(PropagationPolicy policy) {
        this.policy = policy;
    }

    /**
     * Propagates the values of slots from the mapping set level to the individual
     * mappings.
     * 
     * @param mappingSet The mapping set whose slot values should be propagated.
     * @return A set containing the names of slots that were effectively propagated.
     */
    public Set<String> propagate(MappingSet mappingSet) {
        return propagate(mappingSet, false);
    }

    /**
     * Propagates the values of slots the mapping set level to the individual
     * mappings.
     * 
     * @param mappingSet The mapping set whose slot values should be propagated.
     * @param preserve   If {@code true}, set-level values will not be removed after
     *                   propagation.
     * @return A set containing the names of slots that were effectively propagated.
     */
    public Set<String> propagate(MappingSet mappingSet, boolean preserve) {
        if ( policy == PropagationPolicy.Disabled ) {
            return new HashSet<String>();
        }

        // We must first determine which of the propagatable slots have values at the
        // set level.
        Map<String, Object> values = new HashMap<String, Object>();
        SlotHelper<MappingSet> setHelper = SlotHelper.getMappingSetHelper(true);
        setHelper.setSlots(slots); // Visit only propagatable slots
        setHelper.visitSlots(mappingSet, (slot, m, value) -> values.put(slot.getName(), value));

        // Prepare to visit the slots on the individual mappings. We only need to visit
        // the propagatable slots that do have a value at the set level.
        SlotHelper<Mapping> mappingHelper = SlotHelper.getMappingHelper(true);
        mappingHelper.setSlots(new ArrayList<String>(values.keySet()), false);

        NullifyVisitor<MappingSet> nullify = new NullifyVisitor<MappingSet>();

        if ( policy == PropagationPolicy.NeverReplace ) {
            // We must iterate over all the mappings a first time to check whether they have
            // their own values for the slots to propagate.
            Set<String> usedSlots = new HashSet<String>();
            for ( Mapping mapping : mappingSet.getMappings() ) {
                mappingHelper.visitSlots(mapping, (slot, m, value) -> {
                    usedSlots.add(slot.getName());
                    return null;
                });
            }

            if ( !usedSlots.isEmpty() ) {
                // Do not visit the slots for which some mappings do have a value already.
                mappingHelper.excludeSlots(new ArrayList<String>(usedSlots));
                values.keySet().removeAll(usedSlots);
            }
        }

        // Iterate over the mappings to perform the actual propagation.
        for ( Mapping mapping : mappingSet.getMappings() ) {
            mappingHelper.visitSlots(mapping, (slot, m, value) -> {
                if ( value == null || policy != PropagationPolicy.ReplaceIfUnset ) {
                    slot.setValue(m, values.get(slot.getName()));
                }
                return null;
            }, true);
        }

        if ( !preserve ) {
            // Remove the values of propagated slots from the set
            setHelper.setSlots(values.keySet());
            setHelper.visitSlots(mappingSet, nullify);
        }

        return values.keySet();
    }

    /**
     * Condenses a mapping set, i.e. move slot values from the level of individual
     * mappings up to the mapping set level wherever possible.
     * <p>
     * Note that for this operation, the {@link PropagationPolicy#ReplaceIfUnset}
     * and {@link PropagationPolicy#NeverReplace} policies are equivalent. They both
     * lead to a slot <em>not</em> being condensed if the set already has a value
     * for that slot.
     * 
     * @param mappingSet The mapping set to condense.
     * @param preserve   If {@code true}, mapping-level values will only be copied
     *                   to the set, and not removed from the mappings.
     * @return A set containing the names of slots whose values were effectively
     *         moved (or copied) to the set level.
     */
    public Set<String> condense(MappingSet mappingSet, boolean preserve) {
        if ( policy == PropagationPolicy.Disabled ) {
            return new HashSet<String>();
        }

        // Iterate over all the mappings to collect all the values in the condensable
        // slots.
        Map<String, Set<Object>> values = new HashMap<String, Set<Object>>();
        SlotHelper<Mapping> mappingHelper = SlotHelper.getMappingHelper(true);
        mappingHelper.setSlots(new ArrayList<String>(slots), false);
        for ( Mapping mapping : mappingSet.getMappings() ) {
            mappingHelper.visitSlots(mapping, (slot, m, value) -> values
                    .computeIfAbsent(slot.getName(), (s) -> new HashSet<Object>()).add(value), true);
        }

        // Visit the condensable slots on the mapping set level and set them to the
        // corresponding collected value.
        SlotHelper<MappingSet> setHelper = SlotHelper.getMappingSetHelper(true);
        setHelper.setSlots(slots);
        Set<String> condensedSlots = new HashSet<String>();
        ISimpleSlotVisitor<MappingSet, Void> v = (slot, ms, value) -> {
            String slotName = slot.getName();
            if ( values.containsKey(slotName) && values.get(slotName).size() == 1
                    && !values.get(slotName).contains(null) ) {
                // Slot is condensable
                Object newValue = values.get(slot.getName()).iterator().next();
                if ( value == null ) {
                    // No value at the set level so we can condense.
                    slot.setValue(ms, newValue);
                    condensedSlots.add(slot.getName());
                } else if ( value.equals(newValue) ) {
                    // The set value is already the same as the value in all mappings, so we can
                    // just mark the slot as being condensed without having to do anything.
                    condensedSlots.add(slot.getName());
                } else if ( policy == PropagationPolicy.AlwaysReplace ) {
                    // Policy says to condense regardless of what may already exist.
                    slot.setValue(ms, newValue);
                    condensedSlots.add(slot.getName());
                }
            } else if ( policy == PropagationPolicy.AlwaysReplace ) {
                // Slot is not condensable, remove useless conflicting value at the set level
                slot.setValue(ms, null);
            }
            return null;
        };
        setHelper.visitSlots(mappingSet, v, true);

        if ( !preserve ) {
            // Remove the values of condensed slots from the individual mappings.
            mappingHelper.setSlots(new ArrayList<String>(condensedSlots), false);
            NullifyVisitor<Mapping> nullify = new NullifyVisitor<Mapping>();
            for ( Mapping mapping : mappingSet.getMappings() ) {
                mappingHelper.visitSlots(mapping, nullify, true);
            }
        }

        return condensedSlots;
    }

    private class NullifyVisitor<T> extends SlotVisitorBase<T> {

        @Override
        public void visit(Slot<T> slot, T object, Object value) {
            slot.setValue(object, (Object) null);
        }

    }
}
