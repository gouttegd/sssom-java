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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.incenp.obofoundry.sssom.SSSOMUtils;

/**
 * Represents the value of an extension slot.
 */
public class ExtensionValue {
    private Object value;
    private ValueType valueType;

    /**
     * Creates a new integer-typed value.
     * 
     * @param i The value.
     */
    public ExtensionValue(int i) {
        value = i;
        valueType = ValueType.INTEGER;
    }

    /**
     * Creates a new double-typed value.
     * 
     * @param d The value.
     */
    public ExtensionValue(double d) {
        value = d;
        valueType = ValueType.DOUBLE;
    }

    /**
     * Creates a new boolean-typed value.
     * 
     * @param b The value.
     */
    public ExtensionValue(boolean b) {
        value = b;
        valueType = ValueType.BOOLEAN;
    }

    /**
     * Creates a new value representing a date.
     * 
     * @param date The value.
     */
    public ExtensionValue(LocalDate date) {
        value = date;
        valueType = ValueType.DATE;
    }

    /**
     * Creates a new value representing a date and time.
     * 
     * @param time The value.
     */
    public ExtensionValue(ZonedDateTime time) {
        value = time;
        valueType = ValueType.DATETIME;
    }

    /**
     * Creates a new string-typed value.
     * 
     * @param s The value.
     */
    public ExtensionValue(String s) {
        value = s;
        valueType = ValueType.STRING;
    }

    /**
     * Creates a new string-based value that may be an identifier.
     * 
     * @param s            The value.
     * @param isIdentifier If {@code true}, the value is treated as an identifier;
     *                     otherwise it is treated as a normal string.
     */
    public ExtensionValue(String s, boolean isIdentifier) {
        value = s;
        valueType = isIdentifier ? ValueType.IDENTIFIER : ValueType.STRING;
    }

    /**
     * Creates a new value with an unknown type.
     * 
     * @param o The value.
     */
    public ExtensionValue(Object o) {
        value = o;
        valueType = ValueType.OTHER;
    }

    @Override
    public String toString() {
        switch ( valueType ) {
        case DOUBLE:
            return SSSOMUtils.format((Double) value);

        case DATE:
            return SSSOMUtils.format((LocalDate) value);

        case DATETIME:
            return SSSOMUtils.format((ZonedDateTime) value);

        default:
            return value.toString();
        }
    }

    /**
     * @return The type of the value.
     */
    public ValueType getType() {
        return valueType;
    }

    /**
     * @return The actual value, as an object of unknown type.
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return {@code true} if the value is typed as an integer.
     */
    public boolean isInteger() {
        return valueType == ValueType.INTEGER;
    }

    /**
     * @return {@code true} if the value is typed as a double.
     */
    public boolean isDouble() {
        return valueType == ValueType.DOUBLE;
    }

    /**
     * @return {@code true} if the value is typed as a boolean.
     */
    public boolean isBoolean() {
        return valueType == ValueType.BOOLEAN;
    }

    /**
     * @return {@code true} if the value is typed as a date.
     */
    public boolean isDate() {
        return valueType == ValueType.DATE;
    }

    /**
     * @return {@code true} if the value is typed as a date and time object.
     */
    public boolean isDatetime() {
        return valueType == ValueType.DATETIME;
    }

    /**
     * @return {@code true} if the value is typed as an identifier.
     */
    public boolean isIdentifier() {
        return valueType == ValueType.IDENTIFIER;
    }

    /**
     * @return {@code true} if the value is typed as a string.
     */
    public boolean isString() {
        return valueType == ValueType.STRING;
    }

    /**
     * @return The value as an integer, or {@code null} is the value has another
     *         type.
     */
    public Integer asInteger() {
        return valueType == ValueType.INTEGER ? Integer.class.cast(value) : null;
    }

    /**
     * @return The value as a double, or {@code null} is the value has another type.
     */
    public Double asDouble() {
        return valueType == ValueType.DOUBLE ? Double.class.cast(value) : null;
    }

    /**
     * @return The value as a boolean, or {@code null} is the value has another
     *         type.
     */
    public Boolean asBoolean() {
        return valueType == ValueType.BOOLEAN ? Boolean.class.cast(value) : null;
    }

    /**
     * @return The value as a date, or {@code null} is the value has another type.
     */
    public LocalDate asDate() {
        return valueType == ValueType.DATE ? LocalDate.class.cast(value) : null;
    }

    /**
     * @return The value as a date and time object, or {@code null} is the value has
     *         another type.
     */
    public ZonedDateTime asDatetime() {
        return valueType == ValueType.DATETIME ? ZonedDateTime.class.cast(value) : null;
    }

    /**
     * @return The value as a string, or {@code null} is the value is not typed as a
     *         string or an identifier.
     */
    public String asString() {
        return valueType == ValueType.STRING || valueType == ValueType.IDENTIFIER ? String.class.cast(value) : null;
    }
}
