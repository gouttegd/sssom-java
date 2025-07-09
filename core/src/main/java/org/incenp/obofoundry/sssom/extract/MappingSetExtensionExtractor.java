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
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Extracts the value of an extension slot from a mapping set object.
 * <p>
 * This is the extractor used by an expression of the form
 * {@code set.extension(PROPERTY)}.
 */
public class MappingSetExtensionExtractor implements IValueExtractor {

    protected String property;

    /**
     * Creates a new instance.
     * 
     * @param property The name of the property representing the extension slot to
     *                 extract.
     */
    public MappingSetExtensionExtractor(String property) {
        this.property = property;
    }

    @Override
    public Object extract(MappingSet ms) {
        Map<String, ExtensionValue> extensions = ms.getExtensions();
        return extensions != null ? extensions.get(property) : null;
    }

    @Override
    public Class<?> getType() {
        return ExtensionValue.class;
    }
}
