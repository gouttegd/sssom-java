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
import java.util.HashMap;
import java.util.Map;

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

/**
 * A command-line interface to manipulate mapping sets.
 */
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
                Option.builder("i").longOpt("input").hasArg().argName("SET").desc("Load a mapping set").build());
        opts.addOption(Option.builder("o").longOpt("output").hasArg().argName("FILE")
                .desc("Write the mapping set to FILE").build());

        opts.addOption(Option.builder().longOpt("prefix").hasArg().argName("NAME=PREFIX")
                .desc("Declare a prefix for use in SSSOM/T rules").build());
        opts.addOption(Option.builder("R").longOpt("rule").hasArg().argName("RULE").desc("Apply a single SSSOM/T rule")
                .build());
        opts.addOption(Option.builder("r").longOpt("ruleset").hasArg().argName("RULESET")
                .desc("Apply a SSSOM/T ruleset").build());

        opts.addOption(Option.builder("v").longOpt("version").desc("Print version information").build());
        opts.addOption(Option.builder("h").longOpt("help").desc("Print the help message").build());

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
                "Read, manipulate, and write SSSOM mapping sets.\n\nOptions:", options,
                "\nReport bugs to Damien Goutte-Gattat <dgouttegattat@incenp.org>.\n\n");

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

    private void loadTransformRules(CommandLine cmd, MappingProcessor<Mapping> processor,
            Map<String, String> prefixMap) {
        SSSOMTransformReader<Mapping> reader = null;

        if (cmd.hasOption("ruleset")) {
            String inputFile = cmd.getOptionValue("ruleset");
            try {
                reader = new SSSOMTransformReader<Mapping>(new SSSOMTMapping(), inputFile);
                reader.addPrefixMap(prefixMap);
                reader.read();
            } catch (IOException ioe) {
                error(SSSOMT_INPUT_ERROR, "Cannot read ruleset from file %s: %s", inputFile, ioe.getMessage());
            }
        }

        if ( cmd.hasOption("rule") ) {
            if ( reader == null ) {
                reader = new SSSOMTransformReader<Mapping>(new SSSOMTMapping());
                reader.addPrefixMap(prefixMap);
            }
            for ( String rule : cmd.getOptionValues("rule") ) {
                reader.read(rule);
            }
        }

        if ( reader != null ) {
            if ( reader.hasErrors() ) {
                for ( SSSOMTransformError error : reader.getErrors() ) {
                    error(0, "Error when parsing SSSOM/T ruleset: %s", error.getMessage());
                }
                error(SSSOMT_INPUT_ERROR, "Invalid SSSOM/T ruleset");
            }
            processor.addRules(reader.getRules());
        }
    }

    private void transform(CommandLine cmd, MappingSet ms) {
        MappingProcessor<Mapping> processor = new MappingProcessor<Mapping>();
        Map<String, String> prefixMap = new HashMap<String, String>();

        if ( cmd.hasOption("prefix") ) {
            for ( String prefixDecl : cmd.getOptionValues("prefix") ) {
                String[] items = prefixDecl.split("=", 2);
                if ( items.length != 2 ) {
                    error(COMMAND_LINE_ERROR, "invalid prefix declaration: %s", prefixDecl);
                }
                prefixMap.put(items[0], items[1]);
            }
        }

        loadTransformRules(cmd, processor, prefixMap);

        if ( processor.hasRules() ) {
            ms.setMappings(processor.process(ms.getMappings()));
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
