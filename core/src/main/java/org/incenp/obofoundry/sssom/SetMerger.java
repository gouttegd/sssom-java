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

package org.incenp.obofoundry.sssom;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.CurieMapSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionDefinitionSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;

/**
 * Helper object to merge one mapping set into another.
 */
public class SetMerger extends SlotVisitorBase<MappingSet> {

    private EnumSet<MergeOption> options = MergeOption.DEFAULT.clone();
    private MappingSet internal;

    /**
     * Sets the options to configure the behaviour of this object. This replaces all
     * pre-existing options.
     * 
     * @param options The new set of options.
     */
    public void setMergeOptions(EnumSet<MergeOption> options) {
        this.options.addAll(options);
    }

    /**
     * Gets the options that define the behaviour of this object. The returned set
     * may be modified to change the behaviour.
     * 
     * @return The set of options currently used by this object.
     */
    public EnumSet<MergeOption> getMergeOptions() {
        return options;
    }

    /**
     * Merges one set into another, according to the current configuration.
     * 
     * @param dst The destination set, in which the source set will be merged.
     * @param src The source set to merge into the destination set.
     */
    public void merge(MappingSet dst, MappingSet src) {
        internal = dst;
        if ( options.contains(MergeOption.MERGE_MAPPINGS) ) {
            internal.getMappings(true).addAll(src.getMappings(true));
        }

        SlotHelper.getMappingSetHelper().visitSlots(src, this);
    }

    @Override
    public void visit(Slot<MappingSet> slot, MappingSet src, Object value) {
        if ( options.contains(MergeOption.MERGE_SCALARS) ) {
            slot.setValue(internal, value);
        }
    }

    @Override
    public void visit(StringSlot<MappingSet> slot, MappingSet src, List<String> values) {
        if ( options.contains(MergeOption.MERGE_LISTS) ) {
            @SuppressWarnings("unchecked")
            List<String> dstValues = (List<String>) slot.getValue(internal);
            if ( dstValues == null ) {
                slot.setValue(internal, values);
            } else {
                for ( String value : values ) {
                    if ( !dstValues.contains(value) ) {
                        dstValues.add(value);
                    }
                }
            }
        }
    }

    @Override
    public void visit(CurieMapSlot<MappingSet> slot, MappingSet src, Map<String, String> values) {
        if ( options.contains(MergeOption.MERGE_CURIE_MAP) ) {
            internal.getCurieMap(true).putAll(values);
        }
    }

    @Override
    public void visit(ExtensionSlot<MappingSet> slot, MappingSet src, Map<String, ExtensionValue> values) {
        if ( options.contains(MergeOption.MERGE_EXTENSIONS) ) {
            internal.getExtensions(true).putAll(values);
        }
    }

    @Override
    public void visit(ExtensionDefinitionSlot<MappingSet> slot, MappingSet object, List<ExtensionDefinition> values) {
        /*
         * We need to merge extension definitions if:
         * 
         * - we merge extensions (in case the source set contains extensions, we need
         * their definitions);
         * 
         * - we merge mappings (in case the mappings from the source set contain
         * extension, we also need their definitions).
         */
        if ( !options.contains(MergeOption.MERGE_EXTENSIONS) && !options.contains(MergeOption.MERGE_MAPPINGS) ) {
            return;
        }

        List<ExtensionDefinition> dstValues = internal.getExtensionDefinitions(true);

        /*
         * Merge definitions from both sets.
         * 
         * Possible clashes:
         * 
         * 1. Same slot name, different property: craft a new slot name for the second
         * definition;
         * 
         * 2. Same property, different slot name: slot name from second definition takes
         * precedence;
         * 
         * 3. Same property, different type: coerce to xsd:string.
         */
        Map<String, ExtensionDefinition> definitionsByProperty = new HashMap<>();
        Set<String> names = new HashSet<>();
        for ( ExtensionDefinition def : dstValues ) {
            definitionsByProperty.put(def.getProperty(), def);
            names.add(def.getSlotName());
        }

        for ( ExtensionDefinition def : values ) {
            ExtensionDefinition existingDef = definitionsByProperty.get(def.getProperty());
            if ( existingDef != null ) {
                if ( !existingDef.getSlotName().equals(def.getSlotName()) ) {
                    // 2. Same property, different slot name
                    existingDef.setSlotName(def.getSlotName());
                }
                if ( !existingDef.getTypeHint().equals(def.getTypeHint()) ) {
                    // 3. Same property, conflicting type
                    existingDef.setTypeHint(null);
                }
            } else {
                String name = def.getSlotName();
                if ( names.contains(name) ) {
                    // 1. Slot name clash: prior definition with same slot name, different property
                    int n = 1;
                    String fixedName;
                    do {
                        fixedName = String.format("%s_%d", name, ++n);
                    } while ( names.contains(fixedName) );

                    def = new ExtensionDefinition(fixedName, def.getProperty(), def.getTypeHint());
                    names.add(fixedName);
                }
                dstValues.add(def);
            }
        }
    }
}
