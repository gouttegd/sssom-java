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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.rdf;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.rdf4j.model.Model;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RDFConverterTest {

    @Test
    void testNamespaceDeclarations() throws SSSOMFormatException, IOException {
        MappingSet ms = getSampleSet();
        RDFConverter converter = new RDFConverter();

        Assertions.assertTrue(converter.toRDF(ms).getNamespaces().isEmpty());
        Assertions.assertTrue(converter.toRDF(ms, false).getNamespaces().isEmpty());

        Model model = converter.toRDF(ms, true);
        Assertions.assertTrue(model.getNamespace("ORGENT").isPresent());
    }

    @Test
    void testCustomNamespaceDeclarations() throws SSSOMFormatException, IOException {
        MappingSet ms = getSampleSet();
        RDFConverter converter = new RDFConverter();

        HashMap<String, String> prefixMap = new HashMap<String, String>();
        prefixMap.put("UNUSED", "http://example.org/unused/prefix/");

        Model model = converter.toRDF(ms, prefixMap);
        Assertions.assertTrue(model.getNamespace("owl").isPresent());
        Assertions.assertTrue(model.getNamespace("UNUSED").isEmpty());
        Assertions.assertTrue(model.getNamespace("ORGENT").isEmpty());

        PrefixManager pm = new PrefixManager();
        pm.add(prefixMap);
        model = converter.toRDF(ms, pm);
        Assertions.assertTrue(model.getNamespace("owl").isPresent());
        Assertions.assertTrue(model.getNamespace("UNUSED").isEmpty());
        Assertions.assertTrue(model.getNamespace("ORGENT").isEmpty());
    }

    @Test
    void testRoundtripConversion() throws SSSOMFormatException, IOException {
        MappingSet ms = getSampleSet();
        RDFConverter converter = new RDFConverter();

        Model model = converter.toRDF(ms, true);
        MappingSet back = converter.fromRDF(model);

        Assertions.assertEquals(ms, back);
    }

    private MappingSet getSampleSet() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c.sssom.tsv");
        MappingSet ms = reader.read();

        // We force the creator_id slot to be lexicographically sorted
        ms.getCreatorId().sort((a, b) -> a.compareTo(b));

        return ms;
    }
}
