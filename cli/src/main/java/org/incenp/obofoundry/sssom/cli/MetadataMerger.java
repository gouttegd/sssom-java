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

package org.incenp.obofoundry.sssom.cli;

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
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;

/**
 * Helper class to merge set-level metadata.
 */
public class MetadataMerger extends SlotVisitorBase<MappingSet> {

    private MappingSet internal;

    /**
     * Merges the set-level metadata of two mapping sets. Only multi-valued metadata
     * slots will be merged.
     * 
     * @param dst The set that will receive the merged metadata.
     * @param src The set containing the metadata to merge into the first one.
     */
    public void merge(MappingSet dst, MappingSet src) {
        internal = dst;
        SlotHelper.getMappingSetHelper().visitSlots(src, this);
    }

    @Override
    public void visit(StringSlot<MappingSet> slot, MappingSet object, List<String> values) {
        @SuppressWarnings("unchecked")
        List<String> dstValues = (List<String>) slot.getValue(internal);
        if ( dstValues == null ) {
            slot.setValue(internal, values);
        } else {
            // Avoid duplicated values (would going through a HashSet be more efficient?)
            for ( String value : values ) {
                if ( !dstValues.contains(value) ) {
                    dstValues.add(value);
                }
            }
        }
    }

    @Override
    public void visit(CurieMapSlot<MappingSet> slot, MappingSet object, Map<String, String> values) {
        @SuppressWarnings("unchecked")
        Map<String, String> dstValues = (Map<String, String>) slot.getValue(internal);
        if ( dstValues == null ) {
            slot.setValue(internal, values);
        } else {
            // Values from the source set takes precedence
            dstValues.putAll(values);
        }
    }

    @Override
    public void visit(ExtensionDefinitionSlot<MappingSet> slot, MappingSet object, List<ExtensionDefinition> values) {
        List<ExtensionDefinition> dstValues = internal.getExtensionDefinitions();
        if ( dstValues == null ) {
            internal.setExtensionDefinitions(values);
        } else {
            /*
             * Merge definitions from both sets.
             * 
             * Possible clashes:
             * 
             * 1. Same slot name, different property: craft a new slot name for the second
             * definition;
             * 
             * 2. Same property, different slot name: slot name from the second definition
             * takes precedence;
             * 
             * 3. Same property, different type: coerce to xsd:string.
             */
            Map<String, ExtensionDefinition> definitionsByProperty = new HashMap<String, ExtensionDefinition>();
            Set<String> names = new HashSet<String>();
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
                        // 3. Same property, conflicting types
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
                    internal.getExtensionDefinitions().add(def);
                }
            }
        }
    }

    @Override
    public void visit(ExtensionSlot<MappingSet> slot, MappingSet object, Map<String, ExtensionValue> values) {
        Map<String, ExtensionValue> dstValues = internal.getExtensions();
        if ( dstValues == null ) {
            internal.setExtensions(values);
        } else {
            // Values from the source set take precedence
            dstValues.putAll(values);
        }
    }
}
