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

package org.incenp.obofoundry.sssom.extract;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValueExtractorFactoryTest {

    private ValueExtractorFactory factory = new ValueExtractorFactory();

    @Test
    void testSetSlotExtractor() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("set.slot.mapping_set_id");

        Assertions.assertInstanceOf(MappingSetSlotExtractor.class, e);
        MappingSetSlotExtractor ce = (MappingSetSlotExtractor) e;
        Assertions.assertEquals("mapping_set_id", ce.slotExtractor.slot.getName());
        Assertions.assertEquals(String.class, e.getType());
    }

    @Test
    void testSetExtensionExtractor() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("set.extension(https://example.org/fooProperty)");

        Assertions.assertInstanceOf(MappingSetExtensionExtractor.class, e);
        MappingSetExtensionExtractor ce = (MappingSetExtensionExtractor) e;
        Assertions.assertEquals("https://example.org/fooProperty", ce.property);
        Assertions.assertEquals(ExtensionValue.class, e.getType());
    }

    @Test
    void testMappingSlotExtractor() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping(7).slot.creator_id(2)");

        Assertions.assertInstanceOf(MappingSlotExtractor.class, e);
        MappingSlotExtractor ce = (MappingSlotExtractor) e;
        Assertions.assertEquals(6, ce.mappingNo);
        Assertions.assertEquals("creator_id", ce.slotExtractor.slot.getName());
        Assertions.assertEquals(String.class, e.getType());
    }

    @Test
    void testMappingExtensionExtractor() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping(2).extension(https://example.org/fooProperty)");

        Assertions.assertInstanceOf(MappingExtensionExtractor.class, e);
        MappingExtensionExtractor ce = (MappingExtensionExtractor) e;
        Assertions.assertEquals(1, ce.mappingNo);
        Assertions.assertEquals("https://example.org/fooProperty", ce.property);
        Assertions.assertEquals(ExtensionValue.class, e.getType());
    }

    @Test
    void testMappingSExprExtractor() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping(1).special.sexpr");

        Assertions.assertInstanceOf(SExpressionExtractor.class, e);
        SExpressionExtractor ce = (SExpressionExtractor) e;
        Assertions.assertEquals(0, ce.mappingNo);
        Assertions.assertEquals(String.class, e.getType());
    }

    @Test
    void testMappingHashExtractor() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping(1).special.hash");

        Assertions.assertInstanceOf(HashExtractor.class, e);
        HashExtractor ce = (HashExtractor) e;
        Assertions.assertEquals(0, ce.mappingNo);
        Assertions.assertEquals(String.class, e.getType());
    }

    @Test
    void testDefaultMappingNo() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping.slot.subject_id");

        Assertions.assertInstanceOf(MappingSlotExtractor.class, e);
        MappingSlotExtractor ce = (MappingSlotExtractor)e;
        Assertions.assertEquals(0, ce.mappingNo);
    }

    @Test
    void testDefaultItemNo() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping.slot.author_id");

        Assertions.assertInstanceOf(MappingSlotExtractor.class, e);
        MappingSlotExtractor ce = (MappingSlotExtractor) e;
        Assertions.assertEquals(0, ce.slotExtractor.itemNo);
    }

    @Test
    void testNegativeIndex() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping(-2).slot.author_id(-1)");

        Assertions.assertInstanceOf(MappingSlotExtractor.class, e);
        MappingSlotExtractor ce = (MappingSlotExtractor) e;
        Assertions.assertEquals(-2, ce.mappingNo);
        Assertions.assertEquals(-1, ce.slotExtractor.itemNo);
    }
}
