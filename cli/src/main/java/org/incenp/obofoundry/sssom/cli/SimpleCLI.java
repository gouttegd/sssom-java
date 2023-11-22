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
import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.TSVWriter;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.transform.MappingProcessingRule;
import org.incenp.obofoundry.sssom.transform.MappingProcessor;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A command-line interface to manipulate mapping sets.
 */
@Command(name = "sssom-cli",
        mixinStandardHelpOptions = true,
        versionProvider = CommandHelper.class,
        description = "Read, manipulate, and write SSSOM mapping sets.",
        footer = "Report bugs to <dgouttegattat@incenp.org>.",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n")
public class SimpleCLI implements Runnable {

    /*
     * Command line options
     */

    @Option(names = { "-i", "--input" },
            paramLabel = "SET",
            description = "Load a mapping set. Default is to read from standard input.")
    private String[] inputFiles = new String[] { "-" };

    @Option(names = { "-o", "--output" },
            paramLabel = "FILE",
            description = "Write the mapping set to FILE. Default is to write to standard output.",
            defaultValue = "-")
    private String outputFile;

    @Option(names = { "-r", "--ruleset" }, paramLabel = "RULESET", description = "Apply a SSSOM/T ruleset.")
    private String rulesetFile;

    @Option(names = { "-R", "--rule" }, paramLabel = "RULE", description = "Apply a single SSSOM/T rule.")
    private String[] rules = new String[] {};

    @Option(names = "--prefix", paramLabel = "NAME=PREFIX", description = "Declare a prefix for use in a SSSOM/T rule.")
    private Map<String, String> prefixMap = new HashMap<String, String>();

    @Option(names = { "-a", "--include-all" },
            description = "Add a default include rule at the end of the processing set.")
    private boolean includeAll;

    private CommandHelper helper = new CommandHelper();

    public static void main(String[] args) {
        SimpleCLI cli = new SimpleCLI();
        int rc = new picocli.CommandLine(cli).setExecutionExceptionHandler(cli.helper).execute(args);
        cli.helper.exit(rc);
    }

    @Override
    public void run() {
        MappingSet ms = loadInputs();
        transform(ms);
        writeOutput(ms);
    }

    private MappingSet loadInputs() {
        MappingSet ms = null;
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
                helper.error("Cannot read file %s: %s", input, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                helper.error("Invalid SSSOM data in file %s: %s", input, sfe.getMessage());
            }
        }

        return ms;
    }

    private void writeOutput(MappingSet set) {
        boolean stdout = outputFile.equals("-");
        try {
            TSVWriter writer = stdout ? new TSVWriter(System.out) : new TSVWriter(outputFile);
            writer.write(set);
        } catch ( IOException ioe ) {
            helper.error("cannot write to file %s: %s", stdout ? "-" : outputFile, ioe.getMessage());
        }
    }

    private void loadTransformRules(MappingProcessor<Mapping> processor) {
        SSSOMTransformReader<Mapping> reader = null;

        if ( rulesetFile != null ) {
            try {
                reader = new SSSOMTransformReader<Mapping>(new SSSOMTMapping(), rulesetFile);
                reader.addPrefixMap(prefixMap);
                reader.read();
            } catch (IOException ioe) {
                helper.error("Cannot read ruleset from file %s: %s", rulesetFile, ioe.getMessage());
            }
        }

        if ( rules.length > 0 ) {
            if ( reader == null ) {
                reader = new SSSOMTransformReader<Mapping>(new SSSOMTMapping());
                reader.addPrefixMap(prefixMap);
            }
            for ( String rule : rules ) {
                reader.read(rule);
            }
        }

        if ( reader != null ) {
            if ( reader.hasErrors() ) {
                for ( SSSOMTransformError error : reader.getErrors() ) {
                    helper.warn("Error when parsing SSSOM/T ruleset: %s", error.getMessage());
                }
                helper.error("Invalid SSSOM/T ruleset");
            }
            processor.addRules(reader.getRules());
        }
    }

    private void transform(MappingSet ms) {
        MappingProcessor<Mapping> processor = new MappingProcessor<Mapping>();

        loadTransformRules(processor);

        if ( processor.hasRules() ) {
            if ( includeAll ) {
                processor.addRule(new MappingProcessingRule<Mapping>(null, null, (mapping) -> mapping));
            }
            ms.setMappings(processor.process(ms.getMappings()));
        }
    }
}
