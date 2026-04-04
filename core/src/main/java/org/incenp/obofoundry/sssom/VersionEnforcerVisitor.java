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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;

/**
 * Helper object to forcibly remove from a set or a mapping all slots or slot
 * values that are incompatible with a given version of the SSSOM specification.
 * 
 * @param <T> The type of object to visit (<code>Mapping</code> or
 *            <code>MappingSet</code>).
 */
public class VersionEnforcerVisitor<T> extends SlotVisitorBase<T> {

    private Version targetVersion = Version.LATEST;

    /**
     * Creates a new instance.
     * 
     * @param version The version of the SSSOM specification to be enforced.
     */
    public VersionEnforcerVisitor(Version version) {
        targetVersion = version;
    }

    @Override
    public void visit(Slot<T> slot, T object, Object unused) {
        if ( !slot.getCompliantVersion().isCompatibleWith(targetVersion) ) {
            slot.setValue(object, (Object) null);
        }
    }

    @Override
    public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
        if ( !slot.getCompliantVersion().isCompatibleWith(targetVersion)
                || (targetVersion == Version.SSSOM_1_0 && value == EntityType.COMPOSED_ENTITY_EXPRESSION) ) {
            slot.setValue(object, (Object) null);
        }
    }

    @Override
    public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value) {
        if ( targetVersion == Version.SSSOM_1_0 && value == MappingCardinality.NONE_TO_NONE ) {
            slot.setValue(object, (Object) null);
        }
    }
}
