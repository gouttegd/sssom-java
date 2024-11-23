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

public class SSSOMTListItemFunctionTest {

    MappingFormatter formatter;
    Mapping mapping;

    public SSSOMTListItemFunctionTest() {
        formatter = new MappingFormatter();
        formatter.setStandardSubstitutions();
        formatter.setModifier(new SSSOMTListItemFunction());

        List<String> authors = new ArrayList<String>();
        authors.add("Alice");
        authors.add("Bob");
        mapping = Mapping.builder().authorLabel(authors).build();
    }

    @Test
    void testGetListItem() {
        IMappingTransformer<String> f = formatter.getTransformer("First author: %{author_label|list_item(1)}");
        Assertions.assertEquals("First author: Alice", f.transform(mapping));
    }

    @Test
    void testIndexOutOfBounds() {
        IMappingTransformer<String> f = formatter.getTransformer("Third author: %{author_label|list_item(3)}");
        Assertions.assertEquals("Third author: [Alice, Bob]", f.transform(mapping));
    }

    @Test
    void testNotAList() {
        IMappingTransformer<String> f = formatter.getTransformer("First author: %{subject_label|list_item(1)}");
        mapping.setSubjectLabel("Subject label");

        Assertions.assertEquals("First author: Subject label", f.transform(mapping));
    }
}
