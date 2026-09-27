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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerialisationFormatTest {

    @Test
    void testInferFromFilename() {
        Assertions.assertEquals(SerialisationFormat.TSV, SerialisationFormat.fromFilename("file.sssom.tsv"));
        Assertions.assertEquals(SerialisationFormat.RDF_TURTLE, SerialisationFormat.fromFilename("file.ttl"));
        Assertions.assertNull(SerialisationFormat.fromFilename("file"));
    }

    @Test
    void testInferFromFilenameThroughCompression() {
        Assertions.assertEquals(SerialisationFormat.TSV, SerialisationFormat.fromFilename("file.sssom.tsv.gz"));

        Assertions.assertEquals(SerialisationFormat.JSON,
                SerialisationFormat.fromFilename("file.sssom.json.gz", true, null));
        Assertions.assertNull(SerialisationFormat.fromFilename("file.sssom.json.gz", false, null));
    }
}
