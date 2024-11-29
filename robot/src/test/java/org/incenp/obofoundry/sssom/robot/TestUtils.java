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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.robot;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.obolibrary.robot.CommandManager;
import org.obolibrary.robot.ConvertCommand;

/**
 * Helper methods for testing the ROBOT plugin.
 */
public class TestUtils {

    /**
     * Runs a ROBOT command.
     * 
     * @param strings The arguments to the command.
     */
    public static void runCommand(String... strings) {
        CommandManager robot = new CommandManager();
        robot.addCommand("sssom-inject", new SSSOMInjectionCommand());
        robot.addCommand("sssom-rename", new RenameFromMappingsCommand());
        robot.addCommand("xref-extract", new XrefExtractCommand());
        robot.addCommand("convert", new ConvertCommand());
        robot.main(strings);
    }

    /**
     * Checks that an output file (expected to be {@code src/test/resources/output/}
     * and with the same filename as the specified name, but with an added
     * {@code .out} extension) is identical to its expected image.
     * 
     * @param filename The name of the file containing the expected contents.
     * @throws IOException If any I/O error occurs.
     */
    public static void checkOutput(String filename) throws IOException {
        File expected = new File("src/test/resources/output/" + filename);
        File written = new File("src/test/resources/output/" + filename + ".out");
        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }
}
