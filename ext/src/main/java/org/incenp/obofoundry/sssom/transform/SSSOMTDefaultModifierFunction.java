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

import java.util.List;

/**
 * Represents the SSSOM/T substitution modifier function "default".
 * <p>
 * This function takes one argument. If the substituted value is {@code null} or
 * empty (empty string or empty list), the argument is inserted instead.
 * <p>
 * For example, the following will insert the value of the {@code mapping_tool}
 * slot, or {@code unknown tool} if the slot is not set.
 * 
 * <pre>
 * "Mapping tool: %{mapping_tool|default('unknown tool')}"
 * </pre>
 */
public class SSSOMTDefaultModifierFunction implements IFormatModifierFunction {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public Object call(Object value, List<String> extra) {
        if ( value != null ) {
            if ( value instanceof String ) {
                String v = (String) value;
                if ( !v.isEmpty() ) {
                    return value;
                }
            } else if ( value instanceof List ) {
                @SuppressWarnings("rawtypes")
                List l = (List) value;
                if ( !l.isEmpty() ) {
                    return value;
                }
            }
        }

        return extra.get(0);
    }

}
