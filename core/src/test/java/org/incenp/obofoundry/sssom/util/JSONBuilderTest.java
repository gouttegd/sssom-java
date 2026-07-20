/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONBuilderTest {

    private JSONBuilder builder = new JSONBuilder();

    @Test
    void buildStringObject() {
        builder.addValue("test");

        Assertions.assertEquals("\"test\"\n", builder.close());
    }

    @Test
    void buildListObject() {
        builder.startList();
        builder.addValue("item 1");
        builder.addValue("item 2");

        Assertions.assertEquals("[\n  \"item 1\",\n  \"item 2\"\n]\n", builder.close());
    }

    @Test
    void buildDictObject() {
        builder.addKey("key 1");
        builder.addValue("value 1");

        Assertions.assertEquals("{\n  \"key 1\": \"value 1\"\n}\n", builder.close());
    }

    @Test
    void buildEmptyList() {
        builder.startList();

        Assertions.assertEquals("[]\n", builder.close());
    }

    @Test
    void buildEmptyDict() {
        builder.startDict();

        Assertions.assertEquals("{}\n", builder.close());
    }
}
