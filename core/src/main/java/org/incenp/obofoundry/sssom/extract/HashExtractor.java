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

import org.incenp.obofoundry.sssom.MappingHasher;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Extracts the standard SSSOM hash for a mapping.
 * <p>
 * This is used by expressions of the form {@code mapping(N).special.hash}.
 */
public class HashExtractor extends MappingValueExtractor {
    
    private MappingHasher hasher = new MappingHasher();

    /**
     * Creates a new instance.
     * 
     * @param mappingNo The 0-based index of the mapping from which to extract the
     *                  hash, or (if negative) the 1-based index starting from the
     *                  last mapping.
     */
    public HashExtractor(int mappingNo) {
        super(mappingNo);
    }

    @Override
    protected Object extract(Mapping mapping) {
        return hasher.hash(mapping);
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }
}
