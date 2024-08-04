/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
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

import java.util.ArrayList;
import java.util.List;

import org.incenp.obofoundry.sssom.model.Constants;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CardinalityTest {

    private Mapping getSampleMapping(String subject, String object) {
        return Mapping.builder().subjectId(subject).objectId(object).build();
    }

    @Test
    void testComputeCardinality() {
        List<Mapping> mappings = new ArrayList<Mapping>();
        
        // one-to-one mappings
        mappings.add(getSampleMapping("subject1", "object1"));
        mappings.add(getSampleMapping("subject1", "object1"));

        // one-to-many mappings (subject2 mapped to object2 and object3)
        mappings.add(getSampleMapping("subject2", "object2"));
        mappings.add(getSampleMapping("subject2", "object3"));

        // many-to-one mappings (subject3 and subject4 both mapped to object 4)
        mappings.add(getSampleMapping("subject3", "object4"));
        mappings.add(getSampleMapping("subject4", "object4"));

        // many-to-many mapping (subject5 mapped to both object5 and object6, object5
        // mapped to subject5 and subject6)
        mappings.add(getSampleMapping("subject5", "object5"));

        // one-to-many mapping (subject5 mapped to both object5 and object6, object6
        // mapped to only subject5)
        mappings.add(getSampleMapping("subject5", "object6")); // 1:n

        // many-to-one mapping (subject5 and subject6 both mapped to object5)
        mappings.add(getSampleMapping("subject6", "object5")); // n:1

        MappingCardinality.inferCardinality(mappings);
        Assertions.assertEquals(MappingCardinality.ONE_TO_ONE, mappings.get(0).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.ONE_TO_ONE, mappings.get(1).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.ONE_TO_MANY, mappings.get(2).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.ONE_TO_MANY, mappings.get(3).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.MANY_TO_ONE, mappings.get(4).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.MANY_TO_ONE, mappings.get(5).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.MANY_TO_MANY, mappings.get(6).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.ONE_TO_MANY, mappings.get(7).getMappingCardinality());
        Assertions.assertEquals(MappingCardinality.MANY_TO_ONE, mappings.get(8).getMappingCardinality());
    }

    @Test
    void testMappingsWithNoTermFoundAreIgnored() {
        List<Mapping> mappings = new ArrayList<Mapping>();

        mappings.add(getSampleMapping("subject1", "object2"));
        mappings.add(getSampleMapping("subject1", Constants.NoTermFound));
        mappings.add(getSampleMapping(Constants.NoTermFound, "object2"));

        MappingCardinality.inferCardinality(mappings);
        Assertions.assertEquals(MappingCardinality.ONE_TO_ONE, mappings.get(0).getMappingCardinality());
        Assertions.assertNull(mappings.get(1).getMappingCardinality());
        Assertions.assertNull(mappings.get(2).getMappingCardinality());
    }
}
