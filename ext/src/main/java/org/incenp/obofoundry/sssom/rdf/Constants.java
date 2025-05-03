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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;

/**
 * Constant values used throughout the RDF support package.
 */
public class Constants {
    private static final String SSSOM_NS = BuiltinPrefix.SSSOM.getPrefix();

    /**
     * The IRI used in RDF to represent a MappingSet object.
     */
    public static final IRI SSSOM_MAPPING_SET = Values.iri(SSSOM_NS, "MappingSet");

    /**
     * The IRI of the property that links a Mapping to the MappingSet it belongs to.
     */
    public static final IRI SSSOM_MAPPINGS = Values.iri(SSSOM_NS, "mappings");

    /**
     * The IRI of the property that links a SSSOM Version value to a mapping set.
     */
    public static final IRI SSSOM_VERSION = Values.iri(SSSOM_NS, "sssom_version");

    /**
     * The IRI of the property that links a “Extension Definition” to the MappingSet
     * it belongs to.
     */
    public static final IRI SSSOM_EXT_DEFINITIONS = Values.iri(SSSOM_NS, "extension_definitions");

    /**
     * The IRI of the property that gives the name of an extension slot.
     */
    public static final IRI SSSOM_EXT_SLOTNAME = Values.iri(SSSOM_NS, "slot_name");

    /**
     * The IRI of the property that gives the property associated with an extension
     * slot.
     */
    public static final IRI SSSOM_EXT_PROPERTY = Values.iri(SSSOM_NS, "property");

    /**
     * The IRI of the property that gives the optional type hint for an extension
     * slot.
     */
    public static final IRI SSSOM_EXT_TYPEHINT = Values.iri(SSSOM_NS, "type_hint");

    /**
     * The IRI used in RDF to represent a Mapping object.
     */
    public static final IRI OWL_AXIOM = Values.iri(BuiltinPrefix.OWL.getPrefix(), "Axiom");
}
