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

import org.incenp.obofoundry.sssom.PrefixManager;

/**
 * Represents the SSSOM/T modifier function "suffix".
 * <p>
 * This function replaces the substituted text, which is expected to be a IRI,
 * by the local part (the "suffix") of its short form. It does not take any
 * additional argument.
 */
public class SSSOMTSuffixFunction implements IFormatModifierFunction {

    private PrefixManager pfxMgr;

    /**
     * Creates a new instance.
     * 
     * @param prefixManager The prefix manager to use to shorten the IRI in the
     *                      substituted text.
     */
    public SSSOMTSuffixFunction(PrefixManager prefixManager) {
        pfxMgr = prefixManager;
    }

    @Override
    public String getName() {
        return "suffix";
    }

    @Override
    public String getSignature() {
        return "";
    }

    @Override
    public Object call(Object value, List<String> extra) {
        if ( List.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            List<String> valueAsList = List.class.cast(value);

            List<String> suffixList = new ArrayList<String>();
            ;
            for ( String s : valueAsList ) {
                suffixList.add(pfxMgr.getLocalName(s));
            }
            return suffixList;
        }
        return pfxMgr.getLocalName(value.toString());
    }

}
