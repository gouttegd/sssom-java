/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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

import java.util.HashMap;
import java.util.Map;

/**
 * A YAML preprocessor to convert SSSOM-Py’s JSON-LD output into a normalised
 * form.
 * <p>
 * What SSSOM-Py calls “JSON” output is actually a JSON-LD-based output, where
 * prefix names are declared in the <em>@context</em> dictionary. When such a
 * dictionary is found, we turn it into a <em>curie_map</em> slot.
 */
public class JsonLDConverter implements IYAMLPreprocessor {

    @Override
    public void process(Map<String, Object> rawMap) throws SSSOMFormatException {
        // Remove @type key that could otherwise be interpreted as an extension slot
        rawMap.remove("@type");

        // Transform the @context into a curie_map
        if ( rawMap.containsKey("@context") ) {
            if ( !rawMap.containsKey("curie_map") ) {
                Object context = rawMap.get("@context");
                if ( context != null ) {
                    if ( !Map.class.isInstance(context) ) {
                        throw new SSSOMFormatException("Typing error when parsing '@context'");
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> contextMap = Map.class.cast(context);
                    HashMap<String, String> curieMap = new HashMap<String, String>();

                    for ( String key : contextMap.keySet() ) {
                        Object value = contextMap.get(key);
                        if ( value != null && String.class.isInstance(value) ) {
                            curieMap.put(key, String.class.cast(value));
                        }
                    }

                    rawMap.put("curie_map", curieMap);
                }
            }

            rawMap.remove("@context");
        }
    }

}
