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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.EntityReference;
import org.incenp.obofoundry.sssom.model.Propagatable;
import org.incenp.obofoundry.sssom.model.SlotURI;
import org.incenp.obofoundry.sssom.model.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a metadata slot on a SSSOM object.
 * 
 * @param <T> The type of SSSOM object (e.g. Mapping, MappingSet, etc.).
 */
public class Slot<T> {
    private Field field;
    private String name;
    private boolean entity;

    /**
     * Creates a new instance.
     * 
     * @param type      The type of SSSOM object the slot is associated with,
     * @param fieldName The name of the Java field that store the slot's data in the
     *                  object.
     */
    public Slot(Class<T> type, String fieldName) {
        try {
            field = type.getDeclaredField(fieldName);
        } catch ( NoSuchFieldException e ) {
            throw new IllegalArgumentException(String.format("Invalid field name: %s", fieldName));
        }

        JsonProperty jsonAnnotation = field.getDeclaredAnnotation(JsonProperty.class);
        name = jsonAnnotation != null ? jsonAnnotation.value() : field.getName();

        entity = field.isAnnotationPresent(EntityReference.class);
    }

    /**
     * Gets the name of the slot as it appears in the SSSOM specification (e.g.,
     * {@code mapping_justification}, {@code subject_id}, etc.).
     * 
     * @return The slot name as per the SSSOM specification, independently of the
     *         implementation.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the URI of the slot.
     * <p>
     * For most slots, this is simply the slot’s name appended to the SSSOM IRI
     * prefix (e.g. {@code https://w3id.org/sssom/subject_label}), but some slots
     * use a URI borrowed from another specification instead (for example, the URI
     * for the {@code creator_id} is {@code http://purl.org/dc/terms/creator}).
     * 
     * @return The URI associated with the slot.
     */
    public String getURI() {
        SlotURI uriAnnotation = field.getDeclaredAnnotation(SlotURI.class);
        return uriAnnotation != null ? uriAnnotation.value() : BuiltinPrefix.SSSOM.getPrefix() + getName();
    }

    /**
     * Indicates whether the slot is intended to hold an entity reference. Entity
     * references are represented as strings but need to be treated differently.
     * 
     * @return {@code true} is the slot is intended to hold an entity reference,
     *         otherwise {@code false}.
     */
    public boolean isEntityReference() {
        return entity;
    }

    /**
     * Indicates whether the slot is a "propagatable slot".
     * 
     * @return {@code true} if the slot can be propagated, otherwise {@code false}.
     */
    public boolean isPropagatable() {
        return field.isAnnotationPresent(Propagatable.class);
    }

    /**
     * Indicates whether the slot is expected to contain a URI.
     * 
     * @return {@code true} if the slot is defined as having a URI range, otherwise
     *         {@code false}.
     */
    public boolean isURI() {
        return field.isAnnotationPresent(URI.class);
    }

    /**
     * Gets the underlying Java type for the slot.
     * 
     * @return The Java data type used to store the slot's data.
     */
    public Class<?> getType() {
        return field.getType();
    }

    /**
     * Gets the value of the slot for a given object.
     * 
     * @param object The object (e.g. a mapping or a mapping set) from which to
     *               retrieve the value.
     * @return The value of the slot in that object.
     */
    public Object getValue(T object) {
        String fieldName = field.getName();
        String accessorName = String.format("get%c%s", Character.toUpperCase(fieldName.charAt(0)),
                fieldName.substring(1));
        try {
            Method accessor = object.getClass().getDeclaredMethod(accessorName, (Class<?>[]) null);
            return accessor.invoke(object, (Object[]) null);
        } catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e ) {
            // Should never happen
        }
        return null;
    }

    /**
     * Sets the value of the slot for a given object.
     * 
     * @param object The object (e.g. a mapping or a mapping set) for which the slot
     *               should be set.
     * @param value  The value to assign to the slot on the given object.
     */
    public void setValue(T object, Object value) {
        String fieldName = field.getName();
        String accessorName = String.format("set%c%s", Character.toUpperCase(fieldName.charAt(0)),
                fieldName.substring(1));
        try {
            Method accessor = object.getClass().getDeclaredMethod(accessorName, new Class<?>[] { field.getType() });
            accessor.invoke(object, new Object[] { field.getType().cast(value) });
        } catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e ) {
            // Should never happen
        } catch ( InvocationTargetException e ) {
            if ( e.getCause() instanceof IllegalArgumentException ) {
                throw IllegalArgumentException.class.cast(e.getCause());
            }
            // Should never happen (IAE is the only exception thrown by setters)
        }
    }
}
