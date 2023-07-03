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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.incenp.obofoundry.sssom.CrossSpeciesBridgeGenerator;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A ROBOT command to translate mappings into OWL axioms and inject them into an
 * ontology.
 */
public class SSSOMInjectionCommand implements Command {

    private Options options;

    public SSSOMInjectionCommand() {
        options = CommandLineHelper.getCommonOptions();
        options.addOption("i", "input", true, "load ontology from file");
        options.addOption("o", "output", true, "save ontology to file");
        options.addOption("s", "sssom", true, "load SSSOM mapping set from file");
        options.addOption(null, "sssom-metadata", true, "load mapping set metadata from specified file");
        options.addOption(null, "cross-species", true, "inject cross-species bridging axioms for specified taxon");
        options.addOption(null, "no-merge", false, "do not merge mapping-derived axioms into the ontology.");
        options.addOption(null, "bridge-file", true, "write mapping-derived axioms into the specified file");
    }

    @Override
    public String getName() {
        return "sssom-inject";
    }

    @Override
    public String getDescription() {
        return "inject axioms from a SSSOM mapping set into the ontology";
    }

    @Override
    public String getUsage() {
        return "robot sssom-inject -i <INPUT> -s <SSSOM_FILE> [axiom type options] -o <OUTPUT>";
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

        if ( state == null ) {
            state = new CommandState();
        }
        IOHelper ioHelper = CommandLineHelper.getIOHelper(line);
        state = CommandLineHelper.updateInputOntology(ioHelper, state, line);

        if ( !line.hasOption("sssom") ) {
            throw new IllegalArgumentException("Missing SSSOM mapping set");
        }
        TSVReader reader = new TSVReader(line.getOptionValue("sssom"), line.getOptionValue("sssom-metadata"));
        MappingSet mappingSet = reader.read();

        OWLOntology ontology = state.getOntology();
        Set<OWLAxiom> bridgingAxioms = new HashSet<OWLAxiom>();

        if ( line.hasOption("cross-species") ) {
            // TODO: Check both subject and object exist in the ontology and are not
            // obsolete.
            // TODO: Generate IAO:0000589 ("OBO unique label") annotations when needed.
            IRI taxonIRI = ioHelper.createIRI(line.getOptionValue("cross-species"));
            CrossSpeciesBridgeGenerator gen = new CrossSpeciesBridgeGenerator(
                    ontology.getOWLOntologyManager().getOWLDataFactory(), taxonIRI);
            for ( Mapping mapping : mappingSet.getMappings() ) {
                OWLAxiom ax = gen.generateAxiom(mapping);
                if ( ax != null ) {
                    bridgingAxioms.add(ax);
                }
            }
        }

        if ( !line.hasOption("no-merge") && !bridgingAxioms.isEmpty() ) {
            ontology.getOWLOntologyManager().addAxioms(ontology, bridgingAxioms);
        }

        if ( line.hasOption("bridge-file") && !bridgingAxioms.isEmpty() ) {
            OWLOntology bridgeOntology = ontology.getOWLOntologyManager().createOntology(bridgingAxioms);
            ioHelper.saveOntology(bridgeOntology, line.getOptionValue("bridge-file"));
        }

        CommandLineHelper.maybeSaveOutput(line, state.getOntology());
        return state;
    }

}
