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

public class SSSOMTSuffixFunctionTest {

    MappingFormatter formatter;

    SSSOMTSuffixFunctionTest() {
        PrefixManager pm = new PrefixManager();
        pm.add("ORGPID", "https://example.org/people/");
        pm.add("COMPID", "https://example.com/people/");
        formatter = new MappingFormatter();
        formatter.setStandardSubstitutions();
        formatter.setModifier(new SSSOMTSuffixFunction(pm));
        formatter.setModifier(new SSSOMTFlattenFunction());
    }

    @Test
    void testSingleSuffixExtraction() {
        Mapping m = new Mapping();
        m.setPredicateId("http://www.w3.org/2004/02/skos/core#exactMatch");

        IMappingTransformer<String> f = formatter.getTransformer("Predicate local name: %{predicate_id|suffix}");
        Assertions.assertEquals("Predicate local name: exactMatch", f.transform(m));
    }

    @Test
    void testMultipleSuffixExtraction() {
        Mapping m = new Mapping();
        m.getAuthorId(true).add("https://example.org/people/0001");
        m.getAuthorId().add("https://example.com/people/0002");

        IMappingTransformer<String> f = formatter.getTransformer("Authors local names: %{author_id|suffix|flatten}");
        Assertions.assertEquals("Authors local names: 0001, 0002", f.transform(m));
    }
}
