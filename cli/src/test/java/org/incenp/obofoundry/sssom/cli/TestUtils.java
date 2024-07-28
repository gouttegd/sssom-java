/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

/**
 * Helper methods for testing.
 */
public class TestUtils {

    /**
     * Runs a SSSOM-CLI command, checks that it returns the expected error code, and
     * raises an assertion failure if it does not. Additionally checks that it
     * produces the expected output, and again raises an assertion failure
     * otherwise.
     * 
     * @param code   The expected return code.
     * @param inputs A list of SSSOM/TSV files to be used as input. May be
     *               {@code null} (e.g. to read from stdin, or to pass custom
     *               {@code --input} options). This method will look for the files
     *               in the {@code src/test/resources/sets} folder first, then in
     *               the module’s top-level directory.
     * @param output The filename where the output should be sent. May be
     *               {@code null}. If not {@code null} and the return code is zero,
     *               this method will check the output against a file with the same
     *               name in {@code src/test/resources/output}.
     * @param others Arbitrary additional arguments to pass to the command. May be
     *               {@code null}.
     * @throws IOException Should not happen (all I/O errors should be caught by the
     *                     command itself).
     */
    public static void runCommand(int code, String[] inputs, String output, String[] others) throws IOException {
        ArrayList<String> args = new ArrayList<String>();

        if ( inputs != null ) {
            for ( String input : inputs ) {
                File f = new File("../core/src/test/resources/sets/" + input);
                if ( !f.exists() ) {
                    f = new File("src/test/resources/sets/" + input);
                }
                args.add(f.exists() ? f.getPath() : input);
            }
        }

        if ( output != null ) {
            args.add("--output");
            args.add("src/test/resources/output/" + output + ".out");
        }

        if ( others != null ) {
            for ( String other : others ) {
                args.add(other);
            }
        }

        String[] argsArray = new String[args.size()];
        args.toArray(argsArray);

        Assertions.assertEquals(code, SimpleCLI.run(argsArray));

        if ( code == 0 && output != null ) {
            File expected = new File("src/test/resources/output/" + output);
            File written = new File("src/test/resources/output/" + output + ".out");
            boolean same = FileUtils.contentEquals(expected, written);
            Assertions.assertTrue(same);
            if ( same ) {
                written.delete();
            }
        }
    }
}
