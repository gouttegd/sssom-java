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

package org.incenp.obofoundry.sssom.slots;

import java.lang.reflect.Field;

import org.incenp.obofoundry.sssom.model.Version;

/**
 * Represents a metadata slot that holds a SSSOM version.
 * 
 * @param <T> The type of SSSOM object the slot is associated with. Of note,
 *            currently, only the MappingSet object is defined to have a slot of
 *            that type.
 */
public class VersionSlot<T> extends Slot<T> {

    /**
     * Creates a new instance.
     * 
     * @param field The Java field that store the slot's data in a SSSOM object.
     */
    VersionSlot(Field field) {
        super(field);
    }

    @Override
    public void accept(ISlotVisitor<T> visitor, T target, Object value) {
        visitor.visit(this, target, (Version) value);
    }

    /**
     * Parses a string value into a SSSOM version and assigns it to the slot for the
     * given object.
     * <p>
     * If the string does not correspond to a recognised SSSOM version, the slot is
     * assigned the special value {@link Version#UNKNOWN}.
     */
    @Override
    public void setValue(T object, String value) {
        Version version = null;
        if ( value != null ) {
            version = Version.fromIRI(value);
            if ( version == Version.UNKNOWN ) {
                version = Version.fromString(value);
            }
        }
        super.setValue(object, version);
    }

}
