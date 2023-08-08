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
    //                                   Predicate IRI                                              Exact?  Inverse predicate IRI
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
    private final String inverse;

    CommonPredicate(String iri) {
        this.iri = iri;
        this.exact = false;
        this.inverse = null;
    }

    CommonPredicate(String iri, boolean exact) {
        this.iri = iri;
        this.exact = exact;
        this.inverse = null;
    }

    CommonPredicate(String iri, String reverse) {
        this.iri = iri;
        this.exact = reverse == null;
        this.inverse = reverse;
    }

    /**
     * Indicates whether the predicate is “exact”. A mapping with an exact predicate
     * can be inverted without changing the predicate.
     * 
     * @return {@code true} if the predicate is exact, {@code false} otherwise.
     */
    public boolean isExact() {
        return exact;
    }

    /**
     * Gets the inverse predicate for this predicate. When a predicate has a known
     * inverse predicate, the inverse predicate should be used when inverting a
     * mapping.
     * 
     * @return The inverse predicate, if known; otherwise {@code null}.
     */
    public String getInverse() {
        return exact ? iri : inverse;
    }

    /**
     * Indicates whether a predicate can be inverted. A predicate can be inverted if
     * it is an exact predicate or if it has a known inverse predicate.
     * 
     * @return {@code true} if the predicate can be inverted, otherwise
     *         {@code false}.
     */
    public boolean isInvertible() {
        return exact || inverse != null;
    }

    @Override
    public String toString() {
        return iri;
    }

    /**
     * Parses a string into a common predicate.
     * 
     * @param v The string to parse.
     * @return The corresponding predicate, or {@code null} if the provided string
     *         does not match any of the common predicates.
     */
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
        if ( predicate == null || !predicate.isInvertible() ) {
            return null;
        }

        // @formatter:off
        Mapping inverted = mapping.toBuilder()
                .predicateId(predicate.getInverse())
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
