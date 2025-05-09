/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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
 * Represents the type of an extension value.
 */
public enum ValueType {
    STRING("http://www.w3.org/2001/XMLSchema#string",
            "http://www.w3.org/2001/XMLSchema#normalizedString",
            "http://www.w3.org/2001/XMLSchema#token"),
    INTEGER("http://www.w3.org/2001/XMLSchema#integer",
            "http://www.w3.org/2001/XMLSchema#int",
            "http://www.w3.org/2001/XMLSchema#long",
            "http://www.w3.org/2001/XMLSchema#short",
            "http://www.w3.org/2001/XMLSchema#byte"),
    DOUBLE("http://www.w3.org/2001/XMLSchema#double",
           "http://www.w3.org/2001/XMLSchema#float"),
    BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean"),
    DATE("http://www.w3.org/2001/XMLSchema#date"),
    DATETIME("http://www.w3.org/2001/XMLSchema#datetime"),
    IDENTIFIER("https://w3id.org/linkml/Uriorcurie",
               "https://w3id.org/linkml/uriOrCurie"),
    OTHER(null);

    private final static Map<String, ValueType> MAP;

    static {
        HashMap<String, ValueType> map = new HashMap<String, ValueType>();
        for ( ValueType vt : ValueType.values() ) {
            if ( vt != ValueType.OTHER ) {
                map.put(vt.iri, vt);
                if ( vt.aliases != null ) {
                    for ( String alias : vt.aliases ) {
                        map.put(alias, vt);
                    }
                }
            }
        }

        MAP = Collections.unmodifiableMap(map);
    }

    private final String iri;
    private String[] aliases;

    ValueType(String iri) {
        this.iri = iri;
        this.aliases = null;
    }

    ValueType(String iri, String... aliases) {
        this.iri = iri;
        this.aliases = aliases;
    }

    @Override
    public String toString() {
        return iri;
    }

    /**
     * Parses a type identifier into a ValueType object.
     * 
     * @param iri The type IRI.
     * @return The corresponding ValueType ({@link #OTHER} if the IRI is not one of
     *         the recognised type).
     */
    public static ValueType fromIRI(String iri) {
        return MAP.getOrDefault(iri, OTHER);
    }
}
