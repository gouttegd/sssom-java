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

import java.util.Map;

/**
 * A YAML preprocessor to convert a dictionary containing a
 * {@code match_term_type} metadata slot into its standardised equivalents.
 * <p>
 * Initial versions of the SSSOM specification described a
 * {@code match_term_type} metadata slot which accepted values from a specific
 * enumeration and was intended to describe what was being matched (e.g., it
 * indicated that a given mapping was between two OWL classes, or between two
 * SKOS concepts, etc.). In SSSOM 0.9.1, this slot was replaced by two distinct
 * slots called {@code subject_type} and {@code object_type}.
 */
public class MatchTermTypeConverter implements IYAMLPreprocessor {

    @Override
    public void process(Map<String, Object> rawMap) throws SSSOMFormatException {
        if ( rawMap.containsKey("match_term_type") && !rawMap.containsKey("subject_type")
                && !rawMap.containsKey("object_type") ) {
            Object rawValue = rawMap.get("match_term_type");
            String value = null;
            if ( rawValue != null ) {
                if ( String.class.isInstance(rawValue) ) {
                    switch ( String.class.cast(rawValue) ) {
                    case "ConceptMatch":
                        value = "skos concept";
                        break;
                    case "ClassMatch":
                        value = "owl class";
                        break;
                    case "ObjectPropertyMatch":
                        value = "owl object property";
                        break;
                    case "IndividualMatch":
                        value = "owl named individual";
                        break;
                    case "DataPropertyMatch":
                        value = "owl data property";
                        break;
                    case "TermMatch":
                        // FIXME: It's unclear what is the equivalent of TermMatch in the new enum.
                        value = "rdfs literal";
                        break;
                    }
                }

                if ( value == null ) {
                    throw new SSSOMFormatException("Typing error when parsing 'match_term_type'");
                }
            }

            rawMap.remove("match_term_type");
            rawMap.put("subject_type", value);
            rawMap.put("object_type", value);
        }
    }

}
