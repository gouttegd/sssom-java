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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.util;

import java.io.IOException;

import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtensionSlotHelperTest {

    @Test
    void testExtensionsToOther() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        MappingSet ms = reader.read();

        ExtensionSlotHelper.toOther(ms, false);
        Assertions.assertEquals("ext_foo=Foo A", ms.getOther());
        Assertions.assertEquals("ext_bar=111|ext_baz=ORGENT:BAZ_0001", ms.getMappings().get(0).getOther());
        Assertions.assertNotNull(ms.getExtensions());
    }

    @Test
    void testExtensionsFromOther() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-otherified-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        MappingSet ms = reader.read();

        Assertions.assertNotNull(ms.getExtensionDefinitions());
        Assertions.assertNull(ms.getExtensions());
        Assertions.assertNull(ms.getMappings().get(0).getExtensions());

        ExtensionSlotHelper.fromOther(ms, false);
        Assertions.assertNotNull(ms.getExtensions());
        Assertions.assertTrue(ms.getExtensions().containsKey("https://example.org/properties/fooProperty"));
        Assertions.assertEquals("Foo A",
                ms.getExtensions().get("https://example.org/properties/fooProperty").asString());
        Assertions.assertEquals("https://example.org/entities/BAZ_0001",
                ms.getMappings().get(0).getExtensions().get("https://example.org/properties/bazProperty").asString());
    }
}
