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
    OWL_CLASS("http://www.w3.org/2002/07/owl#Class", "owl:Class"),
    OWL_OBJECT_PROPERTY("http://www.w3.org/2002/07/owl#ObjectProperty", "owl:ObjectProperty"),
    OWL_DATA_PROPERTY("http://www.w3.org/2002/07/owl#DataProperty", "owl:DataProperty"),
    OWL_ANNOTATION_PROPERTY("http://www.w3.org/2002/07/owl#AnnotationProperty", "owl:AnnotationProperty"),
    OWL_NAMED_INDIVIDUAL("http://www.w3.org/2002/07/owl#NamedIndividual", "owl:NamedIndividual"),
    SKOS_CONCEPT("http://www.w3.org/2004/02/skos/core#Concept", "skos:Concept"),
    RDFS_RESOURCE("http://www.w3.org/2000/01/rdf-schema#Resource", "rdfs:Resource"),
    RDFS_CLASS("http://www.w3.org/2000/01/rdf-schema#Class", "rdfs:Class"),
    RDFS_LITERAL("http://www.w3.org/2000/01/rdf-schema#Literal", "rdfs:Literal"),
    RDFS_DATATYPE("http://www.w3.org/2000/01/rdf-schema#Datatype", "rdfs:Datatype"),
    RDF_PROPERTY("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property", "rdf:Property"),
    COMPOSED_ENTITY_EXPRESSION("https://w3id.org/sssom/ComposedEntityExpression");

    private final static Map<String, EntityType> MAP;
    private final static Map<String, EntityType> URI_MAP;
    private final static Map<String, EntityType> SYN_MAP;

    static {
        Map<String, EntityType> map = new HashMap<String, EntityType>();
        Map<String, EntityType> uri_map = new HashMap<String, EntityType>();
        Map<String, EntityType> syn_map = new HashMap<String, EntityType>();
        for ( EntityType value : EntityType.values() ) {
            map.put(value.toString(), value);
            if ( value.iri != null ) {
                uri_map.put(value.iri, value);
            }
            if ( value.synonym != null ) {
                syn_map.put(value.synonym, value);
            }
        }

        MAP = Collections.unmodifiableMap(map);
        URI_MAP = Collections.unmodifiableMap(uri_map);
        SYN_MAP = Collections.unmodifiableMap(syn_map);
    }

    private String iri;
    private String synonym;

    EntityType(String iri) {
        this.iri = iri;
    }

    EntityType(String iri, String synonym) {
        this.iri = iri;
        this.synonym = synonym;
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

    /**
     * Parses a string into a entity type enum value.
     * <p>
     * This is similar to {@link #fromString(String)}, but optionally allows to
     * recognise “synonyms”.
     * 
     * @param v   The string to parse.
     * @param lax If {@code true}, will attempt to match the provided value against
     *            known synonyms of the expected values.
     * @return The corresponding enumeration value, or {@code null} if the provided
     *         value does not match any entity type.
     */
    public static EntityType fromString(String v, boolean lax) {
        EntityType et = MAP.get(v);
        if ( et == null && lax ) {
            et = SYN_MAP.get(v);
        }
        return et;
    }

    /**
     * Parses an IRI into the corresponding entity type enum value.
     * <p>
     * Do note that currently, not all values for the EntityTpe enumeration have an
     * associated IRI.
     * 
     * @param iri The IRI to parse.
     * @return The corresponding enumeration value, or {@code null} if the provided
     *         IRI does not match any entity type.
     */
    public static EntityType fromIRI(String iri) {
        return URI_MAP.get(iri);
    }
}
