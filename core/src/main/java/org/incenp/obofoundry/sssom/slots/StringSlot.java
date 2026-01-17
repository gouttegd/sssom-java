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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a metadata slot that is backed by a String-typed field.
 * 
 * @param <T> The type of SSSOM object the slot is associated with.
 */
public class StringSlot<T> extends Slot<T> {

    /**
     * Creates a new instance.
     * 
     * @param field The Java field that store the slot's data in a SSSOM object.
     */
    StringSlot(Field field) {
        super(field);
    }

    /**
     * Indicates whether the slot holds a list of values rather than a single value.
     * 
     * @return {@code true} is the slot is multivalued, otherwise {@code false}.
     */
    public boolean isMultivalued() {
        return getType().equals(List.class);
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

    @Override
    public void setValue(T object, String value) {
        if ( isMultivalued() && value != null ) {
            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) getValue(object);
            if ( values == null ) {
                values = new ArrayList<String>();
                super.setValue(object, values);
            }
            values.add(value);
        } else {
            super.setValue(object, (Object) value);
        }
    }
}
