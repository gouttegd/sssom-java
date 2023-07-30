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
 * Represents some "well-known" mapping predicates for which we have built-in
 * knowledge on how to invert them.
 */
public enum CommonPredicate {
    // @formatter:off
    //                                   Predicate IRI                                              Exact?  Reverse predicate IRI
    SKOS_EXACT_MATCH                    ("http://www.w3.org/2004/02/skos/core#exactMatch",          true                                                            ),
    SKOS_NARROW_MATCH                   ("http://www.w3.org/2004/02/skos/core#narrowMatch",                 "http://www.w3.org/2004/02/skos/core#broadMatch"        ),
    SKOS_BROAD_MATCH                    ("http://www.w3.org/2004/02/skos/core#broadMatch",                  "http://www.w3.org/2004/02/skos/core#narrowMatch"       ),
    OWL_EQUIVALENT_CLASS                ("http://www.w3.org/2002/07/owl#equivalentClass",           true                                                            ),
    SEMAPV_CROSS_SPECIES_EXACT_MATCH    ("https://w3id.org/semapv/vocab/crossSpeciesExactMatch",    true                                                            ),
    SEMAPV_CROSS_SPECIES_NARROW_MATCH   ("https://w3id.org/semapv/vocab/crossSpeciesNarrowMatch",           "https://w3id.org/semapv/vocab/crossSpeciesBroadMatch"  ),
    SEMAPV_CROSS_SPECIES_BROAD_MATCH    ("https://w3id.org/semapv/vocab/crossSpeciesBroadMatch",            "https://w3id.org/semapv/vocab/crossSpeciesNarrowMatch" );
    // @formatter:on

    private final static Map<String, CommonPredicate> MAP;

    static {
        Map<String, CommonPredicate> map = new HashMap<String, CommonPredicate>();
        for ( CommonPredicate value : CommonPredicate.values() ) {
            map.put(value.toString(), value);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    private final String iri;
    private final boolean exact;
    private final String reverse;

    CommonPredicate(String iri) {
        this.iri = iri;
        this.exact = false;
        this.reverse = null;
    }

    CommonPredicate(String iri, boolean exact) {
        this.iri = iri;
        this.exact = exact;
        this.reverse = null;
    }

    CommonPredicate(String iri, String reverse) {
        this.iri = iri;
        this.exact = reverse == null;
        this.reverse = reverse;
    }

    public boolean isExact() {
        return exact;
    }

    public String getReverse() {
        return exact ? iri : reverse;
    }

    public boolean isReversible() {
        return exact || reverse != null;
    }

    @Override
    public String toString() {
        return iri;
    }

    public static CommonPredicate fromString(String v) {
        return MAP.getOrDefault(v, null);
    }

    /**
     * Try inverting a mapping according to standard inversion rules.
     * 
     * @param mapping The mapping to invert.
     * @return A new mapping that is an inversion of the provided one, or
     *         {@code null} if the mapping could not be inverted.
     */
    public static Mapping invert(Mapping mapping) {
        CommonPredicate predicate = CommonPredicate.fromString(mapping.getPredicateId());
        if ( predicate == null || !predicate.isReversible() ) {
            return null;
        }

        // @formatter:off
        Mapping inverted = mapping.toBuilder()
                .predicateId(predicate.getReverse())
                .subjectCategory(mapping.getObjectCategory())
                .subjectId(mapping.getObjectId())
                .subjectLabel(mapping.getObjectLabel())
                .subjectMatchField(mapping.getObjectMatchField())
                .subjectPreprocessing(mapping.getObjectPreprocessing())
                .subjectSource(mapping.getObjectSource())
                .subjectSourceVersion(mapping.getObjectSource())
                .subjectType(mapping.getObjectType())
                .objectCategory(mapping.getSubjectCategory())
                .objectId(mapping.getSubjectId())
                .objectLabel(mapping.getSubjectLabel())
                .objectMatchField(mapping.getSubjectMatchField())
                .objectPreprocessing(mapping.getSubjectPreprocessing())
                .objectSource(mapping.getSubjectSource())
                .objectSourceVersion(mapping.getSubjectSourceVersion())
                .objectType(mapping.getSubjectType())
                .build();
        // @formatter:on
        return inverted;
    }
}
