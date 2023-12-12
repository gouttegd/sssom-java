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

package org.incenp.obofoundry.sssom.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Test;

public class MappingEditorTest {

    @Test
    void testBasicChange() {
        MappingEditor editor = new MappingEditor();
        editor.addSimpleAssign("object_id", "https://example.org/anotherObject");

        Mapping m1 = getSampleMapping();
        Mapping edited = editor.transform(m1);

        assertNotEquals(m1.getObjectId(), edited.getObjectId());
        assertEquals("https://example.org/anotherObject", edited.getObjectId());
    }

    @Test
    void testSettingNoValue() {
        MappingEditor editor = new MappingEditor();
        editor.addSimpleAssign("mapping_justification", null);
        assertNull(editor.transform(getSampleMapping()).getMappingJustification());

        editor.addSimpleAssign("mapping_justification", "");
        assertNull(editor.transform(getSampleMapping()).getMappingJustification());

        assertThrows(IllegalArgumentException.class, () -> editor.addSimpleAssign("object_id", null));
    }

    @Test
    void testReplacement() {
        MappingEditor editor = new MappingEditor();
        editor.addReplacement("object_id", "example.org/([a-z]+)$", "example.net/$1");
        assertEquals("https://example.net/object", editor.transform(getSampleMapping()).getObjectId());
    }

    private Mapping getSampleMapping() {
        // @formatter:off
        Mapping m = Mapping.builder()
                .subjectId("https://example.org/subject")
                .predicateId(CommonPredicate.SKOS_EXACT_MATCH.name())
                .objectId("https://example.org/object")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build();
        // @formatter:on

        return m;
    }
}
