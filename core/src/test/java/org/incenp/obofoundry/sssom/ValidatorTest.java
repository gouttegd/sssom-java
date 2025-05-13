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

package org.incenp.obofoundry.sssom;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidatorTest {

    @Test
    void testMappingsWithMissingRequiredSlots() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-required-slots.sssom.tsv");
        reader.setValidationEnabled(false); // So we can check each mapping ourselves
        MappingSet ms = reader.read();

        String[] expectedErrors = { Validator.MISSING_SUBJECT_ID, Validator.MISSING_SUBJECT_ID,
                Validator.MISSING_SUBJECT_LABEL, Validator.MISSING_OBJECT_ID, Validator.MISSING_OBJECT_ID,
                Validator.MISSING_OBJECT_LABEL, Validator.MISSING_PREDICATE, Validator.MISSING_JUSTIFICATION };

        Validator v = new Validator();
        for ( int i = 0, n = ms.getMappings().size(); i < n; i++ ) {
            Assertions.assertEquals(expectedErrors[i], v.validate(ms.getMappings().get(i)));
        }
    }

    @Test
    void testMappingsWithInvalidPredicateTypes() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-predicate-types.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        String[] expectedErrors = { null, null, Validator.INVALID_PREDICATE_TYPE, Validator.INVALID_PREDICATE_TYPE };

        Validator v = new Validator();
        for ( int i = 0, n = ms.getMappings().size(); i < n; i++ ) {
            Assertions.assertEquals(expectedErrors[i], v.validate(ms.getMappings().get(i)));
        }
    }

    @Test
    void testGetCompliantVersion() {
        MappingSet set = new MappingSet();
        Validator v = new Validator();

        // Empty set is compliant with 1.0
        Assertions.assertEquals(Version.SSSOM_1_0, v.getCompliantVersion(set));

        // A set with only slots from 1.0 is compliant with 1.0
        set.setComment("A comment");
        set.setPublicationDate(LocalDate.now());
        Assertions.assertEquals(Version.SSSOM_1_0, v.getCompliantVersion(set));

        // A set with a slot from 1.1 is compliant with 1.1
        set.setPredicateType(EntityType.RDF_PROPERTY);
        Assertions.assertEquals(Version.SSSOM_1_1, v.getCompliantVersion(set));

        // Unless the slot is sssom_version
        set = MappingSet.builder().sssomVersion(Version.SSSOM_1_1).build();
        Assertions.assertEquals(Version.SSSOM_1_0, v.getCompliantVersion(set));

        // A set with only 1.0 metadata slots but containing mappings with 1.1 slots
        // requires 1.1
        set = MappingSet.builder().comment("A comment").mappings(new ArrayList<>()).build();
        set.getMappings().add(Mapping.builder().subjectType(EntityType.COMPOSED_ENTITY_EXPRESSION).build());
        Assertions.assertEquals(Version.SSSOM_1_1, v.getCompliantVersion(set));
    }
}
