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

import java.time.LocalDate;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MappingHasherTest {

    @Test
    void testZBase32Encoding() {
        byte[] hello = { 'H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!', '\n' };
        Assertions.assertEquals("jb1sa5dxfoofq551pt1nnno", MappingHasher.toZBase32(hello));
    }

    @Test
    void testSimpleHashing() {
        Mapping m = new Mapping();
        m.setSubjectId("SUBJECT");
        m.getAuthorId(true).add("AUTHOR");
        m.setConfidence(0.7);
        m.getExtensions(true).put("PROPERTY", new ExtensionValue(LocalDate.of(2025, 6, 1)));

        // Expected ZBase32-encoded SHA2-256 hash of
        // "(7:mapping((10:subject_id7:SUBJECT)(9:author_id(6:AUTHOR))(10:confidence3:0.7)(10:extensions((8:PROPERTY10:2025-06-01)))))"
        //
        // Obtained with:
        // echo -n "<S-EXPRESSION>" | openssl dgst -sha256 -binary | zbase32
        String hash = "4jfngj8y8bh9fu7ahhj9ic6miqz78cskxhyo61zkgb3gjte3ocuo";
        Assertions.assertEquals(hash, new MappingHasher().hash(m));
    }
}
