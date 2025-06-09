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
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Checks that a mapping has a subject.
 * <p>
 * The subject is represented either by the {@code subject_id} slot, or by the
 * {@code subject_label} slot iff the subject type is set to
 * {@link EntityType#RDFS_LITERAL} (i.e. if the mapping is a “literal mapping”).
 */
public class MissingSubjectValidator implements IMappingValidator {

    @Override
    public ValidationError validate(Mapping mapping) {
        if ( mapping.getSubjectType() == EntityType.RDFS_LITERAL ) {
            if ( mapping.getSubjectLabel() == null || mapping.getSubjectLabel().isEmpty() ) {
                return ValidationError.MISSING_SUBJECT;
            }
        } else {
            if ( mapping.getSubjectId() == null || mapping.getSubjectId().isEmpty() ) {
                return ValidationError.MISSING_SUBJECT;
            }
        }
        return null;
    }
}
