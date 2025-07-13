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

import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.MappingHasher;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ValueExtractorTest {

    private MappingSet testSet;
    private ValueExtractorFactory factory = new ValueExtractorFactory();

    public ValueExtractorTest() {
        try {
            TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
            reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
            testSet = reader.read();
        } catch ( Exception e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testSetSlotExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("set.slot(license)");
        Assertions.assertEquals("https://creativecommons.org/licenses/by/4.0/", e.extract(testSet));
    }

    @Test
    void testSetMultiValuedSlotExtraction() throws ExtractorSyntaxException {
        // Explicit index
        IValueExtractor e = factory.parse("set.slot(creator_id, 2)");
        Assertions.assertEquals("https://example.org/people/0000-0000-0001-1234", e.extract(testSet));

        // Default index
        e = factory.parse("set.slot(creator_id)");
        Assertions.assertEquals("https://example.com/people/0000-0000-0002-5678", e.extract(testSet));

        // Negative index
        e = factory.parse("set.slot(creator_id, -2)"); // first item
        Assertions.assertEquals("https://example.com/people/0000-0000-0002-5678", e.extract(testSet));

        // Out of bound index
        e = factory.parse("set.slot(creator_id, -3)");
        Assertions.assertNull(e.extract(testSet));
    }

    @Test
    void testUnsetSlotExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("set.slot(mapping_tool)");
        Assertions.assertNull(e.extract(testSet));
    }

    @Test
    void testSetExtensionExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("set.extension(http://sssom.invalid/ext_undeclared_foo)");
        Object v = e.extract(testSet);
        Assertions.assertInstanceOf(ExtensionValue.class, v);
        ExtensionValue ev = (ExtensionValue) v;
        Assertions.assertEquals("Foo B", ev.asString());
    }

    @Test
    void testUnsetExtensionExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("set.extension(http://sssom.invalid/not-present)");
        Assertions.assertNull(e.extract(testSet));
    }

    @Test
    void testMappingSlotExtraction() throws ExtractorSyntaxException {
        // Default index
        IValueExtractor e = factory.parse("mapping.slot(subject_label)");
        Assertions.assertEquals("alice", e.extract(testSet));

        // Explicit index
        e = factory.parse("mapping(2).slot(subject_id)");
        Assertions.assertEquals("https://example.org/entities/0002", e.extract(testSet));

        // Negative index
        e = factory.parse("mapping(-4).slot(subject_label)");
        Assertions.assertEquals("fanny", e.extract(testSet));

        // Out of bound index
        e = factory.parse("mapping(9).slot(subject_label)");
        Assertions.assertNull(e.extract(testSet));
    }

    @Test
    void testMappingExtensionExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping.extension(https://example.org/properties/barProperty)");
        Object v = e.extract(testSet);
        Assertions.assertInstanceOf(ExtensionValue.class, v);
        ExtensionValue ev = (ExtensionValue) v;
        Assertions.assertEquals(ValueType.INTEGER, ev.getType());
        Assertions.assertEquals(111, ev.asInteger());
    }

    @Test
    void testSExpressionExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping.special(sexpr)");
        Assertions.assertEquals(testSet.getMappings().get(0).toSExpr(), e.extract(testSet));
    }

    @Test
    void testHashExtraction() throws ExtractorSyntaxException {
        IValueExtractor e = factory.parse("mapping.special(hash)");
        String expected = new MappingHasher().hash(testSet.getMappings().get(0));
        Assertions.assertEquals(expected, e.extract(testSet));
    }
}
