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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.checks;

import org.incenp.obofoundry.sssom.ValidationError;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Checks that a mapping record that has a <code>review_date</code> also has
 * either a <code>reviewer_id</code> or a <code>reviewer_label</code>.
 */
public class MissingReviewerValidator implements IMappingValidator {

    @Override
    public ValidationError validate(Mapping mapping) {
        if ( mapping.getReviewDate() != null || mapping.getReviewerConfidence() != null ) {
            if ( (mapping.getReviewerId() == null || mapping.getReviewerId().isEmpty())
                    && (mapping.getReviewerLabel() == null || mapping.getReviewerLabel().isEmpty()) ) {
                return ValidationError.MISSING_REVIEWER;
            }
        }
        return null;
    }

}
