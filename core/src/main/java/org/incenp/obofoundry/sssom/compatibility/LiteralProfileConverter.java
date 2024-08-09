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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.compatibility;

import java.util.Map;

import org.incenp.obofoundry.sssom.IYAMLPreprocessor;
import org.incenp.obofoundry.sssom.SSSOMFormatException;

/**
 * A YAML preprocessor to convert <code>literal*</code> slots.
 * <p>
 * For a time, the SSSOM specification half-defined a "literal profile",
 * intended for mappings between a literal and an entity. That profile has
 * several literal-specific slots. In SSSOM 1.0, this profile has been removed
 * and replaced by a convention that literal subjects are represented by storing
 * the literal in the <code>subject_label</code> slot and setting the
 * <code>subject_type</code> slot to <code>rdfs literal</code>.
 */
public class LiteralProfileConverter implements IYAMLPreprocessor {

    @Override
    public void process(Map<String, Object> rawMap) throws SSSOMFormatException {
        if ( rawMap.containsKey("literal") && !rawMap.containsKey("subject_label") ) {
            rawMap.put("subject_label", rawMap.get("literal"));
            rawMap.put("subject_type", "rdfs literal");

            Object o = rawMap.get("literal_source");
            if ( o != null ) {
                rawMap.put("subject_source", o);
            }

            o = rawMap.get("literal_source_version");
            if ( o != null ) {
                rawMap.put("subject_source_version", o);
            }

            o = rawMap.get("literal_preprocessing");
            if ( o != null ) {
                rawMap.put("subject_preprocessing", o);
            }

            rawMap.remove("literal");
            rawMap.remove("literal_datatype");
            rawMap.remove("literal_source");
            rawMap.remove("literal_source_version");
            rawMap.remove("literal_preprocessing");
        }
    }
}
