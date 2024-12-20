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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

/**
 * A default implementation of the {@link ISlotVisitor} interface that does
 * nothing for every slot type. Client code can extend this class and override
 * only the method they need instead of implementing {@link ISlotVisitor}
 * directly.
 * 
 * @param <T> The SSSOM object whose slots this visitor will visit.
 */
public class SlotVisitorBase<T> implements ISlotVisitor<T> {

    @Override
    public void visit(Slot<T> slot, T object, Object value) {
    }

    @Override
    public void visit(StringSlot<T> slot, T object, String value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(StringSlot<T> slot, T object, List<String> values) {
        visit((Slot<T>) slot, object, values);
    }

    @Override
    public void visit(URISlot<T> slot, T object, String value) {
        visit((StringSlot<T>) slot, object, value);
    }

    @Override
    public void visit(URISlot<T> slot, T object, List<String> values) {
        visit((StringSlot<T>) slot, object, values);
    }

    @Override
    public void visit(EntityReferenceSlot<T> slot, T object, String value) {
        visit((StringSlot<T>) slot, object, value);
    }

    @Override
    public void visit(EntityReferenceSlot<T> slot, T object, List<String> values) {
        visit((StringSlot<T>) slot, object, values);
    }

    @Override
    public void visit(DoubleSlot<T> slot, T object, Double value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(DateSlot<T> slot, T object, LocalDate value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(PredicateModifierSlot<T> slot, T object, PredicateModifier value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(CurieMapSlot<T> slot, T object, Map<String, String> value) {
        visit((Slot<T>) slot, object, value);
    }

    @Override
    public void visit(ExtensionDefinitionSlot<T> slot, T object, List<ExtensionDefinition> values) {
        visit((Slot<T>) slot, object, values);
    }

    @Override
    public void visit(ExtensionSlot<T> slot, T object, Map<String, ExtensionValue> value) {
        visit((Slot<T>) slot, object, value);
    }
}
