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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONWriterTest {

    /*
     * Basic test of the JSON writer, using the "exo2c" sample set.
     */
    @Test
    void testSimpleWrite() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c.sssom.tsv");
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "exo2c", null, null);
    }

    /*
     * Likewise, but testing that we can correctly serialise non-standard metadata.
     */
    @Test
    void testSimpleWriteWithExtensions() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-extensions-defined", null, ExtraMetadataPolicy.DEFINED);
        assertWrittenAsExpected(ms, "test-extensions-undefined", null, ExtraMetadataPolicy.UNDEFINED);
        assertWrittenAsExpected(ms, "test-extensions-none", null, ExtraMetadataPolicy.NONE);
    }

    /*
     * Checks that a mapping set is written exactly as we expect. This method will
     * write the provided set to a temporary file and compares the written file with
     * a theoretical file, and raises an assertion failure if the two files do not
     * have exactly the same contents.
     * 
     * The theoretical file should be either in src/test/resources/output/ or in
     * src/test/resources/sets/, with a .sssom.json extension.
     * 
     * The temporary file will be written in src/test/resources/output/, with a
     * .sssom.json.out extension. If actualBasename is null, the basename of the
     * theoretical (expected) file will be used. The temporary file will be
     * automatically deleted if it is found to be identical to the theoretical file.
     */
    private void assertWrittenAsExpected(MappingSet ms, String expectedBasename, String actualBasename,
            ExtraMetadataPolicy extraPolicy) throws IOException {
        if ( actualBasename == null ) {
            actualBasename = expectedBasename;
        }

        File written = new File("src/test/resources/output/" + actualBasename + ".sssom.json.out");
        JSONWriter writer = new JSONWriter(written);
        if ( extraPolicy != null ) {
            writer.setExtraMetadataPolicy(extraPolicy);
        }
        writer.write(ms.toBuilder().build());

        File expected = new File("src/test/resources/output/" + expectedBasename + ".sssom.json");
        if ( !expected.exists() ) {
            expected = new File("src/test/resources/sets/" + expectedBasename + ".sssom.json");
        }

        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }
}
