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

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CSVWWriterTest {

    @Test
    void testSimpleWrite() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c.sssom.tsv");
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "exo2c", null, null);
    }

    @Test
    void testWriteExtensions() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "exo2c-with-extensions", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED));
    }

    private void assertWrittenAsExpected(MappingSet ms, String expectedBasename, String actualBasename,
            Consumer<CSVWWriter> consumer)
            throws IOException {
        if ( actualBasename == null ) {
            actualBasename = expectedBasename;
        }

        File csvWritten = new File("src/test/resources/output/" + actualBasename + ".sssom.csvw.out");
        File metaWritten = new File("src/test/resources/output/" + actualBasename + ".sssom.csvw-metadata.json.out");
        CSVWWriter writer = new CSVWWriter(csvWritten, metaWritten);
        writer.setCSVUrl(actualBasename + ".sssom.csvw");
        if ( consumer != null ) {
            consumer.accept(writer);
        }
        writer.write(ms.toBuilder().build());

        File csvExpected = new File("src/test/resources/output/" + expectedBasename + ".sssom.csvw");
        if ( !csvExpected.exists() ) {
            csvExpected = new File("src/test/resources/sets/" + expectedBasename + ".sssom.csvw");
        }
        File metaExpected = new File(csvExpected.getAbsolutePath() + "-metadata.json");

        Assertions.assertTrue(FileUtils.contentEquals(csvExpected, csvWritten));
        csvWritten.delete();

        Assertions.assertTrue(FileUtils.contentEquals(metaExpected, metaWritten));
        metaWritten.delete();
    }
}
