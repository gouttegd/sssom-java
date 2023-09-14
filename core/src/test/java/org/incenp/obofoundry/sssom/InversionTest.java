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

package org.incenp.obofoundry.sssom;

import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InversionTest {

    private Mapping getSampleMapping(String predicate) {
        // @formatter:off
        Mapping mapping = Mapping.builder()
                .subjectId("http://example.org/subject")
                .predicateId(predicate)
                .objectId("http://example.org/object")
                .build();
        // @formatter:on

        return mapping;
    }

    @Test
    void testSimpleMappingInversion() {
        Mapping mapping = getSampleMapping(CommonPredicate.SKOS_EXACT_MATCH.toString());

        Mapping inverted = CommonPredicate.invert(mapping);
        Assertions.assertEquals(inverted.getSubjectId(), mapping.getObjectId());
        Assertions.assertEquals(CommonPredicate.SKOS_EXACT_MATCH.toString(), mapping.getPredicateId());
        Assertions.assertEquals(inverted.getObjectId(), mapping.getSubjectId());
    }

    @Test
    void testPredicateInversion() {
        Mapping mapping = getSampleMapping(CommonPredicate.SKOS_BROAD_MATCH.toString());

        Mapping inverted = CommonPredicate.invert(mapping);
        Assertions.assertEquals(CommonPredicate.SKOS_NARROW_MATCH.toString(), inverted.getPredicateId());
    }

    @Test
    void testNonInvertibleMapping() {
        Mapping mapping = getSampleMapping("http://www.w3.org/2004/02/skos/core#closeMatch");

        Mapping inverted = CommonPredicate.invert(mapping);
        Assertions.assertNull(inverted);
    }

    @Test
    void testInvertingCardinality() {
        Mapping mapping = getSampleMapping(CommonPredicate.SKOS_EXACT_MATCH.toString());
        mapping.setMappingCardinality(MappingCardinality.ONE_TO_MANY);

        Mapping inverted = CommonPredicate.invert(mapping);
        Assertions.assertEquals(MappingCardinality.MANY_TO_ONE, inverted.getMappingCardinality());
    }
}
