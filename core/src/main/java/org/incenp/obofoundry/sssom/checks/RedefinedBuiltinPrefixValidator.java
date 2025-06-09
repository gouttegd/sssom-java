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

import java.util.Map;

import org.incenp.obofoundry.sssom.ValidationError;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Checks that the curie map embedded in a mapping set does not redefine any of
 * the prefixes that are considered “builtin” by the SSSOM specification.
 */
public class RedefinedBuiltinPrefixValidator implements IMappingSetValidator {

    @Override
    public ValidationError validate(MappingSet ms) {
        Map<String, String> curieMap = ms.getCurieMap();
        if ( curieMap != null ) {
            for ( String prefix : curieMap.keySet() ) {
                BuiltinPrefix bp = BuiltinPrefix.fromString(prefix);
                if ( bp != null && !bp.getPrefix().equals(curieMap.get(prefix)) ) {
                    return ValidationError.REDEFINED_BUILTIN_PREFIX;
                }
            }
        }
        return null;
    }
}
