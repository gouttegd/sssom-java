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

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.incenp.obofoundry.sssom.SSSOMReader;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.util.ReaderFactory;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;
import org.obolibrary.robot.RenameOperation;
import org.semanticweb.owlapi.model.IRI;

/**
 * A ROBOT command to rename OWL entities using a SSSOM mapping set as source.
 */
public class RenameFromMappingsCommand implements Command {

    private Options options;
    private HashSet<String> predicates;

    public RenameFromMappingsCommand() {
        options = CommandLineHelper.getCommonOptions();
        options.addOption("i", "input", true, "load ontology from file");
        options.addOption("I", "input-iri", true, "load ontology from an IRI");
        options.addOption("o", "output", true, "save ontology to file");
        options.addOption("s", "sssom", true, "load SSSOM mapping set from file");
        options.addOption("p", "predicate", true, "select mappings with the indicated predicate");
        options.addOption("l", "labels", false, "update labels in addition to IRIs");

        predicates = new HashSet<String>();
        predicates.add("http://purl.obolibrary.org/obo/IAO_0100001");
    }

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public String getDescription() {
        return "rename entities from a SSSOM mapping set";
    }

    @Override
    public String getUsage() {
        return "robot rename -i <INPUT> -s <SSSOM_FILE> -o <OUTPUT>";
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

        if ( !line.hasOption('s') ) {
            throw new IllegalArgumentException("Missing SSSOM mapping set");
        }

        MappingSet ms = null;
        ReaderFactory readerFactory = new ReaderFactory();
        for ( String sssomFile : line.getOptionValues('s') ) {
            SSSOMReader reader = readerFactory.getReader(sssomFile);
            if ( ms == null ) {
                ms = reader.read();
            } else {
                ms.getMappings().addAll(reader.read().getMappings());
            }
        }

        boolean updateLabels = line.hasOption('l');
        boolean noFiltering = false;
        if ( line.hasOption('p') ) {
            PrefixManager pm = new PrefixManager();
            pm.add(ioHelper.getPrefixes());

            predicates.clear();
            for ( String value : line.getOptionValues('p') ) {
                if ( value.equalsIgnoreCase("all") || value.equalsIgnoreCase("any") ) {
                    noFiltering = true;
                } else {
                    predicates.add(pm.expandIdentifier(value));
                }
            }
        }

        HashMap<String, String> renames = new HashMap<String, String>();
        HashMap<IRI, String> labels = new HashMap<IRI, String>();
        for ( Mapping mapping : ms.getMappings() ) {
            if ( noFiltering || predicates.contains(mapping.getPredicateId()) ) {
                renames.put(mapping.getSubjectId(), mapping.getObjectId());
                if ( updateLabels && mapping.getObjectLabel() != null ) {
                    labels.put(IRI.create(mapping.getObjectId()), mapping.getObjectLabel());
                }
            }
        }
        RenameOperation.renameFull(state.getOntology(), ioHelper, renames, labels, true);

        CommandLineHelper.maybeSaveOutput(line, state.getOntology());

        return state;
    }

}
