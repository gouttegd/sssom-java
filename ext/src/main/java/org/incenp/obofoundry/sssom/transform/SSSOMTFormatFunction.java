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

import java.util.IllegalFormatException;
import java.util.List;

/**
 * Represents the SSSOM/T modifier function "format".
 * <p>
 * Use this function to apply an arbitrary formatting to a substituted value.
 * The function accepts a single parameter which should be a string containing a
 * single placeholder specification as accepted by Java’s
 * {@link String#format(String, Object...)} method. That placeholder will be
 * replaced by the substituted value.
 * <p>
 * For example, to format the value of the <code>confidence</code> slot (which
 * is of type Double):
 * 
 * <pre>
 * "Confidence: %{confidence|format('%.03f')}"
 * </pre>
 * <p>
 * If called on a list-typed value, the formatting will be applied to all
 * elements of the list.
 */
public class SSSOMTFormatFunction extends BaseStringModifierFunction {

    @Override
    public String getName() {
        return "format";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    protected String apply(Object value, List<String> extra) {
        try {
            return String.format(extra.get(0), value);
        } catch ( IllegalFormatException e) {
            return e.getMessage();
        }
    }
}
