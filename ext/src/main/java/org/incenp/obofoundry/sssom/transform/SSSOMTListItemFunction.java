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
 * Represents the SSSOM/T modifier function "list_item".
 * <p>
 * Use this function to extract a single item from a list-typed value. It
 * accepts a single argument which should be 1-based index of the item to
 * extract.
 * <p>
 * For example, to get the second author ID of a mapping:
 * 
 * <pre>
 * "Second author: %{author_id|list_item(2)}"
 * </pre>
 */
public class SSSOMTListItemFunction implements IFormatModifierFunction {

    @Override
    public String getName() {
        return "list_item";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public Object call(Object value, List<String> extra) {
        if ( !List.class.isInstance(value) ) {
            return value;
        }

        @SuppressWarnings("unchecked")
        List<String> valueAsList = List.class.cast(value);
        try {
            int n = Integer.parseInt(extra.get(0));
            if ( n > 0 && n <= valueAsList.size() ) {
                return valueAsList.get(n - 1);
            }
        } catch ( NumberFormatException e ) {
        }

        return value;
    }
}
