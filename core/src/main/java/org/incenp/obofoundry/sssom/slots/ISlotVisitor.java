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
import org.incenp.obofoundry.sssom.model.Version;

/**
 * A visitor interface to visit the different types of metadata slots of a SSSOM
 * object.
 * 
 * @param <T> The SSSOM object whose slots this visitor is to visit.
 */
public interface ISlotVisitor<T> {

    /**
     * Visits a generic slot.
     * <p>
     * This method is normally not used as all slots in a SSSOM object have more
     * precise types. But it can be used to implement behaviours that should be
     * common to several slot types.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(Slot<T> slot, T object, Object value);

    /**
     * Visits a string-typed slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(StringSlot<T> slot, T object, String value);

    /**
     * Visits a multi-valued string-typed slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param values The values of the slot.
     */
    public void visit(StringSlot<T> slot, T object, List<String> values);

    /**
     * Visits a URI-typed slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(URISlot<T> slot, T object, String value);

    /**
     * Visits a multi-valued URI-typed slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param values The values of the slot.
     */
    public void visit(URISlot<T> slot, T object, List<String> values);

    /**
     * Visits a slot that holds an entity reference.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(EntityReferenceSlot<T> slot, T object, String value);

    /**
     * Visits a multi-valued slot that holds entity references.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param values The values of the slot.
     */
    public void visit(EntityReferenceSlot<T> slot, T object, List<String> values);

    /**
     * Visits a double-typed slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(DoubleSlot<T> slot, T object, Double value);

    /**
     * Visits a date-typed slot.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(DateSlot<T> slot, T object, LocalDate value);

    /**
     * Visits a slot that holds an entity type enumeration value.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(EntityTypeSlot<T> slot, T object, EntityType value);

    /**
     * Visits a slot that holds a mapping cardinality enumeration value.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value);

    /**
     * Visits a slot that holds a predicate modifier enumeration value.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(PredicateModifierSlot<T> slot, T object, PredicateModifier value);

    /**
     * Visits a slot that holds a SSSOM version value.
     * 
     * @param slot    The slot that is being visited.
     * @param object  The object to which the slot is attached.
     * @param version The value of the slot.
     */
    default public void visit(VersionSlot<T> slot, T object, Version version) {
    }

    /**
     * Visits a slot that holds a curie map.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param value  The value of the slot.
     */
    public void visit(CurieMapSlot<T> slot, T object, Map<String, String> value);

    /**
     * Visits a slot that holds a list of extension definitions for non-standard
     * metadata.
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param values The values of the slot.
     */
    public void visit(ExtensionDefinitionSlot<T> slot, T object, List<ExtensionDefinition> values);

    /**
     * Visits a slot that holds extension values (non-standard metadata).
     * 
     * @param slot   The slot that is being visited.
     * @param object The object to which the slot is attached.
     * @param values The values of the slot.
     */
    public void visit(ExtensionSlot<T> slot, T object, Map<String, ExtensionValue> values);
}
