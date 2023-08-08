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
 * An interface to filter mappings according in a mapping set. This is the
 * fundamental building block for all filtering operations on mappings.
 * <p>
 * Simple filters can easily be implemented using lambda functions. For example,
 * to filter in mappings that have a “mapping source”:
 * 
 * <pre>
 * IMappingFilter myFilter = (mapping) -&gt; mapping.getMappingSource() != null;
 * </pre>
 */
public interface IMappingFilter {

    /**
     * Check if a mapping satisfies a given condition.
     * 
     * @param mapping The mapping to test.
     * @return {@code true} if the mapping satisfies the condition, {@code false}
     *         otherwise.
     */
    public boolean filter(Mapping mapping);
}
