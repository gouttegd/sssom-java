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

package org.incenp.obofoundry.sssom.owl;

import java.util.HashSet;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;

/**
 * A mapping preprocessor that drops any mapping if another mapping with the
 * same value for a given field has already been set in the set.
 */
public class DuplicateFilterProcessor implements IMappingTransformer<Mapping> {

    private IMappingTransformer<String> accessor;
    private Set<String> seen = new HashSet<String>();

    /**
     * Creates a new instance.
     * 
     * @param fieldAccessor A mapping transformer that gives the value to use to
     *                      filter duplicate mappings.
     */
    public DuplicateFilterProcessor(IMappingTransformer<String> fieldAccessor) {
        accessor = fieldAccessor;
    }

    @Override
    public Mapping transform(Mapping mapping) {
        String value = accessor.transform(mapping);
        if ( seen.contains(value) ) {
            return null;
        }

        seen.add(value);
        return mapping;
    }

}
