/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import picocli.CommandLine.TypeConversionException;

public class AnnotationConverterTest {

    AnnotationOptionConverter converter = new AnnotationOptionConverter();

    @Test
    void testBaseFeature() {
        AnnotationOption opt = converter.convert("creator_id=creator");
        Assertions.assertEquals("creator_id", opt.slot.getName());
        Assertions.assertEquals("creator", opt.value);
        Assertions.assertFalse(opt.append);
    }

    @Test
    void testAppendFlag() {
        AnnotationOption opt = converter.convert("creator_id+=creator");
        Assertions.assertEquals("creator_id", opt.slot.getName());
        Assertions.assertEquals("creator", opt.value);
        Assertions.assertTrue(opt.append);
    }

    @Test
    void testEmptyValue() {
        AnnotationOption opt = converter.convert("creator_id=");
        Assertions.assertEquals("creator_id", opt.slot.getName());
        Assertions.assertNull(opt.value);
        Assertions.assertFalse(opt.append);

        try {
            converter.convert("creator_id+=");
            Assertions.fail("Invalid use of append flag with no value not caught");
        } catch ( TypeConversionException e ) {
        }
    }

    @Test
    void testDetectMissingSeparator() {
        try {
            converter.convert("noseparator");
            Assertions.fail("Exception not thrown for a missing separator character");
        } catch ( TypeConversionException e ) {
        }
    }

    @Test
    void testInvalidSlotName() {
        try {
            converter.convert("invalidslot=value");
            Assertions.fail("Exception not thrown for an invalid slot name");
        } catch ( TypeConversionException e ) {
        }
    }
}
