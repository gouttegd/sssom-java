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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.incenp.obofoundry.sssom.TSVWriter;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.owl.XrefExtractor;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;

/**
 * A ROBOT command to infer mappings from cross-references in a ontology and
 * export them to a SSSOM file.
 */
public class XrefExtractCommand implements Command {

    private Options options;

    public XrefExtractCommand() {
        options = CommandLineHelper.getCommonOptions();
        options.addOption("i", "input", true, "load ontology from file");
        options.addOption(null, "mapping-file", true, "write extracted mappings to file");
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

        XrefExtractor extractor = new XrefExtractor();
        extractor.setPrefixMap(ioHelper.getPrefixes());
        MappingSet ms = extractor.extract(state.getOntology());

        if ( line.hasOption("mapping-file") ) {
            TSVWriter writer = new TSVWriter(line.getOptionValue("mapping-file"));
            writer.write(ms);
            writer.close();
        }

        return state;
    }
}
