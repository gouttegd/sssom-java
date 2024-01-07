/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TSVWriterTest {

    /*
     * Basic test of the TSV writer. We make up a minimal mapping set, write it out
     * to disk, and check it comes out as expected.
     */
    @Test
    void testTSVWriter() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setMappingSetTitle("Sample mapping set 2");
        ms.setMappingSetVersion("1.0");
        ms.setPublicationDate(LocalDate.of(2023, 9, 13));
        ms.setLicense("https://creativecommons.org/licenses/by/4.0/");
        ms.setMappingSetId("https://example.org/sssom/sample-mapping-set");

        TSVWriter writer = new TSVWriter("src/test/resources/sample2.sssom.tsv.out");
        writer.write(ms);

        Assertions.assertTrue(checkExpectedFile("sample2"));
    }

    /*
     * Basic round-trip test. We read a small SSSOM file (in "canonical" format,
     * with proper ordering), write it out, and check it comes out identical to the
     * original file.
     */
    @Test
    void testRoundtrip() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("sample1"));
    }

    /*
     * Same, but with a set where not all mappings have values in all columns. This
     * checks that the writer correctly determines the columns to write based on
     * which slots are used in the set.
     */
    @Test
    void testWriteMappingsWithMissingValues() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("missing-values"));
    }

    /*
     * Same, but with a set that contains (both at the set level and at the mapping
     * level) list-valued slots.
     */
    @Test
    void testWriteListValues() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("list-values"));
    }

    /*
     * Check that superfluous prefixes are not written to the Curie map.
     */
    @Test
    void testStripUnusedPrefix() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sample1.sssom.tsv");
        MappingSet ms = reader.read();

        // Add unused prefixes to the Curie map
        ms.getCurieMap().put("UNUSED1", "http://purl.obolibrary.org/obo/");
        ms.getCurieMap().put("UNUSED2", "https://example.org/");

        TSVWriter writer = new TSVWriter("src/test/resources/unused-prefixes.sssom.tsv.out");
        writer.write(ms);

        Assertions.assertTrue(checkExpectedFile("sample1", "unused-prefixes"));
    }

    @Test
    void testCustomCurieMap() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sample1.sssom.tsv");
        MappingSet ms = reader.read();

        // Build a custom map from the existing one...
        HashMap<String, String> customMap = new HashMap<String, String>();
        customMap.putAll(ms.getCurieMap());

        // then empty the existing map...
        ms.getCurieMap().clear();

        // and write the set with the custom map
        TSVWriter writer = new TSVWriter("src/test/resources/custom-map.sssom.tsv.out");
        writer.setCurieMap(customMap);
        writer.write(ms);

        Assertions.assertTrue(checkExpectedFile("sample1", "custom-map"));
    }

    /*
     * Check that "propagatable" slots are condensed back when writing.
     */
    @Test
    void testSlotCondensation() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("propagated-slots"));
    }

    /*
     * Check that extra slots are either not written, written in "declared" form, or
     * written as if they were standard slots, depending on the ExtraMetadataPolicy.
     */
    @Test
    void testWritingExtraSlots() throws IOException, SSSOMFormatException {
        File source = new File("src/test/resources/extra-slots.sssom.tsv");
        TSVReader reader = new TSVReader(source);
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        TSVWriter writer = new TSVWriter("src/test/resources/extra-slots-none.sssom.tsv.out");
        writer.setExtraMetadataPolicy(ExtraMetadataPolicy.NONE);
        writer.write(ms.toBuilder().build());
        Assertions.assertTrue(checkExpectedFile("extra-slots-none"));

        writer = new TSVWriter("src/test/resources/extra-slots-declared.sssom.tsv.out");
        writer.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        writer.write(ms.toBuilder().build());
        Assertions.assertTrue(checkExpectedFile("extra-slots-declared"));

        writer = new TSVWriter("src/test/resources/extra-slots-all.sssom.tsv.out");
        writer.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        writer.write(ms);
        Assertions.assertTrue(checkExpectedFile("extra-slots-all"));
    }

    /*
     * Check that the writer rejects extra slot names containing invalid characters.
     */
    @Test
    void testInvalidExtraSlotNames() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        ms.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms.getExtensionDefinitions().add(new ExtensionDefinition("foo", "https://example.org/fooProperty"));
        ms.getExtensionDefinitions().add(new ExtensionDefinition("bar", "https://example.org/barProperty"));
        ms.getExtensionDefinitions().add(new ExtensionDefinition("/invalid", "https://example.org/invalidSlotName1"));
        ms.getExtensionDefinitions().add(new ExtensionDefinition("invalid?", "https://example.org/invalidSlotName2"));

        ms.setExtensions(new HashMap<String, ExtensionValue>());
        ms.getExtensions().put("https://example.org/fooProperty", new ExtensionValue("ABC"));
        ms.getExtensions().put("https://example.org/invalidSlotName1", new ExtensionValue("DEF"));

        ms.getMappings().get(0).setExtensions(new HashMap<String, ExtensionValue>());
        ms.getMappings().get(0).getExtensions().put("https://example.org/barProperty", new ExtensionValue("BarA"));
        ms.getMappings().get(0).getExtensions().put("https://example.org/invalidSlotName2",
                new ExtensionValue("InvalidA"));

        TSVWriter writer = new TSVWriter("src/test/resources/invalid-extra-slot-names.sssom.tsv.out");
        writer.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        writer.write(ms);

        Assertions.assertTrue(checkExpectedFile("invalid-extra-slot-names"));
    }

    /*
     * Test license and mapping set ID slots are forcefully generated if absent.
     */
    @Test
    void testWritingDefaultSlots() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setMappingSetId(null);

        File written = new File("src/test/resources/default-slots.sssom.tsv.out");
        TSVWriter writer = new TSVWriter(written);
        writer.write(ms);

        TSVReader reader = new TSVReader(written);
        ms = reader.read();

        Assertions.assertNotNull(ms.getLicense());
        Assertions.assertEquals("https://w3id.org/sssom/license/all-rights-reserved", ms.getLicense());

        Assertions.assertNotNull(ms.getMappingSetId());
        Assertions.assertTrue(ms.getMappingSetId().startsWith("http://sssom.invalid/"));

        written.delete();
    }

    /*
     * Test that strings in the YAML metadata block are escaped.
     */
    @Test
    void testEscapingYAML() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setMappingSetTitle("Title\twith\u00A0non-printable\u0080characters");

        TSVWriter writer = new TSVWriter("src/test/resources/escaping-yaml.sssom.tsv.out");
        writer.write(ms);

        Assertions.assertTrue(checkExpectedFile("escaping-yaml"));
    }

    @Test
    void testEscapingTSV() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.getMappings().get(0).setComment("Value\twith\ttab\tcharacters");
        ms.getMappings().get(0).setObjectLabel("Value with \"quote\" characters");
        ms.getMappings().get(0).setIssueTrackerItem("Value with\nnew line character");

        TSVWriter writer = new TSVWriter("src/test/resources/escaping-tsv.sssom.tsv.out");
        writer.write(ms);

        Assertions.assertTrue(checkExpectedFile("escaping-tsv"));
    }

    /*
     * Compare a written out set with a file containing the expected output.
     */
    private boolean checkExpectedFile(String expected, String actual) throws IOException, SSSOMFormatException {
        String basedir = "src/test/resources/";
        File expectedFile = new File(basedir + expected);
        File actualFile = new File(basedir + actual);
        boolean same = FileUtils.contentEquals(expectedFile, actualFile);
        if ( same ) {
            actualFile.delete();
        }
        return same;
    }

    /*
     * Compare a written out set with a file containing the expected output, where
     * the name of the actual file is derived from the name of the theoretical file.
     */
    private boolean checkExpectedFile(String basename) throws IOException, SSSOMFormatException {
        return checkExpectedFile(basename + ".sssom.tsv", basename + ".sssom.tsv.out");
    }

    /*
     * Read a mapping set, write it out, and compare.
     */
    private boolean roundtrip(String name) throws IOException, SSSOMFormatException {
        File source = new File(String.format("src/test/resources/%s.sssom.tsv", name));
        TSVReader reader = new TSVReader(source);
        MappingSet ms = reader.read();

        File target = new File(String.format("src/test/resources/%s.sssom.tsv.out", name));
        TSVWriter writer = new TSVWriter(target);
        writer.write(ms);

        boolean same = FileUtils.contentEquals(source, target);
        if ( same ) {
            target.delete();
        }

        return same;
    }

    /*
     * Get a common set to be used in the tests above.
     */
    private MappingSet getTestSet() {
        // @formatter:off
        MappingSet ms = MappingSet.builder()
                .mappings(new ArrayList<Mapping>())
                .curieMap(new HashMap<String,String>())
                .mappingSetId("https://example.org/sssom/sample-mapping-set")
                .build();
        ms.getMappings().add(Mapping.builder()
                .subjectId("http://purl.obolibrary.org/obo/FBbt_00000001")
                .predicateId("https://w3id.org/semapv/vocab/crossSpeciesExactMatch")
                .objectId("http://purl.obolibrary.org/obo/UBERON_0000468")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        // @formatter:off

        ms.getCurieMap().put("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        ms.getCurieMap().put("UBERON", "http://purl.obolibrary.org/obo/UBERON_");

        return ms;
    }
}
