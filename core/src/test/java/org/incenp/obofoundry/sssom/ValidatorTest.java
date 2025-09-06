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
import java.util.EnumSet;

import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidatorTest {

    @Test
    void testMappingsWithMissingRequiredSlots() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-required-slots.sssom.tsv");
        reader.setValidationEnabled(false); // So we can check each mapping ourselves
        MappingSet ms = reader.read();

        ValidationError[] expectedErrors = { ValidationError.MISSING_SUBJECT, ValidationError.MISSING_SUBJECT,
                ValidationError.MISSING_SUBJECT, ValidationError.MISSING_OBJECT, ValidationError.MISSING_OBJECT,
                ValidationError.MISSING_OBJECT, ValidationError.MISSING_PREDICATE,
                ValidationError.MISSING_JUSTIFICATION };

        Validator v = new Validator();
        for ( int i = 0, n = ms.getMappings().size(); i < n; i++ ) {
            Assertions.assertEquals(expectedErrors[i].getMessage(), v.validate(ms.getMappings().get(i)));
        }

        // Same but with the new interface
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_SUBJECT));
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_OBJECT));
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_PREDICATE));
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_JUSTIFICATION));
        Assertions.assertEquals(4, errors.size());
    }

    @Test
    void testMappingsWithInvalidPredicateTypes() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-predicate-types.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        String[] expectedErrors = { null, null, ValidationError.INVALID_PREDICATE_TYPE.getMessage(),
                ValidationError.INVALID_PREDICATE_TYPE.getMessage() };

        Validator v = new Validator();
        for ( int i = 0, n = ms.getMappings().size(); i < n; i++ ) {
            Assertions.assertEquals(expectedErrors[i], v.validate(ms.getMappings().get(i)));
        }

        // Same but with the new interface
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.INVALID_PREDICATE_TYPE));
        Assertions.assertEquals(1, errors.size());
    }

    @Test
    void testMissingRequiredSetSlots() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-required-set-slots.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();
        Validator v = new Validator();

        // Check the entire set
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_SET_ID));
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_LICENSE));
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_SUBJECT));
        Assertions.assertEquals(3, errors.size());

        // Check the mapping set only
        errors = v.validate(ms, false);
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_SET_ID));
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_LICENSE));
        Assertions.assertEquals(2, errors.size());
    }

    @Test
    void testRecordIds() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-record-ids.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        Validator v = new Validator();
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void testMissingRecordIds() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-record-ids-mix.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        Validator v = new Validator();
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_RECORD_ID));
    }

    @Test
    void testLastRecordMissingRecordID() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-record-ids.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        // Remove the last record ID
        ms.getMappings().get(ms.getMappings().size() - 1).setRecordId(null);

        Validator v = new Validator();
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_RECORD_ID));
    }

    @Test
    void testAllRecordsMissingRecordIDExceptLast() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-record-ids.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        // Remove all record IDs except for the last record
        for ( int i = 0, n = ms.getMappings().size(); i < n - 1; n++ ) {
            ms.getMappings().get(i).setRecordId(null);
        }

        Validator v = new Validator();
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.MISSING_RECORD_ID));
    }

    @Test
    void testDuplicatedRecordIds() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-record-ids-duplicate.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        Validator v = new Validator();
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.contains(ValidationError.DUPLICATED_RECORD_ID));
    }

    @Test
    void testDisabledValidation() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-required-slots.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        Validator v = new Validator(ValidationLevel.DISABLED);
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void testMinimalValidation() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-required-slots.sssom.tsv");
        reader.setValidationEnabled(false);
        MappingSet ms = reader.read();

        Validator v = new Validator(ValidationLevel.MINIMAL);
        EnumSet<ValidationError> errors = v.validate(ms);
        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertFalse(errors.contains(ValidationError.MISSING_SET_ID));
        Assertions.assertFalse(errors.contains(ValidationError.MISSING_LICENSE));
    }
}
