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

package org.incenp.obofoundry.sssom;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MappingTest {

    @Test
    void testListAccessors() {
        Mapping m = new Mapping();

        Assertions.assertNull(m.getSeeAlso());
        m.getSeeAlso(true).add("seeAlso1");
        Assertions.assertEquals("seeAlso1", m.getSeeAlso().get(0));

        m.getSeeAlso(true).add("seeAlso2");
        Assertions.assertEquals("seeAlso1", m.getSeeAlso().get(0));
        Assertions.assertEquals("seeAlso2", m.getSeeAlso().get(1));
    }

    @Test
    void testExtensionAccessor() {
        Mapping m = new Mapping();
        Assertions.assertNull(m.getExtensions());

        m.getExtensions(true).put("myext", new ExtensionValue("my-value"));
        Assertions.assertTrue(m.getExtensions().containsKey("myext"));
    }
}
