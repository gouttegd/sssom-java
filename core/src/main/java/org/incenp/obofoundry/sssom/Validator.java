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

package org.incenp.obofoundry.sssom;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Helper class to validate MappingSet and Mapping objects.
 * <p>
 * This class is primarily intended to be used internally by the parsers for
 * post-parsing validation, to ensure that the parsed objects are compliant with
 * the requirements from the SSSOM specification. However it may also be used
 * independently of the parsers, for example to validate a set that was
 * constructed <em>ex nihilo</em>.
 */
public class Validator {

    public static final String MISSING_SUBJECT_LABEL = "Missing subject_label";
    public static final String MISSING_SUBJECT_ID = "Missing subject_id";
    public static final String MISSING_OBJECT_LABEL = "Missing object_label";
    public static final String MISSING_OBJECT_ID = "Missing object_id";
    public static final String MISSING_PREDICATE = "Missing predicate_id";
    public static final String MISSING_JUSTIFICATION = "Missing mapping_justification";

    /**
     * Validates an individual mapping. This method checks that the slots that are
     * required by the specification are present and non-empty.
     * 
     * @param mapping The mapping to validate.
     * @return An error message if the mapping is invalid, otherwise {@code null}.
     */
    public String validate(Mapping mapping) {
        if ( mapping.getSubjectType() == EntityType.RDFS_LITERAL ) {
            if ( mapping.getSubjectLabel() == null || mapping.getSubjectLabel().isEmpty() ) {
                return MISSING_SUBJECT_LABEL;
            }
        } else {
            if ( mapping.getSubjectId() == null || mapping.getSubjectId().isEmpty() ) {
                return MISSING_SUBJECT_ID;
            }
        }

        if ( mapping.getObjectType() == EntityType.RDFS_LITERAL ) {
            if ( mapping.getObjectLabel() == null || mapping.getObjectLabel().isEmpty() ) {
                return MISSING_OBJECT_LABEL;
            }
        } else {
            if ( mapping.getObjectId() == null || mapping.getObjectId().isEmpty() ) {
                return MISSING_OBJECT_ID;
            }
        }

        if ( mapping.getPredicateId() == null || mapping.getPredicateId().isEmpty() ) {
            return MISSING_PREDICATE;
        }

        if ( mapping.getMappingJustification() == null || mapping.getMappingJustification().isEmpty() ) {
            return MISSING_JUSTIFICATION;
        }

        return null;
    }
}
