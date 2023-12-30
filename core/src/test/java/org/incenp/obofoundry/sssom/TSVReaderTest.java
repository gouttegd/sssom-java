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
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.model.EntityType;
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
     * Check that we can read a file that uses multi-valued slots.
     */
    @Test
    void testListValues() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/list-values.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertEquals(2, ms.getCreatorId().size());
        Assertions.assertEquals("https://orcid.org/AAAA-BBBB-CCCC-0001", ms.getCreatorId().get(0));
        Assertions.assertEquals("https://orcid.org/AAAA-BBBB-CCCC-0002", ms.getCreatorId().get(1));

        Assertions.assertEquals(1, ms.getSeeAlso().size());
        Assertions.assertEquals("https://example.org/seealso1", ms.getSeeAlso().get(0));

        Mapping m = ms.getMappings().get(0);

        Assertions.assertEquals(2, m.getAuthorId().size());
        Assertions.assertEquals("https://orcid.org/AAAA-BBBB-CCCC-0003", m.getAuthorId().get(0));
        Assertions.assertEquals("https://orcid.org/AAAA-BBBB-CCCC-0004", m.getAuthorId().get(1));

        Assertions.assertEquals(1, m.getSeeAlso().size());
        Assertions.assertEquals("https://example.org/seealso2", m.getSeeAlso().get(0));
    }

    /*
     * Check that we can read a file that misuses multi-valued slots for
     * single-valued slots.
     * 
     */
    @Test
    void testPseudoListValues() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/pseudo-list-values.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertEquals(1, ms.getCreatorId().size());
        Assertions.assertEquals("https://orcid.org/AAAA-BBBB-CCCC-0001", ms.getCreatorId().get(0));

        Assertions.assertEquals(1, ms.getSeeAlso().size());
        Assertions.assertEquals("https://example.org/seealso1", ms.getSeeAlso().get(0));
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
     * Check that we can read from a stream rather than from a file.
     */
    @Test
    void testReadingFromStream() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader(new FileInputStream("src/test/resources/sample1.sssom.tsv"));
        MappingSet ms = reader.read();
        Assertions.assertEquals("Sample mapping set 1", ms.getMappingSetTitle());
        Assertions.assertEquals(8, ms.getMappings().size());

        reader = new TSVReader(new FileInputStream("src/test/resources/sample-external-metadata.sssom.tsv"));
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for missing metadata when reading from stream");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("No embedded metadata and external metadata not specified", sfe.getMessage());
        }
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

    /*
     * "Propagatable" slots in the set metadata should be propagated to the
     * individual mappings.
     */
    @Test
    void testSlotPropagation() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/propagated-slots.sssom.tsv");
        MappingSet ms = reader.read();

        for ( Mapping m : ms.getMappings() ) {
            Assertions.assertEquals("http://example.org/provider", m.getMappingProvider());
            Assertions.assertNotEquals("sample mapping tool", m.getMappingTool());
        }
        Assertions.assertEquals("another mapping tool", ms.getMappings().get(2).getMappingTool());
    }

    /*
     * Obsolete fields should be translated to their standard equivalents.
     */
    @Test
    void testObsoleteFields() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/obsolete-fields.sssom.tsv");
        MappingSet ms = reader.read();
        Mapping m1 = ms.getMappings().get(0);
        Mapping m2 = ms.getMappings().get(1);

        Assertions.assertEquals("https://w3id.org/semapv/vocab/LexicalMatching", m1.getMappingJustification());
        Assertions.assertNull(m2.getMappingJustification());
        
        Assertions.assertNull(m1.getSubjectType());
        Assertions.assertNull(m1.getObjectType());
        Assertions.assertEquals(EntityType.OWL_CLASS, m2.getSubjectType());
        Assertions.assertEquals(EntityType.OWL_CLASS, m2.getObjectType());
    }

    /*
     * Extra slots should be completely ignored when ExtraMetadataPolicy is set to
     * NONE.
     */
    @Test
    void testIgnoreExtraMetadata() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/extra-slots.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.NONE);
        MappingSet ms = reader.read();

        Assertions.assertNull(ms.getExtraMetadata());

        Assertions.assertNull(ms.getExtraColumns());

        Assertions.assertNull(ms.getMappings().get(0).getExtraMetadata());
    }

    /*
     * Only the extra slots that have been properly "declared" (by being located
     * under the "extra_metadata" slot for set-level metadata, or by being declared
     * in the "extra_columns" slot for mapping-level metadata) should be accepted
     * when ExtraMetadataPolicy is set to DECLARATION_REQUIRED.
     */
    @Test
    void testAcceptDeclaredExtraMetadata() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/extra-slots.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DECLARATION_REQUIRED);
        MappingSet ms = reader.read();

        Assertions.assertNotNull(ms.getExtraMetadata());
        Assertions.assertTrue(ms.getExtraMetadata().containsKey("foo"));
        Assertions.assertEquals("ABC", ms.getExtraMetadata().get("foo"));
        Assertions.assertFalse(ms.getExtraMetadata().containsKey("notfoo"));

        Assertions.assertNotNull(ms.getExtraColumns());
        Assertions.assertTrue(ms.getExtraColumns().contains("bar"));
        Assertions.assertFalse(ms.getExtraColumns().contains("bat"));
        Assertions.assertFalse(ms.getExtraColumns().contains("bax"));
        Assertions.assertTrue(ms.getExtraColumns().contains("baz"));

        Mapping m1 = ms.getMappings().get(0);
        Assertions.assertNotNull(m1.getExtraMetadata());
        Assertions.assertTrue(m1.getExtraMetadata().containsKey("bar"));
        Assertions.assertEquals("Bar1", m1.getExtraMetadata().get("bar"));
        Assertions.assertFalse(m1.getExtraMetadata().containsKey("bat"));
        Assertions.assertTrue(m1.getExtraMetadata().containsKey("baz"));
        Assertions.assertEquals("Baz1", m1.getExtraMetadata().get("baz"));
    }

    /*
     * All non-standard metadata should end up in the "extra_metadata" slots,
     * regardless of whether they have been declared, when ExtraMetadataPolicy is
     * set to ALL.
     */
    @Test
    void testAcceptAllExtraMetadata() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/extra-slots.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.ALL);
        MappingSet ms = reader.read();

        Assertions.assertNotNull(ms.getExtraMetadata());
        Assertions.assertTrue(ms.getExtraMetadata().containsKey("foo"));
        Assertions.assertEquals("ABC", ms.getExtraMetadata().get("foo"));
        Assertions.assertTrue(ms.getExtraMetadata().containsKey("notfoo"));
        Assertions.assertEquals("DEF", ms.getExtraMetadata().get("notfoo"));

        Assertions.assertNotNull(ms.getExtraColumns());
        Assertions.assertTrue(ms.getExtraColumns().contains("bar"));
        Assertions.assertTrue(ms.getExtraColumns().contains("bat"));
        Assertions.assertFalse(ms.getExtraColumns().contains("bax"));
        Assertions.assertTrue(ms.getExtraColumns().contains("baz"));

        Mapping m1 = ms.getMappings().get(0);
        Assertions.assertNotNull(m1.getExtraMetadata());
        Assertions.assertTrue(m1.getExtraMetadata().containsKey("bar"));
        Assertions.assertEquals("Bar1", m1.getExtraMetadata().get("bar"));
        Assertions.assertTrue(m1.getExtraMetadata().containsKey("bat"));
        Assertions.assertEquals("Bat1", m1.getExtraMetadata().get("bat"));
        Assertions.assertTrue(m1.getExtraMetadata().containsKey("baz"));
        Assertions.assertEquals("Baz1", m1.getExtraMetadata().get("baz"));
    }
}
