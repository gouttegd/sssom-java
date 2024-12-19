/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.slots;

import java.lang.reflect.Field;
import java.util.List;

import org.incenp.obofoundry.sssom.ISlotVisitor;

/**
 * Represents a metadata slot that is intended to hold an entity reference
 * (which is internally represented by a String).
 * 
 * @param <T> The type of SSSOM object the slot is associated with.
 */
public class EntityReferenceSlot<T> extends StringSlot<T> {

    /**
     * Creates a new instance.
     * 
     * @param field The Java field that store the slot's data in a SSSOM object.
     */
    public EntityReferenceSlot(Field field) {
        super(field);
    }

    @Override
    public void accept(ISlotVisitor<T> visitor, T target, Object value) {
        if ( isMultivalued() ) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            visitor.visit(this, target, list);
        } else {
            visitor.visit(this, target, (String) value);
        }
    }
}
