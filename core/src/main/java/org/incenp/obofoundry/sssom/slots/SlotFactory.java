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
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.model.EntityReference;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.model.URI;

/**
 * A class that provides helper methods to create {@code Slot} instances.
 */
public class SlotFactory {

    /**
     * Creates a Slot object from a Java field.
     * 
     * @param <T>   The type of SSSOM object the slot to create is to be associated
     *              with.
     * @param type  The Java type
     * @param field
     * @return
     */

    /**
     * Creates a Slot object from a Java field.
     * 
     * @param <T>   The type of SSSOM object the slot to create is to be associated
     *              with.
     * @param field The Java field that stores the slot's data.
     * @return The newly created Slot object. May be {@code null} if the field does
     *         not correspond to a metadata slot (currently this is only the case
     *         for the {@code mappings} field of the MappingSet object.
     */
    public static <T> Slot<T> fromField(Field field) {
        if ( field.isAnnotationPresent(EntityReference.class) ) {
            return new EntityReferenceSlot<T>(field);
        } else if ( field.isAnnotationPresent(URI.class) ) {
            return new URISlot<T>(field);
        }

        Class<?> javaType = field.getType();
        String name = field.getName();
        if ( javaType.equals(String.class) ) {
            return new StringSlot<T>(field);
        } else if ( javaType.equals(Double.class) ) {
            return new DoubleSlot<T>(field);
        } else if ( javaType.equals(LocalDate.class) ) {
            return new DateSlot<T>(field);
        } else if ( javaType.equals(EntityType.class) ) {
            return new EntityTypeSlot<T>(field);
        } else if ( javaType.equals(MappingCardinality.class) ) {
            return new MappingCardinalitySlot<T>(field);
        } else if ( javaType.equals(PredicateModifier.class) ) {
            return new PredicateModifierSlot<T>(field);
        } else if ( javaType.equals(List.class) ) {
            if ( name.equals("extensionDefinitions") ) {
                return new ExtensionDefinitionSlot<T>(field);
            } else if ( !name.equals("mappings") ) {
                return new StringSlot<T>(field);
            }
        } else if ( javaType.equals(Map.class) ) {
            if ( name.equals("curieMap") ) {
                return new CurieMapSlot<T>(field);
            } else if ( name.equals("extensions") ) {
                return new ExtensionSlot<T>(field);
            }
        }

        // The field is not a field that holds a metadata slot.
        return null;
    }
}
