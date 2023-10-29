/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
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
 * A YAML preprocessor to convert a dictionary containing a {@code match_type}
 * metadata slot into its standardised equivalent.
 * <p>
 * Initial versions of the SSSOM specification described a {@code match_type}
 * metadata slot which accepted values from a specific enumeration. In SSSOM
 * 0.9.1, this slot was replaced by a new {@code mapping_justification} slot
 * which accepts entity references, and the enumeration was replaced by terms
 * from the SEMAPV vocabulary.
 */
public class MatchTypeConverter implements IYAMLPreprocessor {

    @Override
    public void process(Map<String, Object> rawMap) throws SSSOMFormatException {
        if ( rawMap.containsKey("match_type") && !rawMap.containsKey("mapping_justification") ) {
            Object rawValue = rawMap.get("match_type");
            String value = null;
            if ( rawValue != null ) {
                if ( String.class.isInstance(rawValue) ) {
                    switch ( String.class.cast(rawValue) ) {
                    case "Lexical":
                        value = "semapv:LexicalMatching";
                        break;
                    case "Logical":
                        value = "semapv:LogicalMatching";
                        break;
                    case "HumanCurated":
                        value = "semapv:ManualMappingCuration";
                        break;
                    case "Complex":
                        value = "semapv:CompositeMatching";
                        break;
                    case "Unspecified":
                        value = "semapv:UnspecifiedMatching";
                        break;
                    case "SemanticSimilarity":
                        value = "semapv:SemanticSimilarityThresholdMatching";
                        break;
                    }
                }

                if ( value == null ) {
                    throw new SSSOMFormatException("Typing error when parsing 'match_type'");
                }
            }

            rawMap.remove("match_type");
            rawMap.put("mapping_justification", value);
        }
    }

}
