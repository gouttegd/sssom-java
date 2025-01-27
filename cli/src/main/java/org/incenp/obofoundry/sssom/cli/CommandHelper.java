/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024,2025 Damien Goutte-Gattat
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

import java.io.PrintStream;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.ParseResult;

/**
 * A helper class for command line tools.
 */
public class CommandHelper implements IVersionProvider, IExecutionExceptionHandler {

    private String name = "sssom-cli";

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
        warn(ex.getMessage());
        return commandLine.getCommandSpec().exitCodeOnExecutionException();
    }

    @Override
    public String[] getVersion() {
        return new String[] {
                "sssom-cli (SSSOM-Java " + CommandHelper.class.getPackage().getImplementationVersion() + ")",
                "Copyright © 2023,2024,2025 Damien Goutte-Gattat", "",
                "This program is released under the GNU General Public License.",
                "See the COPYING file or <http://www.gnu.org/licenses/gpl.html>." };
    }

    /**
     * Prints an informative message on standard output.
     * 
     * @param format The message to print, as a format string.
     * @param args   Arguments for the format specifiers inside the message.
     */
    public void info(String format, Object... args) {
        print(System.out, format, args);
    }

    /**
     * Prints a warning message on standard error output.
     * 
     * @param format The message to print, as a format string.
     * @param args   Arguments for the format specifiers inside the message.
     */
    public void warn(String format, Object... args) {
        print(System.err, format, args);
    }

    /**
     * Prints an error message on standard error output and exits the application.
     * 
     * @param format The message to print, as a format string.
     * @param args   Arguments for the format specifiers inside the message.
     */
    public void error(String format, Object... args) {
        // Throw an exception to interrupt the application; the exception will be caught
        // by PicoCLI and the error message will be displayed by the exception handler.
        throw new RuntimeException(String.format(format, args));
    }

    private void print(PrintStream stream, String format, Object... args) {
        stream.printf(name + ": ");
        stream.printf(format, args);
        stream.print('\n');
    }
}
