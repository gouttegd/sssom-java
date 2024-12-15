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

public class RDFSerialiserTest {

    @Test
    void testNamespaceDeclarations() throws SSSOMFormatException, IOException {
        MappingSet ms = getSampleSet();
        RDFSerialiser serialiser = new RDFSerialiser();

        Assertions.assertTrue(serialiser.toRDF(ms).getNamespaces().isEmpty());
        Assertions.assertTrue(serialiser.toRDF(ms, false).getNamespaces().isEmpty());

        Model model = serialiser.toRDF(ms, true);
        Assertions.assertTrue(model.getNamespace("ORGENT").isPresent());
    }

    @Test
    void testCustomNamespaceDeclarations() throws SSSOMFormatException, IOException {
        MappingSet ms = getSampleSet();
        RDFSerialiser serialiser = new RDFSerialiser();

        HashMap<String, String> prefixMap = new HashMap<String, String>();
        prefixMap.put("UNUSED", "http://example.org/unused/prefix/");

        Model model = serialiser.toRDF(ms, prefixMap);
        Assertions.assertTrue(model.getNamespace("owl").isPresent());
        Assertions.assertTrue(model.getNamespace("UNUSED").isEmpty());
        Assertions.assertTrue(model.getNamespace("ORGENT").isEmpty());

        PrefixManager pm = new PrefixManager();
        pm.add(prefixMap);
        model = serialiser.toRDF(ms, pm);
        Assertions.assertTrue(model.getNamespace("owl").isPresent());
        Assertions.assertTrue(model.getNamespace("UNUSED").isEmpty());
        Assertions.assertTrue(model.getNamespace("ORGENT").isEmpty());
    }

    private MappingSet getSampleSet() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c.sssom.tsv");
        return reader.read();
    }
}
