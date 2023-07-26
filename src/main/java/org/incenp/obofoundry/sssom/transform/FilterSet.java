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

import java.util.ArrayDeque;
import java.util.Deque;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A set of filters. It filters mappings according to several filters whose
 * results are combined using binary operators.
 */
public class FilterSet implements IMappingFilter {

    private Deque<IMappingFilter> combined = new ArrayDeque<IMappingFilter>();
    private String repr;

    @Override
    public boolean filter(Mapping mapping) {
        return combined.isEmpty() ? true : combined.peekLast().filter(mapping);
    }

    /**
     * Adds a filter to the set.
     * 
     * @param filter   The filter to add.
     * @param operator {@code true} to combine the new filter with the previous
     *                 filters in the set with a logical AND operator; {@code false}
     *                 to use a logical OR operator instead.
     */
    public void addFilter(IMappingFilter filter, boolean isAnd) {
        if ( combined.isEmpty() ) {
            combined.addLast(filter);
            repr = filter.toString();
        } else if ( isAnd ) {
            IMappingFilter last = combined.peekLast();
            combined.addLast((mapping) -> last.filter(mapping) && filter.filter(mapping));
            repr = String.format("%s && %s", repr, filter.toString());
        } else {
            IMappingFilter last = combined.peekLast();
            combined.addLast((mapping) -> last.filter(mapping) || filter.filter(mapping));
            repr = String.format("%s || %s", repr, filter.toString());
        }
    }

    @Override
    public String toString() {
        return String.format("(%s)", repr);
    }
}
