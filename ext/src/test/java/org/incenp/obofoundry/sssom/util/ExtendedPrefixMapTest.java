/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.util;

import java.io.IOException;
import java.util.Map;

import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtendedPrefixMapTest {

    @Test
    void testRead() throws IOException {
        ExtendedPrefixMap epm = new ExtendedPrefixMap("src/main/resources/obo.epm.json");

        Map<String, String> pm = epm.getSimplePrefixMap();
        Assertions.assertTrue(pm.containsKey("FBbt"));
        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_", pm.get("FBbt"));
    }

    @Test
    void testCanonicalisation() throws IOException {
        ExtendedPrefixMap epm = new ExtendedPrefixMap("src/main/resources/obo.epm.json");

        // Check canonicalisation of a synonym-using IRI
        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_1234",
                epm.canonicalise("http://flybase.org/cgi-bin/fbcvq.html?query=FBbt:1234"));

        // Check canonicalisation of an IRI already using the canonical prefix
        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_5678",
                epm.canonicalise("http://purl.obolibrary.org/obo/FBbt_5678"));
    }

    @Test
    void testMappingSetCanonicalisation() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/fbbt-uncanonical-urls.sssom.tsv");
        MappingSet ms = reader.read();

        ExtendedPrefixMap epm = new ExtendedPrefixMap("src/main/resources/obo.epm.json");
        epm.canonicalise(ms);

        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_00000001", ms.getMappings().get(0).getSubjectId());
    }
}
