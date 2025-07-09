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

package org.incenp.obofoundry.sssom.extract;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Base class for extractors that extract a value from a specific mapping
 * record.
 * <p>
 * This is used by all expressions of the form {@code mapping.} or
 * {@code mapping(N).}.
 */
public abstract class MappingValueExtractor implements IValueExtractor {

    protected int mappingNo;

    /**
     * Creates a new instance.
     * 
     * @param mappingNo If non-negative, the 0-based index of the mapping from which
     *                  to extract a value. If negative, the 1-based index starting
     *                  from the last mapping of the set.
     */
    protected MappingValueExtractor(int mappingNo) {
        this.mappingNo = mappingNo;
    }

    @Override
    public Object extract(MappingSet ms) {
        int len = ms.getMappings().size();
        int n = mappingNo >= 0 ? mappingNo : len + mappingNo;
        if ( n >= 0 && n < len ) {
            return extract(ms.getMappings().get(n));
        }
        return null;
    }

    /**
     * Extracts the desired value from the given mapping.
     * 
     * @param mapping The mapping from which to extract the value.
     * @return The desired value, or {@code null} if the mapping does not have a
     *         value at the specified location.
     */
    protected abstract Object extract(Mapping mapping);
}
