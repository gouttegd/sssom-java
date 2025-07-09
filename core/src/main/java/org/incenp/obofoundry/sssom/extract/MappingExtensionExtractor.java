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

import java.util.Map;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Extracts the value of an extension slot from a mapping object.
 * <p>
 * This is the extractor used by an expression of the form
 * {@code mapping(N).extension(PROPERTY)}.
 */
public class MappingExtensionExtractor extends MappingValueExtractor {

    protected String property;

    /**
     * Creates a new instance.
     * 
     * @param mappingNo The 0-based index of the mapping from which to extract the
     *                  extension value, or (if negative) the 1-based index starting
     *                  from the last mapping.
     * @param property  The name of the property representing the extension slot to
     *                  extract.
     */
    public MappingExtensionExtractor(int mappingNo, String property) {
        super(mappingNo);
        this.property = property;
    }

    @Override
    protected Object extract(Mapping mapping) {
        Map<String, ExtensionValue> extensions = mapping.getExtensions();
        return extensions != null ? extensions.get(property) : null;
    }

    @Override
    public Class<?> getType() {
        return ExtensionValue.class;
    }
}
