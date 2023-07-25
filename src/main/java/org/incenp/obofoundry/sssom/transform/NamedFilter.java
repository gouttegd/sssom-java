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

package org.incenp.obofoundry.sssom.transform;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A filter that has a string representation. This class merely wraps an
 * existing filter. It is mostly intended for debugging, so that filters created
 * using lambda functions can still be displayed in an readable form.
 */
public class NamedFilter implements IMappingFilter {

    private String repr;
    private IMappingFilter impl;

    /**
     * Creates a new instance.
     * 
     * @param name   A string representation of the filter.
     * @param filter The actual filter.
     */
    public NamedFilter(String name, IMappingFilter filter) {
        repr = name;
        impl = filter;
    }

    @Override
    public boolean filter(Mapping mapping) {
        return impl.filter(mapping);
    }

    @Override
    public String toString() {
        return repr;
    }

}
