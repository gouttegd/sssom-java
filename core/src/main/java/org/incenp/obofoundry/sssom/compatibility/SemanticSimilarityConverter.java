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
 * A YAML preprocessor to convert a dictionary containing a
 * {@code semantic_similarity_score} or {@code semantic_similarity_measure}
 * metadata slot into its standardised equivalent.
 * <p>
 * Initial versions of the SSSOM specification described a
 * {@code semantic_similarity_score} metadata slot to hold the similarity score
 * between the subject and the object. In SSSOM 1.0, this slot was renamed into
 * {@code similarity_score}. Likewise for {@code semantic_similarity_measure},
 * renamed into {@code similarity_measure}.
 */
public class SemanticSimilarityConverter implements IYAMLPreprocessor {

    @Override
    public void process(Map<String, Object> rawMap) throws SSSOMFormatException {
        if ( rawMap.containsKey("semantic_similarity_score") && !rawMap.containsKey("similarity_score") ) {
            rawMap.put("similarity_score", rawMap.get("semantic_similarity_score"));
        }

        if ( rawMap.containsKey("semantic_similarity_measure") && !rawMap.containsKey("similarity_measure") ) {
            rawMap.put("similarity_measure", rawMap.get("semantic_similarity_measure"));
        }

        rawMap.remove("semantic_similarity_score");
        rawMap.remove("semantic_similarity_measure");
    }

}
