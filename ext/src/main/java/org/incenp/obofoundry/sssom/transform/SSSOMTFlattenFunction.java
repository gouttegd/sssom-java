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

package org.incenp.obofoundry.sssom.transform;

import java.util.List;

/**
 * Represents the SSSOM/T modifier function "flatten".
 * <p>
 * Use this function to format a list-typed value into a single string. It
 * accepts up to three arguments, all optiona:
 * <ul>
 * <li>the separator to insert between each item (defaults to <code>, </code>);
 * <li>a marker to insert at the beginning at the list (defaults to the empty
 * string);
 * <li>a marker to insert at the end of the list (defaults to the empty string).
 * </ul>
 * <p>
 * For example, to format the list of authors as a bracket-enclosed,
 * space-separated list:
 * 
 * <pre>
 * "Authors: %{author_label|flatten(' ', '[', ']')}"
 * </pre>
 */
public class SSSOMTFlattenFunction implements IFormatModifierFunction {

    @Override
    public String getName() {
        return "flatten";
    }

    @Override
    public String getSignature() {
        return "S?S?S?";
    }

    @Override
    public Object call(Object value, List<String> extra) {
        if ( !List.class.isInstance(value) ) {
            return value;
        }

        @SuppressWarnings("unchecked")
        List<String> valueAsList = List.class.cast(value);
        int n = extra.size();
        String inter = n >= 1 ? extra.get(0) : ", ";
        String start = n >= 2 ? extra.get(1) : "";
        String end = n >= 3 ? extra.get(2) : "";

        return start + String.join(inter, valueAsList) + end;
    }

}
