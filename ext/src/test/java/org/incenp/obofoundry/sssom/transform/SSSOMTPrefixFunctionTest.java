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

package org.incenp.obofoundry.sssom.transform;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SSSOMTPrefixFunctionTest {

    MappingFormatter formatter;

    SSSOMTPrefixFunctionTest() {
        PrefixManager pm = new PrefixManager();
        pm.add("ORGPID", "https://example.org/people/");
        pm.add("COMPID", "https://example.com/people/");
        formatter = new MappingFormatter();
        formatter.setStandardSubstitutions();
        formatter.setModifier(new SSSOMTPrefixFunction(pm));
        formatter.setModifier(new SSSOMTFlattenFunction());
    }

    @Test
    void testSinglePrefixExtraction() {
        Mapping m = new Mapping();
        m.setPredicateId("http://www.w3.org/2004/02/skos/core#exactMatch");

        IMappingTransformer<String> f = formatter.getTransformer("Predicate prefix: %{predicate_id|prefix}");
        Assertions.assertEquals("Predicate prefix: skos", f.transform(m));
    }

    @Test
    void testMultiplePrefixExtraction() {
        Mapping m = new Mapping();
        m.getAuthorId(true).add("https://example.org/people/0001");
        m.getAuthorId().add("https://example.com/people/0002");

        IMappingTransformer<String> f = formatter.getTransformer("Authors prefixes: %{author_id|prefix|flatten}");
        Assertions.assertEquals("Authors prefixes: ORGPID, COMPID", f.transform(m));
    }
}
