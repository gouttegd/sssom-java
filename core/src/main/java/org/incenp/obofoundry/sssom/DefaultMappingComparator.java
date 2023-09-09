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

import java.util.Comparator;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Comparator for mapping objects. This class allows to sort mappings in
 * <em>almost</em> the order recommended by the SSSOM specification.
 */
public class DefaultMappingComparator implements Comparator<Mapping> {

    @Override
    public int compare(Mapping o1, Mapping o2) {
        // Try comparing on subject - predicate - object. This should already be enough
        // for most mappings.
        int ret = o1.getSubjectId().compareTo(o2.getSubjectId());
        if ( ret == 0 ) {
            ret = o1.getPredicateId().compareTo(o2.getPredicateId());
        }
        if ( ret == 0 ) {
            ret = o1.getObjectId().compareTo(o2.getObjectId());
        }

        // Sort on a few other fields. According to the SSSOM specification we should
        // sort on *all* fields, but we are not going to do that here.
        if ( ret == 0 && o1.getComment() != null && o2.getComment() != null ) {
            ret = o1.getComment().compareTo(o2.getComment());
        }
        if ( ret == 0 && o1.getMappingJustification() != null && o2.getMappingJustification() != null ) {
            ret = o1.getMappingJustification().compareTo(o2.getMappingJustification());
        }
        if ( ret == 0 && o1.getConfidence() != null && o2.getConfidence() != null ) {
            ret = o1.getConfidence().compareTo(o2.getConfidence());
        }
        if ( ret == 0 && o1.getSemanticSimilarityScore() != null && o2.getSemanticSimilarityScore() != null ) {
            ret = o1.getSemanticSimilarityScore().compareTo(o2.getSemanticSimilarityScore());
        }

        // Giving up. If both mappings still can't be distinguished at this point, let
        // the result be non-deterministic.

        return ret;
    }

}
