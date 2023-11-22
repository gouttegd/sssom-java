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
