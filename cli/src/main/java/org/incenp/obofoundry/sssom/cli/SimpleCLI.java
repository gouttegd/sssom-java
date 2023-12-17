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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.ExtendedPrefixMap;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.TSVWriter;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
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

    @Option(names = "--split",
            paramLabel = "DIRECTORY",
            description = "Split the set along subject and object prefix names and write the split sets in the specified directory.")
    private String splitOutputDirectory;

    @Option(names = { "-r", "--ruleset" }, paramLabel = "RULESET", description = "Apply a SSSOM/T ruleset.")
    private String rulesetFile;

    @Option(names = { "-R", "--rule" }, paramLabel = "RULE", description = "Apply a single SSSOM/T rule.")
    private String[] rules = new String[] {};

    @Option(names = "--prefix", paramLabel = "NAME=PREFIX", description = "Declare a prefix for use in SSSOM/T.")
    private Map<String, String> prefixMap = new HashMap<String, String>();

    @Option(names = "--prefix-map",
            paramLabel = "METAFILE",
            description = "Use the prefix map from specified metadata file for SSSOM/T.")
    private String externalPrefixMap;

    @Option(names = "--prefix-map-from-input",
            description = "Use the prefix map of the input set(s) for SSSOM/T (not recommended).")
    private boolean useInputPrefixMap;

    @Option(names = { "-a", "--include-all" },
            description = "Add a default include rule at the end of the processing set.")
    private boolean includeAll;

    @Option(names = { "-c", "--force-cardinality" }, description = "Include mapping cardinality values.")
    private boolean forceCardinality;

    @Option(names = "--mangle-iris",
            paramLabel = "EPM",
            description = "Use an extended prefix map (EPM) to mangle IRIs in the mapping set. This is done before any other processing.")
    private String epmFile;

    @Option(names = "--output-prefix-map",
            paramLabel = "SRC",
            description = "Specify the source of the output prefix map. Possible values: ${COMPLETION-CANDIDATES}.")
    private OutputMapSource outputPrefixMapSource = OutputMapSource.BOTH;

    private CommandHelper helper = new CommandHelper();

    public static void main(String[] args) {
        SimpleCLI cli = new SimpleCLI();
        int rc = new picocli.CommandLine(cli).setExecutionExceptionHandler(cli.helper)
                .setUsageHelpLongOptionsMaxWidth(23).setUsageHelpAutoWidth(true).execute(args);
        cli.helper.exit(rc);
    }

    @Override
    public void run() {
        MappingSet ms = loadInputs();
        transform(ms);
        postProcess(ms);
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
                    MappingSet tmp = reader.read();
                    ms.getMappings().addAll(tmp.getMappings());
                    ms.getCurieMap().putAll(tmp.getCurieMap());
                }
            } catch ( IOException ioe ) {
                helper.error("Cannot read file %s: %s", input, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                helper.error("Invalid SSSOM data in file %s: %s", input, sfe.getMessage());
            }
        }

        if ( epmFile != null ) {
            ExtendedPrefixMap epm = null;
            try {
                if ( epmFile.equals("obo") ) {
                    epm = new ExtendedPrefixMap(ExtendedPrefixMap.class.getResourceAsStream("/obo.epm.json"));
                } else {
                    epm = new ExtendedPrefixMap(epmFile);
                }
            } catch ( IOException ioe ) {
                helper.error("Cannot read extended prefix map: %s", ioe.getMessage());
            }
            epm.canonicalise(ms);
        }

        return ms;
    }

    private void writeOutput(MappingSet set) {
        if ( outputPrefixMapSource == OutputMapSource.SSSOMT ) {
            // Replace the input map with the SSSOM/T map
            set.setCurieMap(prefixMap);
        } else if ( outputPrefixMapSource == OutputMapSource.BOTH ) {
            // Add the SSSOM/T map to the input map (the SSSOM/T map takes precedence)
            set.getCurieMap().putAll(prefixMap);
        }

        if ( splitOutputDirectory != null ) {
            writeSplitSet(set, splitOutputDirectory);
            return; // Skip writing the full set when writing splits
        }
        boolean stdout = outputFile.equals("-");
        try {
            TSVWriter writer = stdout ? new TSVWriter(System.out) : new TSVWriter(outputFile);
            writer.write(set);
        } catch ( IOException ioe ) {
            helper.error("cannot write to file %s: %s", stdout ? "-" : outputFile, ioe.getMessage());
        }
    }

    private void writeSplitSet(MappingSet ms, String directory) {
        File dir = new File(directory);
        if ( !dir.isDirectory() && !dir.mkdirs() ) {
            helper.error("cannot create directory %s", directory);
        }

        HashMap<String, List<Mapping>> mappingsBySplit = new HashMap<String, List<Mapping>>();
        PrefixManager pm = new PrefixManager();
        pm.add(ms.getCurieMap());

        for ( Mapping mapping : ms.getMappings() ) {
            String subjectPrefixName = pm.getPrefixName(mapping.getSubjectId());
            String objectPrefixName = pm.getPrefixName(mapping.getObjectId());
            if ( subjectPrefixName != null && objectPrefixName != null ) {
                String splitId = subjectPrefixName + "-to-" + objectPrefixName;
                mappingsBySplit.computeIfAbsent(splitId, k -> new ArrayList<Mapping>()).add(mapping);
            }
        }

        for ( String splitId : mappingsBySplit.keySet() ) {
            MappingSet splitSet = ms.toBuilder().mappings(null).build();
            splitSet.setMappings(mappingsBySplit.get(splitId));

            File output = new File(dir, splitId + ".sssom.tsv");
            try {
                TSVWriter writer = new TSVWriter(output);
                writer.write(splitSet);
            } catch ( IOException ioe ) {
                helper.error("cannot write to file %s: %s", output.getName(), ioe.getMessage());
            }
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
            prefixMap.putAll(reader.getPrefixMap());
        }
    }

    private void setTransformPrefixMap(MappingSet ms) {
        HashMap<String, String> map = new HashMap<String, String>();
        if ( useInputPrefixMap ) {
            map.putAll(ms.getCurieMap());
        }
        if ( externalPrefixMap != null ) {
            try {
                TSVReader reader = new TSVReader(null, externalPrefixMap);
                map.putAll(reader.read(true).getCurieMap());
            } catch ( IOException ioe ) {
                helper.error("Cannot read file %s: %s", externalPrefixMap, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                helper.error("Invalid SSSOM data in file %s: %s", externalPrefixMap, sfe.getMessage());
            }
        }

        // Prefixes declared on the command line always take precedence over the input
        // maps and the external prefix map
        map.forEach((k, v) -> prefixMap.putIfAbsent(k, v));
    }

    private void transform(MappingSet ms) {
        MappingProcessor<Mapping> processor = new MappingProcessor<Mapping>();

        setTransformPrefixMap(ms);
        loadTransformRules(processor);

        if ( processor.hasRules() ) {
            if ( includeAll ) {
                processor.addRule(new MappingProcessingRule<Mapping>(null, null, (mapping) -> mapping));
            }
            ms.setMappings(processor.process(ms.getMappings()));
        }
    }

    private void postProcess(MappingSet ms) {
        if ( forceCardinality ) {
            MappingCardinality.inferCardinality(ms.getMappings());
        } else {
            ms.getMappings().forEach(mapping -> mapping.setMappingCardinality(null));
        }
    }

    enum OutputMapSource {
        INPUT,
        SSSOMT,
        BOTH;
    }
}
