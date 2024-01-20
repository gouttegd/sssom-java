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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SSSOMCLITest {

    @BeforeEach
    void setUp() {
        // Let the CLI know it is running as part of the test suite, so that it does not
        // call System.exit (which would terminate the testing framework).
        System.setProperty("org.incenp.obofoundry.sssom.cli#inTest", "yes");
    }

    @Test
    void testReadingExternalMetadata() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/sample-external-metadata.sssom.tsv:../core/src/test/resources/sample-external-metadata-2.sssom.yml",
                      "--output", "src/test/resources/read-from-external-metadata.sssom.tsv.out");
        // @formatter:on
        checkOutput("read-from-external-metadata");
    }

    @Test
    void testReadingOneSetFromStdin() throws IOException {
        FileInputStream input = new FileInputStream("../core/src/test/resources/sample1.sssom.tsv");
        System.setIn(input);
        runCommand(0, "--output", "src/test/resources/read-from-stdin.tsv.out");
        checkOutput("read-from-stdin.tsv");
        input.close();

        input = new FileInputStream("../core/src/test/resources/sample1.sssom.tsv");
        System.setIn(input);
        runCommand(0, "--input", "-", "--output", "src/test/resources/read-from-stdin.tsv.out");
        checkOutput("read-from-stdin.tsv");
        input.close();
    }

    /*
     * Check that we can apply a SSSOM/T ruleset.
     */
    @Test
    void testSimpleSSSOMTRuleset() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/sample1.sssom.tsv",
                      "--ruleset", "../core/src/test/resources/ruleset2.sssomt",
                      "--output", "src/test/resources/filtered1.sssom.tsv.out");
        // @formatter:on
        checkOutput("filtered1.sssom.tsv");
    }

    /*
     * Check that we can apply single SSSOM/T rules.
     */
    @Test
    void testSimpleSSSOMRules() throws IOException {
        // @formatter:off
        runCommand(0, "--input",  "../core/src/test/resources/sample1.sssom.tsv",
                      "--prefix", "UBERON=http://purl.obolibrary.org/obo/UBERON_",
                      "--prefix", "FBbt=http://purl.obolibrary.org/obo/FBbt_",
                      "--rule",   "subject==FBbt:* -> invert()",
                      "--rule",   "subject==UBERON:6* -> stop()",
                      "--rule",   "subject==* -> include()",
                      "--output", "src/test/resources/filtered1.sssom.tsv.out");
        // @formatter:on
        checkOutput("filtered1.sssom.tsv");
    }

    /*
     * Check that obsolete fields in input are translated to their standard
     * equivalents in output.
     */
    @Test
    void testUpdateOldFile() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/obsolete-fields.sssom.tsv",
                      "--output", "src/test/resources/updated-fields.sssom.tsv.out");
        // @formatter:on
        checkOutput("updated-fields.sssom.tsv");
    }

    /*
     * Check that multi-valued slots misused as single-value slots in input are
     * correctly written as true multi-valued slots in output.
     */
    @Test
    void testNormalisePseudoLists() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/pseudo-list-values.sssom.tsv",
                      "--output", "src/test/resources/normalised-lists.sssom.tsv.out");
        // @formatter:on
        checkOutput("normalised-lists.sssom.tsv");
    }

    /*
     * Check that we can apply changes to mappings as part of a SSSOM/T ruleset.
     */
    @Test
    void testMappingEdition() throws IOException {
        // @formatter:off
        runCommand(0, "--input",  "../core/src/test/resources/sample1.sssom.tsv",
                "--rule",   "subject==* -> edit(\"predicate_id=skos:exactMatch\")",
                "--rule",   "subject==* -> include()",
                "--output", "src/test/resources/edited1.sssom.tsv.out");
        // @formatter:on
        checkOutput("edited1.sssom.tsv");
    }

    @Test
    void testEditionWithAssign() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/sample1.sssom.tsv",
                "--rule",   "subject==* -> assign(\"predicate_id\", \"skos:exactMatch\")",
                "--rule",   "subject==* -> include()",
                "--output", "src/test/resources/edited2.sssom.tsv.out");
        // @formatter:on
        checkOutput("edited2.sssom.tsv");
    }

    @Test
    void testEditionWithReplace() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/sample1.sssom.tsv",
                "--rule",   "subject==* -> replace(\"predicate_id\", \"ExactMatch\", \"BroadMatch\")",
                "--rule",   "subject==* -> include()",
                "--output", "src/test/resources/edited3.sssom.tsv.out");
        // @formatter:on
        checkOutput("edited3.sssom.tsv");
    }

    @Test
    void testManglingWithEPM() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/uncanonical.sssom.tsv",
                "--mangle-iris",  "../core/src/main/resources/obo.epm.json",
                "--output",       "src/test/resources/canonicalised.sssom.tsv.out");
        // @formatter:on
        checkOutput("canonicalised.sssom.tsv");
    }
    
    @Test
    void testMergingMetadata() throws IOException {
    	// @formatter:off
    	runCommand(0, "--input", "../core/src/test/resources/sample1.sssom.tsv",
    	        "--input",       "../core/src/test/resources/sample2.sssom.tsv",
    	        "--output",      "src/test/resources/merged1+2.sssom.tsv.out");
    	// @formatter:on
    	checkOutput("merged1+2.sssom.tsv");
    	
    	// @formatter:off
    	runCommand(0, "--input", "../core/src/test/resources/sample2.sssom.tsv",
    	        "--input",       "../core/src/test/resources/sample1.sssom.tsv",
    	        "--output",      "src/test/resources/merged2+1.sssom.tsv.out");
    	// @formatter:on
    	checkOutput("merged2+1.sssom.tsv");
    	
    	// @formatter:off
    	runCommand(0, "--input", "../core/src/test/resources/sample2.sssom.tsv",
    	        "--input",       "../core/src/test/resources/sample1.sssom.tsv",
    	        "--no-metadata-merge",
    	        "--output",      "src/test/resources/merged2+1-no-metadata-merge.sssom.tsv.out");
    	// @formatter:on
    	checkOutput("merged2+1-no-metadata-merge.sssom.tsv");
    }
    
    @Test
    void testMergingExternalMetadata() throws IOException {
    	// @formatter:off
    	runCommand(0, "--input",     "../core/src/test/resources/sample2.sssom.tsv",
    	        "--output-metadata", "../core/src/test/resources/sample-external-metadata.sssom.yml",
    	        "--output",      "src/test/resources/merged-external-metadata.sssom.tsv.out");
    	// @formatter:on
    	checkOutput("merged-external-metadata.sssom.tsv");
    }
    
    @Test
    void testOutputPrefixMap() throws IOException {
    	// @formatter:off
    	runCommand(0, "--input",       "../core/src/test/resources/sample4.sssom.tsv",
    			"--output",            "src/test/resources/output-map-both.sssom.tsv.out",
    			"--include-all",       "--prefix", "MESH=https://meshb.nlm.nih.gov/record/ui?ui=",
    			"--output-prefix-map", "BOTH");
    	// @formatter:on
    	checkOutput("output-map-both.sssom.tsv");
    	
    	// @formatter:off
    	runCommand(0, "--input",       "../core/src/test/resources/sample4.sssom.tsv",
    			"--output",            "src/test/resources/output-map-input.sssom.tsv.out",
    			"--include-all",       "--prefix", "MESH=https://meshb.nlm.nih.gov/record/ui?ui=",
    			"--output-prefix-map", "INPUT");
    	// @formatter:on
    	checkOutput("output-map-input.sssom.tsv");

    	// @formatter:off
    	runCommand(0, "--input",       "../core/src/test/resources/sample4.sssom.tsv",
    			"--output",            "src/test/resources/output-map-sssomt.sssom.tsv.out",
    			"--include-all",       "--prefix", "MESH=https://meshb.nlm.nih.gov/record/ui?ui=",
    			"--output-prefix-map", "SSSOMT");
    	// @formatter:on
    	checkOutput("output-map-sssomt.sssom.tsv");
    }
    
    @Test
    void testTransformPrefixMapPrecedence() throws IOException {
    	// @formatter:off
    	runCommand(0, "--input",       "../core/src/test/resources/sample4.sssom.tsv",
    			"--output",            "src/test/resources/transform-map-1.sssom.tsv.out",
    			"--rule",              "object==mesh:* -> include()",
    			"--prefix-map-from-input");
    	// @formatter:on
    	checkOutput("transform-map-1.sssom.tsv");
    	
    	// @formatter:off
    	runCommand(0, "--input",       "../core/src/test/resources/sample4.sssom.tsv",
    			"--output",            "src/test/resources/transform-map-2.sssom.tsv.out",
    			"--rule",              "object==mesh:* -> include()",
    			"--prefix",            "mesh=http://id.nlm.nih.gov/mesh",
    			"--prefix-map-from-input");
    	// @formatter:on
    	checkOutput("transform-map-2.sssom.tsv");
    }

    /*
     * Run a CLI command.
     */
    private int runCommand(int code, String... strings) {
        try {
            SimpleCLI.main(strings);
        } catch ( Exception e ) {
            Assertions.assertEquals(String.valueOf(code), e.getMessage());
        }
        return code;
    }

    /*
     * Compare a file with its expected contents.
     */
    private void checkOutput(String filename) throws IOException {
        File expected = new File("src/test/resources/" + filename);
        File written = new File("src/test/resources/" + filename + ".out");
        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }
}
