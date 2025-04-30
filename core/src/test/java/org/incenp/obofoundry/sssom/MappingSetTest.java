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
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MappingSetTest {

    @Test
    void testListAccessors() {
        MappingSet set = new MappingSet();

        Assertions.assertNull(set.getCreatorLabel());
        set.getCreatorLabel(true).add("A. U. Thor");
        Assertions.assertEquals("A. U. Thor", set.getCreatorLabel().get(0));

        Assertions.assertNull(set.getMappings());
        set.getMappings(true).add(Mapping.builder().subjectId("subject1").build());
        Assertions.assertEquals("subject1", set.getMappings().get(0).getSubjectId());
    }

    @Test
    void testExtensionAccessor() {
        MappingSet set = new MappingSet();

        Assertions.assertNull(set.getExtensions());
        set.getExtensions(true).put("myext", new ExtensionValue("my-value"));
        Assertions.assertTrue(set.getExtensions().containsKey("myext"));
    }

    @Test
    void testCurieMapAccessor() {
        MappingSet set = new MappingSet();

        Assertions.assertNull(set.getCurieMap());
        set.getCurieMap(true).put("name", "prefix");
        Assertions.assertTrue(set.getCurieMap().containsKey("name"));
    }
}
