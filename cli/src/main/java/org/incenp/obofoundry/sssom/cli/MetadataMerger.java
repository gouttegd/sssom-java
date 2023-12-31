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

package org.incenp.obofoundry.sssom.cli;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.SlotVisitorBase;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Helper class to merge set-level metadata.
 */
public class MetadataMerger extends SlotVisitorBase<MappingSet, Void> {

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
    public Void visit(Slot<MappingSet> slot, MappingSet object, List<String> values) {
        @SuppressWarnings("unchecked")
        List<String> dstValues = (List<String>) slot.getValue(internal);
        if ( dstValues == null ) {
            slot.setValue(internal, values);
        } else {
            dstValues.addAll(values);
        }
        return null;
    }

    @Override
    public Void visit(Slot<MappingSet> slot, MappingSet object, Map<String, String> values) {
        @SuppressWarnings("unchecked")
        Map<String, String> dstValues = (Map<String, String>) slot.getValue(internal);
        if ( dstValues == null ) {
            slot.setValue(internal, values);
        } else {
            dstValues.putAll(values);
        }
        return null;
    }
}
