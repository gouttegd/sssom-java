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
import java.util.Map;

/**
 * URL prefixes considered builtin by the SSSOM specification. These prefixes do
 * not need to be declared in the curie map that accompanies a SSSOM mapping
 * set.
 */
public enum BuiltinPrefix {
    SSSOM("https://w3id.org/sssom/"),
    OWL("http://www.w3.org/2002/07/owl#"),
    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    RDFS("http://www.w3.org/2000/01/rdf-schema#"),
    SKOS("http://www.w3.org/2004/02/skos/core#"),
    SEMAPV("https://w3id.org/semapv/vocab/");

    private final static Map<String, BuiltinPrefix> MAP;

    static {
        Map<String, BuiltinPrefix> map = new HashMap<String, BuiltinPrefix>();
        for ( BuiltinPrefix value : BuiltinPrefix.values() ) {
            map.put(value.getPrefixName(), value);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    private final String prefix;

    BuiltinPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return The URL prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return The standard prefix name for this prefix.
     */
    public String getPrefixName() {
        return name().toLowerCase();
    }

    /**
     * Parses a prefix name into one of the built-in prefix.
     * 
     * @param v The prefix name to parse.
     * @return The corresponding prefix, or {@code null} if the specified name is
     *         not a built-in prefix name.
     */
    public static BuiltinPrefix fromString(String v) {
        return MAP.getOrDefault(v, null);
    }
}
