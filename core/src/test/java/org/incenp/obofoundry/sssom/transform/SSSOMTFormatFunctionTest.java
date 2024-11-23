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

package org.incenp.obofoundry.sssom.transform;

import java.util.ArrayList;
import java.util.List;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SSSOMTFormatFunctionTest {

    MappingFormatter formatter;

    SSSOMTFormatFunctionTest() {
        formatter = new MappingFormatter();
        formatter.setStandardSubstitutions();
        formatter.setModifier(new SSSOMTFormatFunction());
    }

    @Test
    void testFormatOnNumericalSlots() {
        Mapping m = new Mapping();
        m.setConfidence(0.3);

        IMappingTransformer<String> f = formatter.getTransformer("Confidence: %{confidence|format(%.3f)}");
        Assertions.assertEquals("Confidence: 0.300", f.transform(m));
    }

    @Test
    void testFormatOnIDSlots() {
        Mapping m = new Mapping();
        m.setSubjectId("https://example.org/entities/0001");

        IMappingTransformer<String> f = formatter.getTransformer("Subject: %{subject_id|format(<%s>)}");
        Assertions.assertEquals("Subject: <https://example.org/entities/0001>", f.transform(m));
    }

    @Test
    void testFormatOnListSlots() {
        Mapping m = new Mapping();
        List<String> authors = new ArrayList<String>();
        authors.add("https://example.org/people/0000-0000-0001-1234");
        authors.add("https://example.com/people/0000-0000-0002-5678");
        m.setAuthorId(authors);

        IMappingTransformer<String> f = formatter.getTransformer("Authors: %{author_id|format(<%s>)}");
        Assertions.assertEquals(
                "Authors: [<https://example.org/people/0000-0000-0001-1234>, <https://example.com/people/0000-0000-0002-5678>]",
                f.transform(m));
    }
}
