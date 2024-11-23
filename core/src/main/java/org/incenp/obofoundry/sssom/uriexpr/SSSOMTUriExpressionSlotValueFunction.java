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

package org.incenp.obofoundry.sssom.uriexpr;

import java.util.List;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.transform.IFormatModifierFunction;

/**
 * Represents the SSSOM/T substitution modifier function "uriexpr_slot_value".
 * <p>
 * This function may be used to extract the value of a slot within a URI
 * Expression, and insert it into a string.
 * <p>
 * Example:
 * 
 * <pre>
 * "The value of 'field1' is: %{subject_id|uriexpr_slot_value(field1)}"
 * </pre>
 * <p>
 * Assuming that the subject ID of the mapping currently being processed is a
 * URI Expression and that the expression does contain a <em>field1</em> slot,
 * this will insert the value of that slot into the string. Otherwise, the
 * subject ID will be inserted unmodified.
 * <p>
 * Note that CURIEs embedded with the URI Expression are expanded into their
 * full-length form. To insert the slot value as a CURIE, append the
 * <em>short</em> modifier after this function, as in:
 * 
 * <pre>
 * "The (CURIE) value of 'field1 is %{subject_id|uriexpr_slot_value(field1)|short}"
 * </pre>
 */
public class SSSOMTUriExpressionSlotValueFunction implements IFormatModifierFunction {

    private PrefixManager pfxMgr;

    /**
     * Creates a new instance.
     * 
     * @param prefixManager The prefix manager to use for expanding short IDs
     *                      embedded within a URI Expression.
     */
    public SSSOMTUriExpressionSlotValueFunction(PrefixManager prefixManager) {
        pfxMgr = prefixManager;
    }

    @Override
    public String getName() {
        return "uriexpr_slot_value";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public Object call(Object value, List<String> arguments) {
        UriExpression expr = UriExpression.parse(value.toString(), pfxMgr);
        if ( expr != null ) {
            String slotValue = expr.getComponent(arguments.get(0));
            if ( slotValue != null ) {
                return slotValue;
            }
        }

        // Not a URI Expression or slot not found, return the original value
        return value;
    }

}
