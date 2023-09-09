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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.TSVWriter;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.owl.XrefExtractor;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ROBOT command to infer mappings from cross-references in a ontology and
 * export them to a SSSOM file.
 */
public class XrefExtractCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(XrefExtractCommand.class);

    private Options options;

    public XrefExtractCommand() {
        options = CommandLineHelper.getCommonOptions();
        options.addOption("i", "input", true, "load ontology from file");
        options.addOption("I", "input-iri", true, "load ontology from an IRI");
        options.addOption(null, "mapping-file", true, "write extracted mappings to file");
        options.addOption(null, "metadata", true, "Use metadata from the specified YAML file");
        options.addOption(null, "permissive", false, "include cross-references with unknown prefixes");
        options.addOption(null, "all-xrefs", false, "create mappings from all cross-references");
        options.addOption(null, "ignore-treat-xrefs", false, "Ignore treat-xrefs-as-... annotations in the ontology");
        options.addOption(null, "map-prefix-to-predicate", true,
                "Use specified predicate for cross-references with specified prefix");
        options.addOption(null, "drop-duplicates", false, "Drop all duplicated cross-references");
        options.addOption(null, "include-obsoletes", false, "Do not ignore cross-references on obsoleted terms");
    }

    @Override
    public String getName() {
        return "xref-extract";
    }

    @Override
    public String getDescription() {
        return "extract SSSOM mappings from cross-references in the ontology";
    }

    @Override
    public String getUsage() {
        return "robot xref-extract -i <INPUT> --mapping-file <OUTPUT>";
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void main(String[] args) {
        try {
            execute(null, args);
        } catch ( Exception e ) {
            CommandLineHelper.handleException(e);
        }
    }

    @Override
    public CommandState execute(CommandState state, String[] args) throws Exception {
        CommandLine line = CommandLineHelper.getCommandLine(getUsage(), options, args);
        if ( line == null ) {
            return null;
        }

        IOHelper ioHelper = CommandLineHelper.getIOHelper(line);
        state = CommandLineHelper.updateInputOntology(ioHelper, state, line);

        Map<String, String> prefixMap = ioHelper.getPrefixes();
        MappingSet ms = null;

        if ( line.hasOption("metadata") ) {
            TSVReader reader = new TSVReader(null, line.getOptionValue("metadata"));
            ms = reader.read(true);
            prefixMap.putAll(ms.getCurieMap());
        } else {
            ms = MappingSet.builder().curieMap(new HashMap<String, String>()).build();
        }

        XrefExtractor extractor = new XrefExtractor();
        extractor.setPrefixMap(prefixMap);
        if ( !line.hasOption("ignore-treat-xrefs") ) {
            extractor.fillPrefixToPredicateMap(state.getOntology());
        }
        if ( line.hasOption("map-prefix-to-predicate") ) {
            for ( String ppMapping : line.getOptionValues("map-prefix-to-predicate") ) {
                String[] parts = ppMapping.split(" ", 2);
                if ( parts.length != 2 ) {
                    throw new IllegalArgumentException("Invalid argument for --map-prefix-to-predicate: " + ppMapping);
                }
                extractor.addPrefixToPredicateMapping(parts[0], parts[1]);
            }
        }
        if ( line.hasOption("include-obsoletes") ) {
            extractor.includeObsoletes(true);
        }

        MappingSet extracted = extractor.extract(state.getOntology(), line.hasOption("permissive"),
                line.hasOption("all-xrefs"));
        ms.setMappings(extracted.getMappings());
        ms.getCurieMap().putAll(extracted.getCurieMap());

        if ( !extractor.getUnknownPrefixNames().isEmpty() ) {
            logger.warn("Unknown prefix names found in cross-references: "
                    + String.join(" ", extractor.getUnknownPrefixNames()));
        }

        if ( line.hasOption("drop-duplicates") ) {
            MappingCardinality.inferCardinality(ms.getMappings());
            List<Mapping> filteredIn = new ArrayList<Mapping>();
            List<Mapping> filteredOut = new ArrayList<Mapping>();

            for ( Mapping m : ms.getMappings() ) {
                if ( m.getMappingCardinality() == MappingCardinality.MANY_TO_MANY
                        || m.getMappingCardinality() == MappingCardinality.MANY_TO_ONE ) {
                    filteredOut.add(m);
                } else {
                    filteredIn.add(m);
                }
            }
            ms.setMappings(filteredIn);

            if ( !filteredOut.isEmpty() ) {
                PrefixManager pm = new PrefixManager();
                pm.add(ms.getCurieMap());
                Map<String, ArrayList<String>> duplicates = new HashMap<String, ArrayList<String>>();

                for ( Mapping dropped : filteredOut ) {
                    String subjectId = pm.shortenIdentifier(dropped.getSubjectId());
                    String objectId = pm.shortenIdentifier(dropped.getObjectId());
                    ArrayList<String> subjects = duplicates.getOrDefault(objectId, new ArrayList<String>());
                    subjects.add(subjectId);
                    duplicates.put(objectId, subjects);
                }

                for ( String object : duplicates.keySet() ) {
                    logger.warn(String.format("Cross-reference ignored: %s mapped to %s", object,
                            String.join(", ", duplicates.get(object))));
                }
            }
        }

        if ( line.hasOption("mapping-file") ) {
            TSVWriter writer = new TSVWriter(line.getOptionValue("mapping-file"));
            writer.write(ms);
        }

        return state;
    }
}
