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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for format modifier functions that can act both on a single value
 * and on a list of values, where all values in the list are to be processed in
 * the same way as single values.
 */
public abstract class BaseStringModifierFunction implements IFormatModifierFunction {

    @Override
    public Object call(Object value, List<String> extra) {
        if ( List.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            List<Object> valueAsList = List.class.cast(value);

            ArrayList<String> returnList = new ArrayList<String>();
            for ( Object o : valueAsList ) {
                returnList.add(apply(o, extra));
            }

            return returnList;
        }

        return apply(value, extra);
    }

    /**
     * Applies the function to a single value.
     * 
     * @param value The value to apply the function to.
     * @param extra Arguments to the function, if any.
     * @return The modified value.
     */
    protected abstract String apply(Object value, List<String> extra);
}
