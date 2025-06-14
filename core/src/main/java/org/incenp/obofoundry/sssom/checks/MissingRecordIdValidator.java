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

    private boolean hasSomeRecordID;

    @Override
    public ValidationError validate(Mapping mapping) {
        if ( mapping.getRecordId() != null && !mapping.getRecordId().isEmpty() ) {
            hasSomeRecordID = true;
        } else if ( hasSomeRecordID ) {
            return ValidationError.MISSING_RECORD_ID;
        }
        return null;
    }

    @Override
    public ValidationError validate(MappingSet ms) {
        // We do not actually validate anything here. We use this method as an
        // initialiser that is called before the real validation method above is called
        // in individual mappings.
        hasSomeRecordID = false;
        return null;
    }

}
