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

package org.incenp.obofoundry.sssom.robot;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obolibrary.robot.CommandManager;
import org.obolibrary.robot.ConvertCommand;

public class SSSOMInjectionCommandTest {

    /*
     * Check generation of a FBbt-to-Uberon/CL bridge.
     */
    @Test
    void testSampleBridge() throws IOException {
        // @formatter:off
        runCommand("sssom-inject",
                "--create",
                "--sssom", "../core/src/test/resources/sets/fbbt.sssom.tsv",
                "--ruleset", "../core/src/test/resources/rules/fbbt-bridge.rules",
                "--exclude-rule", "xrefs",
                "convert",
                "--format", "ofn",
                "--output", "src/test/resources/output/fbbt-bridge.ofn.out");
        // @formatter:on
        checkOutput("fbbt-bridge.ofn");
    }

    /*
     * Check the production of cross-references from mappings.
     */
    @Test
    void testSampleXref() throws IOException {
        // @formatter:off
        runCommand("sssom-inject",
                "--create",
                "--sssom", "../core/src/test/resources/sets/fbbt.sssom.tsv",
                "--ruleset", "../core/src/test/resources/rules/fbbt-bridge.rules",
                "--exclude-rule", "fbbt",
                "convert",
                "--format", "ofn",
                "--output", "src/test/resources/output/fbbt-xrefs.ofn.out");
        // @formatter:on
        checkOutput("fbbt-xrefs.ofn");
    }

    /*
     * Run a ROBOT command.
     */
    private void runCommand(String... strings) {
        CommandManager robot = new CommandManager();
        robot.addCommand("sssom-inject", new SSSOMInjectionCommand());
        robot.addCommand("convert", new ConvertCommand());
        robot.main(strings);
    }

    /*
     * Compare a file with its expected contents.
     */
    private void checkOutput(String filename) throws IOException {
        File expected = new File("src/test/resources/output/" + filename);
        File written = new File("src/test/resources/output/" + filename + ".out");
        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }
}
