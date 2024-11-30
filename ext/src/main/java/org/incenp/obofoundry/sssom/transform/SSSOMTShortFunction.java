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

import java.util.ArrayList;
import java.util.List;

import org.incenp.obofoundry.sssom.PrefixManager;

/**
 * Represents the SSSOM/T substitution modifier function "short".
 * <p>
 * This function shortens the substituted text, which is expected to be a IRI,
 * into its “CURIE” form. It does not take any additional argument.
 */
public class SSSOMTShortFunction implements IFormatModifierFunction {

    private PrefixManager pfxMgr;

    /**
     * Creates a new instance.
     * 
     * @param prefixManager The prefix manager to use to shorten the substituted
     *                      text.
     */
    public SSSOMTShortFunction(PrefixManager prefixManager) {
        pfxMgr = prefixManager;
    }

    @Override
    public String getName() {
        return "short";
    }

    @Override
    public String getSignature() {
        return "";
    }

    @Override
    public Object call(Object value, List<String> arguments) {
        if ( List.class.isInstance(value) ) {
            // Shorten all IRIs in the list
            @SuppressWarnings("unchecked")
            List<String> valueAsList = List.class.cast(value);

            List<String> shortenedList = new ArrayList<String>();
            for ( String s : valueAsList ) {
                shortenedList.add(pfxMgr.expandIdentifier(s));
            }
            return shortenedList;
        }

        return pfxMgr.shortenIdentifier(value.toString());
    }

}
