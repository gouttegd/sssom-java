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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents the cardinality of a mapping. This indicates whether a mapping is
 * between exactly one subject and one object, between one subject and several
 * objects, etc.
 */
public enum MappingCardinality {
    ONE_TO_ONE("1:1"),
    ONE_TO_MANY("1:n"),
    MANY_TO_ONE("n:1"),
    ONE_TO_NONE("1:0"),
    NONE_TO_ONE("0:1"),
    MANY_TO_MANY("n:n");

    private final static Map<String, MappingCardinality> MAP;

    static {
        Map<String, MappingCardinality> map = new HashMap<String, MappingCardinality>();
        for ( MappingCardinality value : MappingCardinality.values() ) {
            map.put(value.toString(), value);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    private final String repr;

    MappingCardinality(String repr) {
        this.repr = repr;
    }

    @Override
    public String toString() {
        return repr;
    }

    /**
     * Gets the inverse of this cardinality (i.e., "1:n" -> "n:1").
     * 
     * @return The inverse cardinality (may be identical, e.g. for "1:1").
     */
    public MappingCardinality getInverse() {
        switch ( this ) {
        case MANY_TO_MANY:
        case ONE_TO_ONE:
            return this;
        case MANY_TO_ONE:
            return MappingCardinality.ONE_TO_MANY;
        case NONE_TO_ONE:
            return MappingCardinality.ONE_TO_NONE;
        case ONE_TO_MANY:
            return MappingCardinality.MANY_TO_ONE;
        case ONE_TO_NONE:
            return MappingCardinality.NONE_TO_ONE;
        default: // Should not happen
            return this;
        }
    }

    /**
     * Parses a string into a mapping cardinality enum value.
     * 
     * @param v The string to parse.
     * @return The corresponding enumeration value, of {@code null} if the provided
     *         string does not match any mapping cardinality.
     */
    @JsonCreator
    public static MappingCardinality fromString(String v) {
        return MAP.get(v);
    }

    /**
     * Infers the cardinality for all mappings in the given list. This overrides any
     * cardinality information that may already be stored in each mapping.
     * 
     * @param mappings The mappings for which to infer cardinality.
     */
    public static void inferCardinality(List<Mapping> mappings) {
        HashMap<String, Integer> subjects = new HashMap<String, Integer>();
        HashMap<String, Integer> objects = new HashMap<String, Integer>();

        for ( Mapping m : mappings ) {
            subjects.put(m.getSubjectId(), subjects.getOrDefault(m.getSubjectId(), 0) + 1);
            objects.put(m.getObjectId(), objects.getOrDefault(m.getObjectId(), 0) + 1);
        }

        for ( Mapping m : mappings ) {
            int nSubjects = subjects.get(m.getSubjectId());
            int nObjects = objects.get(m.getObjectId());

            if ( nSubjects == 1 ) {
                m.setMappingCardinality(nObjects == 1 ? MappingCardinality.ONE_TO_ONE : MappingCardinality.ONE_TO_MANY);
            } else {
                m.setMappingCardinality(
                        nObjects == 1 ? MappingCardinality.MANY_TO_ONE : MappingCardinality.MANY_TO_MANY);
            }
        }
    }
}
