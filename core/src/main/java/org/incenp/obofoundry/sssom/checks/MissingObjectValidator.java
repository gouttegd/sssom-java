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
 * Checks that a mapping has an object.
 * <p>
 * The object is represented either by the {@code object_id} slot, or by the
 * {@code object_label} slot iff the object type is set to
 * {@link EntityType#RDFS_LITERAL} (i.e. if the mapping is a “literal mapping”).
 */
public class MissingObjectValidator implements IMappingValidator {

    @Override
    public ValidationError validate(Mapping mapping) {
        if ( mapping.getObjectType() == EntityType.RDFS_LITERAL ) {
            if ( mapping.getObjectLabel() == null || mapping.getObjectLabel().isEmpty() ) {
                return ValidationError.MISSING_OBJECT;
            }
        } else {
            if ( mapping.getObjectId() == null || mapping.getObjectId().isEmpty() ) {
                return ValidationError.MISSING_OBJECT;
            }
        }
        return null;
    }
}
