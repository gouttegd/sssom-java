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

package org.incenp.obofoundry.sssom.slots;

/**
 * A visitor interface to visit the different metadata slot of a SSSOM Java
 * object. This is similar to the {@link ISlotVisitor} interface but with a
 * single method that does not distinguish between slot types, but has the
 * benefit (since it defines only one method) that it can be implemented as a
 * lambda.
 * 
 * @param <T> The SSSOM object whose slots this visitor will visit.
 * @param <V> The type of objects the visitor returns upon visiting a slot.
 */
public interface ISimpleSlotVisitor<T, V> {

    /**
     * Visits a SSSOM slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return once the slot has been
     *         visited.
     */
    public V visit(Slot<T> slot, T object, Object value);
}
