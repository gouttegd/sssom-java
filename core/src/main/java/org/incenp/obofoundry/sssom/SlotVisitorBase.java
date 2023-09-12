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

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

/**
 * A default implementation of the {@link SlotVisitor} interface that returns
 * {@code null} for every slot. Client code can extend this class and override
 * only the method they need instead of implementing {@link SlotVisitor}
 * directly.
 * 
 * @param <T> The SSSOM object whose slots this visitor will visit.
 * @param <V> The type of objects the methods of this visitor will return.
 */
public class SlotVisitorBase<T, V> implements SlotVisitor<T, V> {

    /**
     * Gets a default value for non-implemented visitor methods.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     * @return Whatever value the visitor wishes to return.
     */
    protected V getDefault(Slot<T> slot, T object, Object value) {
        return null;
    }

    @Override
    public V visit(Slot<T> slot, T object, String value) {
        return getDefault(slot, object, value);
    }

    @Override
    public V visit(Slot<T> slot, T object, List<String> values) {
        return getDefault(slot, object, values);
    }

    @Override
    public V visit(Slot<T> slot, T object, Double value) {
        return getDefault(slot, object, value);
    }

    @Override
    public V visit(Slot<T> slot, T object, Map<String, String> values) {
        return getDefault(slot, object, values);
    }

    @Override
    public V visit(Slot<T> slot, T object, LocalDate value) {
        return getDefault(slot, object, value);
    }

    @Override
    public V visit(Slot<T> slot, T object, EntityType value) {
        return getDefault(slot, object, value);
    }

    @Override
    public V visit(Slot<T> slot, T object, MappingCardinality value) {
        return getDefault(slot, object, value);
    }

    @Override
    public V visit(Slot<T> slot, T object, PredicateModifier value) {
        return getDefault(slot, object, value);
    }
}
