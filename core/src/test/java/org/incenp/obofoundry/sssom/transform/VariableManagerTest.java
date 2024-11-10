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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableManagerTest {

    VariableManager mgr = new VariableManager();

    @Test
    void testUndefinedVariable() {
        Assertions.assertFalse(mgr.hasVariable("UNKNOWN"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> mgr.expandVariable("UNKNOWN", getSampleMapping()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> mgr.getTransformer("UNKNOWN"));
    }

    @Test
    void testBasicExpansion() {
        mgr.addVariable("MYVAR", "default value");
        mgr.addVariable("MYVAR", "value for example.org", (m) -> m.getSubjectId().startsWith("https://example.org/"));

        Mapping m = getSampleMapping();
        Assertions.assertEquals("value for example.org", mgr.expandVariable("MYVAR", m));

        m.setSubjectId("https://example.com/entities/0011");
        Assertions.assertEquals("default value", mgr.expandVariable("MYVAR", m));
    }

    @Test
    void testDelayedExpansion() {
        mgr.addVariable("MYVAR", "default value");
        mgr.addVariable("MYVAR", "value for example.org", (m) -> m.getSubjectId().startsWith("https://example.org/"));

        Mapping m = getSampleMapping();
        Assertions.assertEquals("value for example.org", mgr.getTransformer("MYVAR").transform(m));

        m.setSubjectId("https://example.com/entities/0011");
        Assertions.assertEquals("default value", mgr.getTransformer("MYVAR").transform(m));
    }

    @Test
    void testNoDefaultValue() {
        mgr.addVariable("MYVAR", "value for example.org", (m) -> m.getSubjectId().startsWith("https://example.org/"));

        Mapping m = getSampleMapping();
        Assertions.assertEquals("value for example.org", mgr.expandVariable("MYVAR", m));

        m.setSubjectId("https://example.com/entities/0011");
        Assertions.assertEquals("", mgr.expandVariable("MYVAR", m));
        Assertions.assertEquals("", mgr.getTransformer("MYVAR").transform(m));
    }

    @Test
    void testExpandToLastAddedMatchingFilter() {
        mgr.addVariable("MYVAR", "default value");
        mgr.addVariable("MYVAR", "value for example.org", (m) -> m.getSubjectId().startsWith("https://example.org/"));
        mgr.addVariable("MYVAR", "value for skos:closeMatch",
                (m) -> m.getPredicateId().equals("http://www.w3.org/2004/02/skos/core#closeMatch"));
        mgr.addVariable("MYVAR", "value for example.com", (m) -> m.getSubjectId().startsWith("https://example.com/"));

        Mapping m = getSampleMapping();
        Assertions.assertEquals("value for skos:closeMatch", mgr.expandVariable("MYVAR", m));
    }

    private Mapping getSampleMapping() {
        // @formatter:off
        Mapping m = Mapping.builder()
                .subjectId("https://example.org/entities/0001")
                .subjectLabel("alice")
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("https://example.com/entities/0011")
                .objectLabel("alpha")
                .build();
        // @formatter:on

        return m;
    }
}
