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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A pseudo-visitor interface to visit the different metadata slots of a SSSOM
 * Java object.
 * 
 * @param <T> The SSSOM object whose slots this visitor will visit.
 * @param <V> The type of objects the methods of this visitor will return.
 */
public interface SlotVisitor<T, V> {

    /**
     * Visits a slot of type text (this includes slots intended to hold entity
     * references).
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    public V visit(Slot<T> slot, T object, String value);

    /**
     * Visits a slot that is a list of text values (this includes slots intended to
     * hold a list of entity references).
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    public V visit(Slot<T> slot, T object, List<String> value);

    /**
     * Visits a slot of type double.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    public V visit(Slot<T> slot, T object, Double value);

    /**
     * Visits a slot that is a string-to-string map.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    public V visit(Slot<T> slot, T object, Map<String, String> value);

    /**
     * Visits a slot of type date.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    public V visit(Slot<T> slot, T object, LocalDate value);

    /**
     * Visits a slot that has a enum value. Implementations should consult the
     * {@code slot} parameter to figure out the type of the enum, if needed.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    public V visit(Slot<T> slot, T object, Object value);
}
