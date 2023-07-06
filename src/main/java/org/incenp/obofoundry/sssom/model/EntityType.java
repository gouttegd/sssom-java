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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents the type of an entity that is being mapped.
 */
public enum EntityType {
    OWL_CLASS,
    OWL_OBJECT_PROPERTY,
    OWL_DATA_PROPERTY,
    OWL_ANNOTATION_PROPERTY,
    OWL_NAMED_INDIVIDUAL,
    SKOS_CONCEPT,
    RDFS_RESOURCE,
    RDFS_CLASS,
    RDFS_LITERAL,
    RDFS_DATATYPE,
    RDF_PROPERTY;

    private final static Map<String, EntityType> MAP;

    static {
        Map<String, EntityType> map = new HashMap<String, EntityType>();
        for ( EntityType value : EntityType.values() ) {
            map.put(value.toString(), value);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }

    @JsonCreator
    public static EntityType fromString(String v) {
        return MAP.get(v);
    }
}
