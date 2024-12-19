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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.incenp.obofoundry.sssom.ISlotVisitor;
import org.incenp.obofoundry.sssom.Slot;

/**
 * Represents a metadata slot that holds a date.
 * 
 * @param <T> The type of SSSOM object the slot is associated with.
 */
public class DateSlot<T> extends Slot<T> {

    /**
     * Creates a new instance.
     * 
     * @param field The Java field that store the slot's data in a SSSOM object.
     */
    public DateSlot(Field field) {
        super(field);
    }

    @Override
    public void accept(ISlotVisitor<T> visitor, T target, Object value) {
        visitor.visit(this, target, (LocalDate) value);
    }

    /**
     * Parses a string value into a date and assigns it to the slot for the given
     * object.
     * <p>
     * If the string represents a <em>datetime</em> rather than just a
     * <em>date</em>, the time part is ignored.
     * 
     * @throws DateTimeParseException If the given string does not contain a valid
     *                                date or datetime.
     */
    @Override
    public void setValue(T object, String value) {
        LocalDate date = null;
        if ( value != null ) {
            if ( value.contains("T") ) {
                date = LocalDateTime.parse(value).toLocalDate();
            } else {
                date = LocalDate.parse(value);
            }
        }
        super.setValue(object, date);
    }
}
