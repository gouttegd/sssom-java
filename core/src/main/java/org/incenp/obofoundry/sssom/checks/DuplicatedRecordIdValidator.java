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

import java.util.HashSet;

import org.incenp.obofoundry.sssom.ValidationError;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Check that no two records in a set have the same record ID.
 */
public class DuplicatedRecordIdValidator implements IMappingSetValidator, IMappingValidator {

    private HashSet<String> recordIds = new HashSet<>();

    @Override
    public ValidationError validate(Mapping mapping) {
        String id = mapping.getRecordId();
        if ( id != null && recordIds.contains(id) ) {
            return ValidationError.DUPLICATED_RECORD_ID;
        } else {
            recordIds.add(id);
        }
        return null;
    }

    @Override
    public ValidationError validate(MappingSet ms) {
        // We do not actually validate anything here. We use this method as an
        // initialiser that is called before the real validation method above is called
        // in individual mappings.
        recordIds.clear();
        return null;
    }

}
