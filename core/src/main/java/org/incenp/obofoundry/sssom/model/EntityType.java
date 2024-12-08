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
    OWL_CLASS("http://www.w3.org/2002/07/owl#Class"),
    OWL_OBJECT_PROPERTY("http://www.w3.org/2002/07/owl#ObjectProperty"),
    OWL_DATA_PROPERTY("http://www.w3.org/2002/07/owl#DataProperty"),
    OWL_ANNOTATION_PROPERTY("http://www.w3.org/2002/07/owl#AnnotationProperty"),
    OWL_NAMED_INDIVIDUAL("http://www.w3.org/2002/07/owl#NamedIndividual"),
    SKOS_CONCEPT("http://www.w3.org/2004/02/skos/core#Concept"),
    RDFS_RESOURCE("http://www.w3.org/2000/01/rdf-schema#Resource"),
    RDFS_CLASS("http://www.w3.org/2000/01/rdf-schema#Class"),
    RDFS_LITERAL("http://www.w3.org/2000/01/rdf-schema#Literal"),
    RDFS_DATATYPE("http://www.w3.org/2000/01/rdf-schema#Datatype"),
    RDF_PROPERTY("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

    private final static Map<String, EntityType> MAP;

    static {
        Map<String, EntityType> map = new HashMap<String, EntityType>();
        for ( EntityType value : EntityType.values() ) {
            map.put(value.toString(), value);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    private String iri;

    EntityType(String iri) {
        this.iri = iri;
    }

    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }

    /**
     * Gets the IRI associated to the enum value.
     * 
     * @return The IRI used to represent the enum value, if any (may be
     *         {@code null}).
     */
    public String getIRI() {
        return iri;
    }

    /**
     * Parses a string into a entity type enum value.
     * 
     * @param v The string to parse.
     * @return The corresponding enumeration value, of {@code null} if the provided
     *         string does not match any entity type.
     */
    @JsonCreator
    public static EntityType fromString(String v) {
        return MAP.get(v);
    }
}
