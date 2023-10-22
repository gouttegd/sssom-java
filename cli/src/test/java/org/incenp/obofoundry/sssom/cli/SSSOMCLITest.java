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
        System.setProperty("org.incenp.obofoundry.sssom.cli.SimpleCLI#inTest", "yes");
    }

    @Test
    void testSimpleSSSOMTRuleset() throws IOException {
        // @formatter:off
        runCommand(0, "--input", "../core/src/test/resources/sample1.sssom.tsv",
                      "--ruleset", "../core/src/test/resources/ruleset2.sssomt",
                      "--output", "src/test/resources/filtered1.sssom.tsv.out");
        // @formatter:on
        checkOutput("filtered1.sssom.tsv");
    }

    private int runCommand(int code, String... strings) {
        try {
            SimpleCLI.main(strings);
        } catch ( Exception e ) {
            Assertions.assertEquals(String.valueOf(code), e.getMessage());
        }
        return code;
    }

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
