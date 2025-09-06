/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {

    @Test
    void testVersionCompatibility() {
        Assertions.assertTrue(Version.SSSOM_1_0.isCompatibleWith(Version.SSSOM_1_0));
        Assertions.assertTrue(Version.SSSOM_1_0.isCompatibleWith(Version.SSSOM_1_1));
        Assertions.assertFalse(Version.SSSOM_1_1.isCompatibleWith(Version.SSSOM_1_0));
        Assertions.assertTrue(Version.SSSOM_1_1.isCompatibleWith(Version.SSSOM_1_1));
    }

    @Test
    void testGetHighestVersion() {
        Assertions.assertEquals(Version.SSSOM_1_0, Version.getHighestVersion(Set.of(Version.SSSOM_1_0)));
        Assertions.assertEquals(Version.SSSOM_1_1, Version.getHighestVersion(Set.of(Version.SSSOM_1_1)));
        Assertions.assertEquals(Version.SSSOM_1_1,
                Version.getHighestVersion(Set.of(Version.SSSOM_1_0, Version.SSSOM_1_1)));
    }

    @Test
    void testUnknownVersion() {
        Version unknown = Version.fromString("not a SSSOM version");
        Assertions.assertEquals(Version.UNKNOWN, unknown);

        // An unknown version cannot be compatible with any known version
        Assertions.assertFalse(unknown.isCompatibleWith(Version.SSSOM_1_0));
        Assertions.assertFalse(unknown.isCompatibleWith(Version.SSSOM_1_1));

        // No known version can be compatible with an unknown version
        Assertions.assertFalse(Version.SSSOM_1_0.isCompatibleWith(unknown));
        Assertions.assertFalse(Version.SSSOM_1_1.isCompatibleWith(unknown));

        // "Highest" version in a set containing an unknown version is unknown
        Assertions.assertEquals(Version.UNKNOWN, Version.getHighestVersion(Set.of(unknown, Version.SSSOM_1_1)));
    }

    @Test
    void testGetCompliantVersion() {
        MappingSet set = new MappingSet();

        // Empty set is compliant with 1.0
        Assertions.assertEquals(Version.SSSOM_1_0, Version.getCompliantVersion(set));

        // A set with only slots from 1.0 is compliant with 1.0
        set.setComment("A comment");
        set.setPublicationDate(LocalDate.now());
        Assertions.assertEquals(Version.SSSOM_1_0, Version.getCompliantVersion(set));

        // A set with a slot from 1.1 is compliant with 1.1
        set.setPredicateType(EntityType.RDF_PROPERTY);
        Assertions.assertEquals(Version.SSSOM_1_1, Version.getCompliantVersion(set));

        // Unless the slot is sssom_version
        set = MappingSet.builder().sssomVersion(Version.SSSOM_1_1).build();
        Assertions.assertEquals(Version.SSSOM_1_0, Version.getCompliantVersion(set));

        // A set with only 1.0 metadata slots but containing mappings with 1.1 slots
        // requires 1.1
        set = MappingSet.builder().comment("A comment").mappings(new ArrayList<>()).build();
        set.getMappings().add(Mapping.builder().subjectType(EntityType.COMPOSED_ENTITY_EXPRESSION).build());
        Assertions.assertEquals(Version.SSSOM_1_1, Version.getCompliantVersion(set));

        // Likewise, but with the "0:0" cardinality value
        set = MappingSet.builder().comment("A comment").mappings(new ArrayList<>()).build();
        set.getMappings().add(Mapping.builder().mappingCardinality(MappingCardinality.NONE_TO_NONE).build());
        Assertions.assertEquals(Version.SSSOM_1_1, Version.getCompliantVersion(set));
    }

    @Test
    void testRecogniseSlotsAddedToMappingSetClass() {
        MappingSet set = new MappingSet();

        // similarity_measure on a Mapping is compliant with 1.0
        set.getMappings(true).add(Mapping.builder().similarityMeasure("similarity measure").build());
        Assertions.assertEquals(Version.SSSOM_1_0, Version.getCompliantVersion(set));

        // similarity_measure on a MappingSet requires 1.1
        set.setSimilarityMeasure("similarity measure");
        Assertions.assertEquals(Version.SSSOM_1_1, Version.getCompliantVersion(set));
    }

    @Test
    void testCheckCompliance() {
        MappingSet set = new MappingSet();
        set.setComment("A comment");
        set.setPublicationDate(LocalDate.now());
        Assertions.assertTrue(Version.SSSOM_1_0.isCompliant(set));
        Assertions.assertTrue(Version.SSSOM_1_1.isCompliant(set));

        // Add a mapping with a SSSOM 1.1 slot
        set.getMappings(true).add(Mapping.builder().mappingToolId("tool:id").build());
        Assertions.assertFalse(Version.SSSOM_1_0.isCompliant(set));
        Assertions.assertTrue(Version.SSSOM_1_1.isCompliant(set));
    }

    @Test
    void testEnforceCompliance() {
        MappingSet set = new MappingSet();
        set.setComment("A comment");
        set.setPublicationDate(LocalDate.now());
        set.setMappingToolId("tool:id");
        set.getMappings(true).add(Mapping.builder().subjectType(EntityType.COMPOSED_ENTITY_EXPRESSION).build());

        Version.SSSOM_1_0.enforceCompliance(set);
        Assertions.assertTrue(Version.SSSOM_1_0.isCompliant(set));
        Assertions.assertNull(set.getMappingToolId());
        Assertions.assertNull(set.getMappings().get(0).getSubjectType());
    }
}
