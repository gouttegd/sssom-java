/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.incenp.obofoundry.sssom.model.ExtensionValue;

/**
 * Helper methods for SSSOM.
 * <p>
 * This class is intended to group various static methods that may be used
 * throughout the library.
 */
public class SSSOMUtils {

    private static DecimalFormat doubleFormatter;

    static {
        doubleFormatter = new DecimalFormat("#.###");
        doubleFormatter.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * Formats a double value.
     * <p>
     * In an attempt to reduce serialisation differences across implementations, the
     * SSSOM specification, and especially the specification for the “canonical
     * SSSOM/TSV format”, recommends that values of double-typed slots (e.g.
     * {@code confidence}) be written with up to three digits after the decimal
     * point as needed, rounding to the nearest neighbour (rounding up if both
     * neighbours are equidistant). This is what this method does.
     * 
     * @param value The double value to format as a string.
     * @return A string serialisation of the value, compliant with the format
     *         recommendations from the SSSOM specification.
     */
    public static String format(Double value) {
        return doubleFormatter.format(value);
    }

    /**
     * Formats a date value.
     * <p>
     * The SSSOM specification says nothing on how to serialise date-typed slots,
     * but LinkML says “for xsd dates, datetimes, and times, [the value] must be a
     * string conforming to the relevant ISO type”. Presumably this means ISO-8601.
     * 
     * @param value The date value to format as a string.
     * @return A string serialisation of the value.
     */
    public static String format(LocalDate value) {
        return value.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * Formats a date and time value.
     * <p>
     * There are no date-time slots in SSSOM, but SSSOM-Java supports extension
     * values with such a type.
     * 
     * @param value The date-time value to format.
     * @return A string serialisation of the value.
     */
    public static String format(ZonedDateTime value) {
        return value.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Formats a value of an unknown type.
     * <p>
     * This is a convenience method to format an object whose type is not known at
     * compile-time. It merely checks the runtime type of the object then calls the
     * appropriate formatting method.
     * 
     * @param value The object to format.
     * @return A string serialisation of the value (or {@code null} if the value
     *         itself is {@code null}).
     */
    public static String format(Object value) {
        if ( value == null ) {
            return null;
        }

        Class<?> valueType = value.getClass();
        if ( valueType == Double.class ) {
            return format((Double) value);
        } else if ( valueType == LocalDate.class ) {
            return format((LocalDate) value);
        } else if ( valueType == ExtensionValue.class ) {
            return ((ExtensionValue) value).toString();
        } else {
            return value.toString();
        }
    }
}
