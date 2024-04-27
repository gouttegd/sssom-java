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

package org.incenp.obofoundry.sssom.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the main CLI module.
 */
public class SSSOMCLITest {

    /*
     * Input set tests
     */

    @Test
    void testReadingOneSetFromArg() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2n.sssom.tsv" }, "exo2n.sssom.tsv", null);
    }

    @Test
    void testReadingSeveralSetsFromArg() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2n.sssom.tsv", "exo2c.sssom.tsv" }, "exo2cn.sssom.tsv", null);

        // Same, but without merging the metadata from the second set
        TestUtils.runCommand(0, new String[] { "exo2n.sssom.tsv", "exo2c.sssom.tsv" },
                "exo2cn-no-metadata-merge.sssom.tsv", new String[] { "--no-metadata-merge" });
    }

    @Test
    void testReadingOneSetFromStdin() throws IOException {
        // No --input option, should read from stdin by default
        FileInputStream input = new FileInputStream("src/test/resources/sets/exo2n.sssom.tsv");
        System.setIn(input);
        TestUtils.runCommand(0, null, "exo2n.sssom.tsv", null);
        input.close();

        // "-" argument on the command line
        input = new FileInputStream("src/test/resources/sets/exo2n.sssom.tsv");
        System.setIn(input);
        TestUtils.runCommand(0, new String[] { "-" }, "exo2n.sssom.tsv", null);
        input.close();
    }

    @Test
    void testReadingExternalMetadata() throws IOException {
        // The external metadata file does not have a ".sssom.yml" extension to make
        // sure it is not automatically be found by the TSV reader
        TestUtils.runCommand(0, new String[] {
                "src/test/resources/sets/exo2n-no-metadata.sssom.tsv:src/test/resources/sets/exo2n-metadata.yml" },
                "exo2n.sssom.tsv", null);
    }

    @Test
    void testReadingWithIOError() throws IOException {
        TestUtils.runCommand(1, new String[] { "inexisting-file.sssom.tsv" }, null, null);
    }

    @Test
    void testReadingWithSSSOMError() throws IOException {
        TestUtils.runCommand(1, new String[] { "pom.xml" }, null, null);
    }

    /*
     * Tests for mangling IRIs with an extended prefix map
     */

    @Test
    void testManglingWithEPM() throws IOException {
        TestUtils.runCommand(0, new String[] { "fbbt-uncanonical-urls.sssom.tsv" }, "fbbt-canonicalised-urls.sssom.tsv",
                new String[] { "--mangle-iris", "../core/src/main/resources/obo.epm.json" });
    }

    @Test
    void testManglingWithInternalEPM() throws IOException {
        TestUtils.runCommand(0, new String[] { "fbbt-uncanonical-urls.sssom.tsv" }, "fbbt-canonicalised-urls.sssom.tsv",
                new String[] { "--mangle-iris", "obo" });
    }

    @Test
    void testReadingEPMError() throws IOException {
        // I/O error, the EPM file does not exist
        TestUtils.runCommand(1, new String[] { "fbbt-uncanonical-urls.sssom.tsv" }, "fbbt-canonicalised-urls.sssom.tsv",
                new String[] { "--mangle-iris", "inexisting-epm.json" });

        // Parsing error, the file is not an EPM
        TestUtils.runCommand(1, new String[] { "fbbt-uncanonical-urls.sssom.tsv" }, "fbbt-canonicalised-urls.sssom.tsv",
                new String[] { "--mangle-iris", "pom.xml" });
    }

    /*
     * Output tests
     */

    @Test
    void testWritingError() throws IOException {
        // Try writing to a directory that does not exist
        TestUtils.runCommand(1, new String[] { "exo2n.sssom.tsv" }, "dir/exo2n.sssom.tsv", null);
    }

    @Test
    void testWritingOutCardinality() throws IOException {
        // We merge the three O2C, O2N, and C2N sets to have a range of cardinalities
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv", "exo2n.sssom.tsv", "exc2n.sssom.tsv" },
                "exall-cardinality.sssom.tsv", new String[] { "--force-cardinality" });
    }

    @Test
    void testWriteOutputMetadata() throws IOException {
        // Use metadata from a different file and merge the original metadata back in
        TestUtils.runCommand(0, new String[] { "exc2n.sssom.tsv" }, "exc2n-with-o2n-metadata.sssom.tsv",
                new String[] { "--output-metadata", "src/test/resources/sets/exo2n-metadata.yml" });

        // Use metadata from a different file, without merging the original metadata
        TestUtils.runCommand(0, new String[] { "exc2n.sssom.tsv" }, "exc2n-with-o2n-metadata-only.sssom.tsv",
                new String[] {
                "--output-metadata", "src/test/resources/sets/exo2n-metadata.yml", "--no-metadata-merge" });
    }

    @Test
    void testWriteOutputMetadataError() throws IOException {
        TestUtils.runCommand(1, new String[] { "exc2n.sssom.tsv" }, null,
                new String[] { "--output-metadata", "inexisting-file.sssom.yml" });

        TestUtils.runCommand(1, new String[] { "exc2n.sssom.tsv" }, null,
                new String[] { "--output-metadata", "pom.xml" });
    }

    @Test
    void testChoosingOutputPrefixMap() throws IOException {
        // In all three tests, we change the original prefixes in SSSOM/T prefix
        // declarations
        String[] args = {
                null,
                "--prefix=ORGENT=none",
                "--prefix=ORG=https://example.org/entities/",
                "--prefix=NETENT=none",
                "--prefix=NET=https://example.net/entities/"
        };

        // Here, only the original map should be used, regardless of the SSSOM/T
        // declarations
        args[0] = "--output-prefix-map=INPUT";
        TestUtils.runCommand(0, new String[] { "exo2n.sssom.tsv" }, "exo2n.sssom.tsv", args);

        // Opposite: only the SSSOM/T-declared prefixes should be used
        args[0] = "--output-prefix-map=SSSOMT";
        TestUtils.runCommand(0, new String[] { "exo2n.sssom.tsv" }, "exo2n-with-ssomt-prefix-map.sssom.tsv", args);

        // Use the SSSOM/T map first, then the original one
        args[0] = "--output-prefix-map=BOTH";
        TestUtils.runCommand(0, new String[] { "exo2n.sssom.tsv" }, "exo2n-with-combined-prefix-map.sssom.tsv", args);
    }

    @Test
    void testSplitOutput() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv", "exo2n.sssom.tsv" }, null,
                new String[] { "--split=src/test/resources/output/split" });

        File dir = new File("src/test/resources/output/split");
        Assertions.assertTrue(dir.isDirectory());

        File o2cSet = new File("src/test/resources/output/split/ORGENT-to-COMENT.sssom.tsv");
        File o2cSetExpected = new File("src/test/resources/output/ORGENT-to-COMENT.sssom.tsv");
        Assertions.assertTrue(FileUtils.contentEquals(o2cSetExpected, o2cSet));

        File o2nSet = new File("src/test/resources/output/split/ORGENT-to-NETENT.sssom.tsv");
        File o2nSetExpected = new File("src/test/resources/output/ORGENT-to-NETENT.sssom.tsv");
        Assertions.assertTrue(FileUtils.contentEquals(o2nSetExpected, o2nSet));

        o2cSet.delete();
        o2nSet.delete();
        dir.delete();
    }

    @Test
    void testSplitOutputDirectoryError() throws IOException {
        // Split directory path exists but is not a directory
        TestUtils.runCommand(1, new String[] { "exo2c.sssom.tsv", "exo2n.sssom.tsv" }, null,
                new String[] { "--split=pom.xml" });
    }

    /*
     * SSSOM/Transform tests
     */

    @Test
    void testApplyASingleRuleset() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-exact-match.sssom.tsv",
                new String[] { "--ruleset=src/test/resources/rules/org-exact-matches.rules" });
    }

    @Test
    void testApplyASingleRule() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-exact-match.sssom.tsv", new String[] {
                "--prefix=ORG=https://example.org/entities/", "--rule=predicate==skos:exactMatch -> include()" });
    }

    @Test
    void testApplySeveralRules() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-exact-match.sssom.tsv",
                new String[] { "--prefix=ORG=https://example.org/entities/",
                        "--rule=!predicate==skos:exactMatch -> stop()", "--rule=subject==ORG:* -> include()" });
    }

    @Test
    void testApplyIncludeAndExcludeRules() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-exact-match.sssom.tsv",
                new String[] { "--prefix=ORG=https://example.org/entities/", "--exclude=!predicate==skos:exactMatch",
                        "--include=subject==ORG:*" });
    }

    @Test
    void testApplyRuleAndRuleset() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-exact-match-plus-org1.sssom.tsv",
                new String[] { "--ruleset=src/test/resources/rules/org-exact-matches.rules",
                        "--rule=subject==ORG:0001 -> include()" });
    }

    @Test
    void testIncludeAll() throws IOException {
        // No --include-all and only a stop rule -> the set should be empty
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-empty.sssom.tsv", new String[] {
                "--prefix=ORG=https://example.org/entities/", "--rule=predicate==skos:closeMatch -> stop()" });

        // With --include-all, mappings that are not explicitly excluded should remain
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-exact-match.sssom.tsv",
                new String[] { "--prefix=ORG=https://example.org/entities/",
                        "--rule=predicate==skos:closeMatch -> stop()", "--include-all" });
    }

    @Test
    void testSSSOMTReadError() throws IOException {
        TestUtils.runCommand(1, new String[] { "exo2c.sssom.tsv" }, null,
                new String[] { "--ruleset=inexisting.rules" });
    }

    @Test
    void testSSSOMTParsingError() throws IOException {
        TestUtils.runCommand(1, new String[] { "exo2c.sssom.tsv" }, null, new String[] { "--ruleset=pom.xml" });
    }

    @Test
    void testSSSOMTPrefixMap() throws IOException {
        String[] input = { "test-sssomt-prefix-map.sssom.tsv" };
        String rule = "--rule=subject==ORGA:* -> include()";

        // No prefix map at all, error
        TestUtils.runCommand(1, input, null, new String[] { rule });

        // Use the prefix map from the input set
        TestUtils.runCommand(0, input, "test-sssomt-prefix-map-orga.sssom.tsv",
                new String[] { "--prefix-map-from-input", rule });

        // Use a prefix from the command-line
        TestUtils.runCommand(0, input, "test-sssomt-prefix-map-orgb.sssom.tsv",
                new String[] { "--prefix=ORGA=https://example.org/entities/B/", rule });

        // Command-line prefix + input prefix map (CLI takes precedence)
        TestUtils.runCommand(0, input, "test-sssomt-prefix-map-orgb.sssom.tsv",
                new String[] { "--prefix-map-from-input", "--prefix=ORGA=https://example.org/entities/B/", rule });

        // External prefix map
        TestUtils.runCommand(0, input, "test-sssomt-prefix-map-orgb.sssom.tsv",
                new String[] { "--prefix-map=src/test/resources/rules/test-sssomt-prefix-map.yml", rule });

        // External prefix map + ruleset prefix map (ruleset takes precedence)
        TestUtils.runCommand(0, input, "test-sssomt-prefix-map-orgc.sssom.tsv",
                new String[] { "--prefix-map=src/test/resources/rules/test-sssomt-prefix-map.yml",
                        "--ruleset=src/test/resources/rules/test-sssomt-prefix-map.rules" });

        // Ruleset prefix map + CLI prefix (CLI takes precedence)
        TestUtils.runCommand(0, input, "test-sssomt-prefix-map-orgb.sssom.tsv",
                new String[] { "--prefix=ORGA=https://example.org/entities/B/", rule });
    }

    @Test
    void testExternalPrefixMapError() throws IOException {
        TestUtils.runCommand(1, new String[] { "test-sssomt-prefix-map.sssom.tsv" }, null,
                new String[] { "--prefix-map=inexisting.yml" });

        TestUtils.runCommand(1, new String[] { "test-sssomt-prefix-map.sssom.tsv" }, null,
                new String[] { "--prefix-map=pom.xml" });
    }

    @Test
    void testUpdateFromOntology() throws IOException {
        // Simply updating the labels
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-updated-from-ont1.sssom.tsv",
                new String[] { "--update-from-ontology", "../core/src/test/resources/ont1.ofn" });

        // Deleting missing subjects
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-subject-checked-against-ont1.sssom.tsv",
                new String[] { "--update-from-ontology", "../core/src/test/resources/ont1.ofn:subject" });

        // Deleting missing objects
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-object-checked-against-ont1.sssom.tsv",
                new String[] { "--update-from-ontology", "../core/src/test/resources/ont1.ofn:object" });
    }
}
