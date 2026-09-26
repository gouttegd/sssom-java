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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
     *               in the {@code src/test/resources/sets} directory first, then in
     *               in {@code ../ext/src/test/resources/sets} directory, then in
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
                    f = new File("../ext/src/test/resources/sets/" + input);
                }
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

    /**
     * Checks that two files have the same contents, and throws an assertion error
     * if not.
     * 
     * @param expected The name of the file containing the expected contents. The
     *                 file is expected to be in the
     *                 {@code src/test/resources/output} directory.
     * @param written  The name of the file to check. The file is expected to be in
     *                 the same directory as {@code expected}. If the check passes,
     *                 that file will be automatically deleted.
     * @throws IOException If any I/O error occurs.
     */
    public static void assertFileEquals(String expected, String written) throws IOException {
        File expectedFile = new File("src/test/resources/output/" + expected);
        File writtenFile = new File("src/test/resources/output/" + written);
        boolean same = FileUtils.contentEquals(expectedFile, writtenFile);
        Assertions.assertTrue(same);
        if ( same ) {
            writtenFile.delete();
        }
    }

    /**
     * Checks that two GZipped files have the same contents, and throws an assertion
     * error if not.
     * <p>
     * We cannot directly compare the <em>compressed</em> contents (e.g. with
     * {@link FileUtils#contentEquals(File, File)}) because the GZip output may vary
     * from one version of the JRE to another. So we need a distinct method to
     * uncompress the files first and then compare the uncompressed contents.
     * 
     * @param expected The name of the file containing the expected contents. The
     *                 file is expected to be in the
     *                 {@code src/test/resources/output} directory.
     * @param written  The name of the file to check. The file is expected to be in
     *                 the same directory as {@code expected}. If the check passes,
     *                 that file will be automatically deleted.
     * @throws IOException If any I/O error occurs.
     */
    public static void assertGZipFileEquals(String expected, String written) throws IOException {
        File expectedFile = new File("src/test/resources/output/" + expected);
        File writtenFile = new File("src/test/resources/output/" + written);
        GZIPInputStream expectedInput = new GZIPInputStream(new FileInputStream(expectedFile));
        GZIPInputStream writtenInput = new GZIPInputStream(new FileInputStream(writtenFile));
        boolean same = IOUtils.contentEquals(expectedInput, writtenInput);
        expectedInput.close();
        writtenInput.close();
        Assertions.assertTrue(same);
        if ( same ) {
            writtenFile.delete();
        }
    }
}
