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

package org.incenp.obofoundry.sssom.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A value that modifies the meaning of a mapping predicate. Currently the only
 * defined modifier is {@code NOT}, which negates the predicate.
 */
public enum PredicateModifier {
    NOT("Not");

    private final String repr;

    PredicateModifier(String repr) {
        this.repr = repr;
    }

    @Override
    public String toString() {
        return repr;
    }

    /**
     * Parses a string into a predicate modifier.
     * 
     * @param v The string to parse.
     * @return The corresponding predicate modifier, of {@code null} if the provided
     *         string does not match any predicate modifier.
     */
    @JsonCreator
    public static PredicateModifier fromString(String v) {
        if ( v.equals("Not") ) {
            return PredicateModifier.NOT;
        }
        return null;
    }
}
