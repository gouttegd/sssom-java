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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TSVReaderTest {

    /*
     * Basic test of the TSV reader. We check that we can read a small SSSOM file
     * and get all the slots correctly.
     */
    @Test
    void testRead() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sample1.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertNotNull(ms);

        Assertions.assertNotNull(ms.getCreatorId());
        Assertions.assertEquals(1, ms.getCreatorId().size());
        Assertions.assertEquals("https://orcid.org/0000-0002-6095-8718", ms.getCreatorId().get(0));
        Assertions.assertEquals("Sample mapping set 1", ms.getMappingSetTitle());
        Assertions.assertEquals(LocalDate.of(2023, 9, 13), ms.getPublicationDate());

        List<Mapping> mappings = ms.getMappings();
        Assertions.assertNotNull(mappings);
        Assertions.assertEquals(8, mappings.size());

        Mapping mapping = mappings.get(0);
        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_00000001", mapping.getSubjectId());
        Assertions.assertEquals("organism", mapping.getSubjectLabel());
        Assertions.assertEquals("https://w3id.org/semapv/vocab/crossSpeciesExactMatch", mapping.getPredicateId());
        Assertions.assertEquals("http://purl.obolibrary.org/obo/UBERON_0000468", mapping.getObjectId());
        Assertions.assertEquals("https://w3id.org/semapv/vocab/ManualMappingCuration",
                mapping.getMappingJustification());
    }

    /*
     * Check that we can read a file where not all mappings have values in all
     * columns. Missing values should result in the corresponding slots being set to
     * null.
     */
    @Test
    void testMissingValues() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/missing-values.sssom.tsv");
        List<Mapping> mappings = reader.read().getMappings();

        Assertions.assertNull(mappings.get(0).getSubjectLabel());
        Assertions.assertNull(mappings.get(0).getConfidence());
        Assertions.assertNull(mappings.get(0).getPredicateModifier());

        Assertions.assertNull(mappings.get(1).getSubjectLabel());
        Assertions.assertNotNull(mappings.get(1).getConfidence());
        Assertions.assertNull(mappings.get(1).getPredicateModifier());

        Assertions.assertNotNull(mappings.get(2).getSubjectLabel());
        Assertions.assertNull(mappings.get(2).getConfidence());
        Assertions.assertNotNull(mappings.get(2).getPredicateModifier());
    }

    /*
     * Check that we can find an external metadata file that has the same basename
     * as the TSV file.
     */
    @Test
    void testReadExternalMetadataAuto() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sample-external-metadata.sssom.tsv");
        MappingSet ms = reader.read();
        Assertions.assertEquals("Sample mapping set with external metadata", ms.getMappingSetTitle());
    }

    /*
     * Check that we can read external metadata from an explicitly specified
     * filename, ignoring if needed any file that has the same basename as the TSV
     * file.
     */
    @Test
    void testReadExternalMetadataExplicit() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sample-external-metadata.sssom.tsv",
                "src/test/resources/sample-external-metadata-2.sssom.yml");
        MappingSet ms = reader.read();
        Assertions.assertEquals("Sample mapping set with external metadata 2", ms.getMappingSetTitle());
    }

    /*
     * Check that we fail if we can't find the metadata.
     */
    @Test
    void testFailIfNoMetadata() throws IOException {
        File tsvFile = new File("src/test/resources/sample-external-metadata.sssom.tsv");
        File renamedFile = new File("src/test/resources/sample-without-metadata.sssom.tsv");
        FileUtils.copyFile(tsvFile, renamedFile);

        TSVReader reader = new TSVReader(renamedFile);
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for missing metadata");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("External metadata file not found", sfe.getMessage());
        } finally {
            renamedFile.delete();
        }
    }

    /*
     * If an external metadata file is specified, the TSV file should not contain an
     * embedded metadata block.
     */
    @Test
    void testFailOnEmbeddedMetadataIfExternalFileSpecified() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/sample1.sssom.tsv",
                "src/test/resources/sample-external-metadata.sssom.yml");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for spurious embedded metadata block");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("Error when parsing TSV table", sfe.getMessage());
        }
    }

    /*
     * Check that we can read the metadata only.
     */
    @Test
    void testReadingMetadataOnly() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sample1.sssom.tsv");
        MappingSet ms = reader.read(true);
        Assertions.assertEquals("Sample mapping set 1", ms.getMappingSetTitle());
        Assertions.assertEquals(0, ms.getMappings().size());

        reader = new TSVReader(null, "src/test/resources/sample-external-metadata.sssom.yml");
        ms = reader.read(false);
        Assertions.assertEquals("Sample mapping set with external metadata", ms.getMappingSetTitle());
        Assertions.assertEquals(0, ms.getMappings().size());
    }

    /*
     * Any undeclared prefix should result in an exception.
     */
    @Test
    void testFailOnUndeclaredPrefixes() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/incomplete-curie-map.sssom.tsv");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for undeclared prefixes");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("Some prefixes are undeclared: ORCID", sfe.getMessage());
        }
    }

    /*
     * Any redefinition of a built-in prefix should result in an exception.
     */
    @Test
    void testFailOnRedefinedBuiltinPrefix() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/redefined-builtin-prefix.sssom.tsv");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for a redefined built-in prefix");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("Re-defined builtin prefix in the provided curie map", sfe.getMessage());
        }
    }
}
