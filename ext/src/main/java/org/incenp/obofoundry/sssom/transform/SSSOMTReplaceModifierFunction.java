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
 * Represents the SSSOM/T modifier function "replace".
 * <p>
 * Use this function to perform a basic find-and-replace operation within the
 * substituted value. The function accepts two parameters, being the text to
 * find and the replacement text, respectively.
 * <p>
 * For example, to get a shortened identifier for the <code>predicate_id</code>,
 * but with the colon (:) character replaced by an underscore (_):
 * 
 * <pre>
 * "%{predicate_id|short|replace(':', '_')}
 * </pre>
 */
public class SSSOMTReplaceModifierFunction implements IFormatModifierFunction {

    @Override
    public String getName() {
        return "replace";
    }

    @Override
    public String getSignature() {
        return "SS";
    }

    @Override
    public Object call(Object value, List<String> extra) {
        String find = extra.get(0);
        String replace = extra.get(1);

        if ( List.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            List<String> valueAsList = List.class.cast(value);

            List<String> replacedList = new ArrayList<String>();
            for ( String s : valueAsList ) {
                replacedList.add(s.replace(find, replace));
            }

            return replacedList;
        }
        return value.toString().replace(find, replace);
    }

}
