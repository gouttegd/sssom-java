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

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.TSVWriter;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.transform.MappingProcessor;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;

public class SimpleCLI {

    private final static int NO_ERROR = 0;
    private final static int COMMAND_LINE_ERROR = 1;
    private final static int SSSOM_INPUT_ERROR = 2;
    private final static int SSSOM_OUTPUT_ERROR = 3;
    private final static int SSSOMT_INPUT_ERROR = 4;

    private boolean noExit = false;

    private void print(PrintStream stream, String format, Object... args) {
        stream.printf("sssom-cli: ");
        stream.printf(format, args);
        stream.print('\n');
    }

    /*
     * We wrap System.exit() so that we don't actually terminate the JVM when
     * running the tests.
     */
    private void exit(int code) {
        if ( noExit ) {
            throw new RuntimeException(String.valueOf(code));
        } else {
            System.exit(code);
        }
    }

    private void info(String format, Object... args) {
        print(System.out, format, args);
    }

    private void error(int code, String format, Object... args) {
        print(System.err, format, args);
        if ( code != 0 ) {
            exit(code);
        }
    }

    private Options getOptions() {
        Options opts = new Options();

        opts.addOption(
                Option.builder("i").longOpt("input").hasArg().argName("SET").desc("A mapping set to load.").build());
        opts.addOption(Option.builder("o").longOpt("output").hasArg().argName("FILE")
                .desc("The file to write the mapping set to.").build());

        opts.addOption(Option.builder("r").longOpt("ruleset").hasArg().argName("RULESET")
                .desc("A SSSOM/T ruleset to apply.").build());

        opts.addOption(Option.builder("v").longOpt("version").desc("Print version information.").build());
        opts.addOption(Option.builder("h").longOpt("help").desc("Print the help message.").build());

        return opts;
    }

    private CommandLine parseArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch ( ParseException e ) {
            showHelp(options, e.getMessage());
        }

        if ( cmd.hasOption('h') ) {
            showHelp(options, null);
        } else if ( cmd.hasOption('v') ) {
            System.out.println(String.format("SSSOM-CLI %s\n" + "Copyright © 2023 Damien Goutte-Gattat\n\n"
                    + "This program is released under the GNU General Public License.", getVersion()));
            System.exit(NO_ERROR);
        }

        return cmd;
    }

    private String getVersion() {
        return SimpleCLI.class.getPackage().getImplementationVersion();
    }

    private void showHelp(Options options, String message) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("sssom-cli [options] [-i <MAPPINGSET>...] [-o <OUTPUTFILE>]",
                "Read and write SSSOM mapping sets.\n\nOptions:", options,
                "\nReport bugs to Damien Goutte-Gattat <dgouttegattat@incenp.org>\n\n");

        if ( message != null ) {
            error(COMMAND_LINE_ERROR, "Invalid command line: %s", message);
        }

        System.exit(NO_ERROR);
    }

    private MappingSet loadInputs(CommandLine cmd) {
        MappingSet ms = null;
        String[] inputFiles = cmd.hasOption("i") ? cmd.getOptionValues("i") : new String[] { "-" };
        for ( String input : inputFiles ) {
            try {
                boolean stdin = input.equals("-");
                TSVReader reader = stdin ? new TSVReader(System.in) : new TSVReader(input);
                if ( ms == null ) {
                    ms = reader.read();
                } else {
                    ms.getMappings().addAll(reader.read().getMappings());
                }
            } catch ( IOException ioe ) {
                error(SSSOM_INPUT_ERROR, "Cannot read file %s: %s", input, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                error(SSSOM_INPUT_ERROR, "Invalid SSSOM data in file %s: %s", input, sfe.getMessage());
            }
        }

        return ms;
    }

    private void writeOutput(CommandLine cmd, MappingSet set) {
        boolean stdout = !cmd.hasOption("o") || cmd.getOptionValue("o").equals("-");
        try {
            TSVWriter writer = stdout ? new TSVWriter(System.out) : new TSVWriter(cmd.getOptionValue("o"));
            writer.write(set);
        } catch ( IOException ioe ) {
            error(SSSOM_OUTPUT_ERROR, "cannot write to file %s: %s", stdout ? "-" : cmd.getOptionValues("o"),
                    ioe.getMessage());
        }
    }

    private void transform(CommandLine cmd, MappingSet ms) {
        if ( !cmd.hasOption("r") ) {
            return;
        }

        try {
            SSSOMTransformReader<Mapping> reader = new SSSOMTransformReader<Mapping>(new SSSOMTMapping(),
                    cmd.getOptionValue("r"));
            reader.read();
            if ( reader.hasErrors() ) {
                for ( SSSOMTransformError error : reader.getErrors() ) {
                    error(0, "Error when parsing SSSOM/T ruleset: %s", error.getMessage());
                }
                error(SSSOMT_INPUT_ERROR, "Invalid SSSOM/T ruleset");
            }

            MappingProcessor<Mapping> processor = new MappingProcessor<Mapping>();
            processor.addRules(reader.getRules());

            ms.setMappings(processor.process(ms.getMappings()));
        } catch ( IOException ioe ) {
            error(SSSOMT_INPUT_ERROR, "Cannot read ruleset from file %s: %s", cmd.getOptionValue("r"),
                    ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        SimpleCLI cli = new SimpleCLI();
        if ( System.getProperty("org.incenp.obofoundry.sssom.cli.SimpleCLI#inTest") != null ) {
            cli.noExit = true;
        }

        CommandLine cmd = cli.parseArguments(args);

        MappingSet ms = cli.loadInputs(cmd);
        cli.transform(cmd, ms);
        cli.writeOutput(cmd, ms);

        cli.exit(NO_ERROR);
    }
}