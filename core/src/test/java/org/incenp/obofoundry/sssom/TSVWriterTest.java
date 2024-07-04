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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        assertWrittenAsExpected(ms, "exo2c-minimal", null, null, null);
    }

    /*
     * Basic round-trip test. We read a small SSSOM file (in "canonical" format,
     * with proper ordering), write it out, and check it comes out identical to the
     * original file.
     */
    @Test
    void testRoundtrip() throws IOException, SSSOMFormatException {
        assertRoundtrip("exo2c");
    }

    /*
     * Same, but with a set where not all mappings have values in all columns. This
     * checks that the writer correctly determines the columns to write based on
     * which slots are used in the set.
     */
    @Test
    void testWriteMappingsWithMissingValues() throws IOException, SSSOMFormatException {
        assertRoundtrip("test-missing-values");
    }

    /*
     * Same, but with a set that contains list-valued slots.
     */
    @Test
    void testWriteListValues() throws IOException, SSSOMFormatException {
        assertRoundtrip("test-mapping-list-values");
    }

    /*
     * Test that we can write an empty file.
     */
    @Test
    void testEmptySet() throws IOException, SSSOMFormatException {
        assertRoundtrip("exo2c-empty");
    }

    /*
     * Check that superfluous prefixes are not written to the Curie map.
     */
    @Test
    void testStripUnusedPrefix() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        // Add unused prefix to the Curie map
        ms.getCurieMap().put("NETENT", "https://example.net/entities/");

        assertWrittenAsExpected(ms, "exo2c-minimal", "test-stripping-unused-prefix", null, null);
    }

    @Test
    void testCustomCurieMap() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        // Build a custom map from the existing one...
        HashMap<String, String> customMap = new HashMap<String, String>();
        customMap.putAll(ms.getCurieMap());

        // then empty the existing map...
        ms.getCurieMap().clear();

        // and write the set with the custom map
        assertWrittenAsExpected(ms, "exo2c-minimal", "test-custom-map", customMap, null);
    }

    /*
     * Check that "propagatable" slots are condensed back when writing.
     */
    @Test
    void testSlotCondensation() throws IOException, SSSOMFormatException {
        assertRoundtrip("exo2c-with-propagatable-slots");
    }

    /*
     * Check that extra slots are either not written, written in "declared" form, or
     * written as if they were standard slots, depending on the ExtraMetadataPolicy.
     */
    @Test
    void testWritingExtraSlots() throws IOException, SSSOMFormatException {
        File source = new File("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        TSVReader reader = new TSVReader(source);
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-extensions-none", null, null, ExtraMetadataPolicy.NONE);

        assertWrittenAsExpected(ms, "test-extensions-defined", null, null, ExtraMetadataPolicy.DEFINED);

        assertWrittenAsExpected(ms, "test-extensions-undefined", null, null, ExtraMetadataPolicy.UNDEFINED);
    }

    /*
     * Check that the writer rejects extra slot names containing invalid characters.
     */
    @Test
    void testInvalidExtraSlotNames() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        ms.setExtensionDefinitions(new ArrayList<ExtensionDefinition>());
        ms.getExtensionDefinitions()
                .add(new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"));
        ms.getExtensionDefinitions().add(new ExtensionDefinition("ext_bar",
                "https://example.org/properties/barProperty", "http://www.w3.org/2001/XMLSchema#integer"));
        ms.getExtensionDefinitions()
                .add(new ExtensionDefinition("/ext_invalid", "https://example.org/properties/invalidSlotName1"));
        ms.getExtensionDefinitions()
                .add(new ExtensionDefinition("ext_invalid?", "https://example.org/properties/invalidSlotName2"));

        ms.setExtensions(new HashMap<String, ExtensionValue>());
        ms.getExtensions().put("https://example.org/properties/fooProperty", new ExtensionValue("Foo A"));
        ms.getExtensions().put("https://example.org/properties/invalidSlotName1", new ExtensionValue("Invalid A"));

        ms.getMappings().get(0).setExtensions(new HashMap<String, ExtensionValue>());
        ms.getMappings().get(0).getExtensions().put("https://example.org/properties/barProperty",
                new ExtensionValue("111"));
        ms.getMappings().get(0).getExtensions().put("https://example.org/properties/invalidSlotName2",
                new ExtensionValue("Invalid B"));

        assertWrittenAsExpected(ms, "test-invalid-extension-slot-names", null, null, ExtraMetadataPolicy.DEFINED);
    }

    /*
     * Test license and mapping set ID slots are forcefully generated if absent.
     */
    @Test
    void testWritingDefaultSlots() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setMappingSetId(null);
        ms.setLicense(null);

        File written = new File("src/test/resources/output/test-default-slots.sssom.tsv.out");
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
     * Test that strings in the YAML metadata block are escaped when needed.
     */
    @Test
    void testEscapingYAML() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setMappingSetId("https://example.org/sets/test-escaping-yaml");
        ms.setMappingSetTitle("O2C set\twith\u00A0non-printable\u0080characters");
        ArrayList<String> other = new ArrayList<String>();
        other.add("> A value starting with an indicator");
        other.add(": Initial colon followed by space");
        other.add(":\tInitial colon followed by tab");
        other.add(":Initial colon not followed by space");
        other.add("? Initial question mark followed by space");
        other.add("?Initial question mark not followed by space");
        other.add("- Initial dash followed by space");
        other.add("-Initial dash not followed by space");
        other.add("Final colon:");
        other.add("Final question mark?");
        other.add("Final dash-");
        other.add(" Initial space");
        other.add("Trailing space ");
        other.add("Internal : sequence");
        other.add("Internal #sequence");
        other.add("\"Initial\" quotes");
        other.add("Internal \"quotes\"");
        other.add("> Internal \"quotes\"");
        ms.setSeeAlso(other);

        assertWrittenAsExpected(ms, "test-escaping-yaml", null, null, null);
    }

    @Test
    void testEscapingTSV() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setMappingSetId("https://example.org/sets/test-escaping-tsv");
        ms.getMappings().get(0).setComment("Value\twith\ttab\tcharacters");
        ms.getMappings().get(0).setObjectLabel("Value with \"quote\" characters");
        ms.getMappings().get(0).setIssueTrackerItem("Value with\nnew line character");

        assertWrittenAsExpected(ms, "test-escaping-tsv", null, null, null);
    }

    /*
     * Checks that a mapping set is written exactly as we expect. This method will
     * write the provided set to a temporary file and compares the written file with
     * a theoretical file, and raises an assertion failure if the two files do not
     * have exactly the same contents.
     * 
     * The theoretical file should be either in src/test/resources/output/ or in
     * src/test/resources/sets/, with a .sssom.tsv extension.
     * 
     * The temporary file will be written in src/test/resources/output/, with a
     * .sssom.tsv.out extension. If actualBasename is null, the basename of the
     * theoretical file will be used. The temporary file will be automatically
     * deleted if it is found to be identical to the theoretical file.
     */
    private void assertWrittenAsExpected(MappingSet ms, String expectedBasename, String actualBasename,
            Map<String, String> curieMap, ExtraMetadataPolicy extraPolicy) throws IOException {
        if ( actualBasename == null ) {
            actualBasename = expectedBasename;
        }

        File written = new File("src/test/resources/output/" + actualBasename + ".sssom.tsv.out");
        TSVWriter writer = new TSVWriter(written);
        if ( curieMap != null ) {
            writer.setCurieMap(curieMap);
        }
        if ( extraPolicy != null ) {
            writer.setExtraMetadataPolicy(extraPolicy);
        }
        writer.write(ms.toBuilder().build());

        File expected = new File("src/test/resources/output/" + expectedBasename + ".sssom.tsv");
        if ( !expected.exists() ) {
            expected = new File("src/test/resources/sets/" + expectedBasename + ".sssom.tsv");
        }

        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }

    /*
     * Checks that a set can be written back to yield exactly the same file as the
     * file from which it was read.
     * 
     * The input set should be in the src/test/resources/sets/ directory with a
     * .sssom.tsv extension. The set will be written back to a file with the same
     * basename and a .sssom.tsv.out extension in the src/test/resources/output/
     * directory. It will be automatically deleted if it is identical to the
     * original file.
     */
    private void assertRoundtrip(String basename) throws IOException, SSSOMFormatException {
        File source = new File("src/test/resources/sets/" + basename + ".sssom.tsv");
        TSVReader reader = new TSVReader(source);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, basename, null, null, null);
    }

    /*
     * Get a common set to be used in the tests above.
     */
    private MappingSet getTestSet() {
        // @formatter:off
        MappingSet ms = MappingSet.builder()
                .mappings(new ArrayList<Mapping>())
                .curieMap(new HashMap<String,String>())
                .mappingSetId("https://example.org/sets/exo2c")
                .license("https://creativecommons.org/licenses/by/4.0/")
                .build();
        ms.getMappings().add(Mapping.builder()
                .subjectId("https://example.org/entities/0001")
                .subjectLabel("alice")
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("https://example.com/entities/0011")
                .objectLabel("alpha")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        // @formatter:off

        ms.getCurieMap().put("ORGENT", "https://example.org/entities/");
        ms.getCurieMap().put("COMENT", "https://example.com/entities/");

        return ms;
    }
}
