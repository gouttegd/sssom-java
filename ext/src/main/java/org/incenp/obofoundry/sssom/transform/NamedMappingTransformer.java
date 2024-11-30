/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.transform;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A mapping transformer that has a string representation. This class merely
 * wraps an existing transformer. It is mostly intended for debugging, so that
 * transformers created using lambda functions can still be displayed in a
 * readable form.
 * <p>
 * Example, for a transformer that produces basic string representations of
 * mappings:
 * 
 * <pre>
 * IMappingTransformer&lt;String&gt; myTransformer = new NamedMappingTransformer&lt;String&gt;("mapping-to-string",
 *         (mapping) -&gt; String.format("%s -[%s]-&gt; %s", mapping.getSubjectId(), mapping.getPredicateId(),
 *                 mapping.getObjectId()));
 * </pre>
 *
 * @param <T> The type of object to transform the mapping into.
 */
public class NamedMappingTransformer<T> implements IMappingTransformer<T> {

    private String repr;
    private IMappingTransformer<T> impl;

    /**
     * Creates a new instance.
     * 
     * @param name        A string representation of the transformer.
     * @param transformer The actual transformer.
     */
    public NamedMappingTransformer(String name, IMappingTransformer<T> transformer) {
        repr = name;
        impl = transformer;
    }

    @Override
    public T transform(Mapping mapping) {
        return impl.transform(mapping);
    }

    @Override
    public String toString() {
        return repr;
    }
}
