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
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Checks that either all records in a set have a record ID, or none have one.
 */
public class MissingRecordIdValidator implements IMappingSetValidator, IMappingValidator {

    private boolean expectRecordID;

    @Override
    public ValidationError validate(Mapping mapping) {
        boolean hasRecordID = mapping.getRecordId() != null && !mapping.getRecordId().isEmpty();
        if ( hasRecordID != expectRecordID ) {
            return ValidationError.MISSING_RECORD_ID;
        }
        return null;
    }

    @Override
    public ValidationError validate(MappingSet ms) {
        // Check whether the first mapping has a record ID; this will set the
        // expectation for all subsequent mappings.
        if ( ms.getMappings() != null && !ms.getMappings().isEmpty() ) {
            Mapping first = ms.getMappings().get(0);
            expectRecordID = first.getRecordId() != null && !first.getRecordId().isEmpty();
        } else {
            expectRecordID = false;
        }
        return null;
    }

}
