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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.owl.AxiomGeneratorFactory;
import org.incenp.obofoundry.sssom.owl.DirectAxiomGenerator;
import org.incenp.obofoundry.sssom.owl.EquivalentAxiomGenerator;
import org.incenp.obofoundry.sssom.owl.OWLGenerator;
import org.incenp.obofoundry.sssom.owl.UniqueLabelGenerator;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingProcessorListener;
import org.incenp.obofoundry.sssom.transform.MappingProcessingRule;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ROBOT command to translate mappings into OWL axioms and inject them into an
 * ontology.
 */
public class SSSOMInjectionCommand implements Command, IMappingProcessorListener<OWLAxiom> {

    private static final Logger logger = LoggerFactory.getLogger(SSSOMInjectionCommand.class);

    private Options options;
    AxiomDispatchTable dispatchTable;

    public SSSOMInjectionCommand() {
        options = CommandLineHelper.getCommonOptions();
        options.addOption("i", "input", true, "load ontology from file");
        options.addOption("o", "output", true, "save ontology to file");
        options.addOption("s", "sssom", true, "load SSSOM mapping set from file");
        options.addOption(null, "sssom-metadata", true, "load mapping set metadata from specified file");
        options.addOption(null, "cross-species", true, "inject cross-species bridging axioms for specified taxon");
        options.addOption(null, "direct", false,
                "inject axioms produced by direct, standard-specified translation of the mappings");
        options.addOption(null, "no-merge", false, "do not merge mapping-derived axioms into the ontology");
        options.addOption(null, "bridge-file", true, "write mapping-derived axioms into the specified file");
        options.addOption(null, "check-subject", false, "ignore mappings whose subject does not exist in the ontology");
        options.addOption(null, "check-object", false, "ignore mappings whose subject does not exist in the ontology");
        options.addOption(null, "ruleset", true, "inject axioms specified in ruleset file");
        options.addOption(null, "include-rule", true, "Only run rules with the specified tag");
        options.addOption(null, "exclude-rule", true, "Do not run rules with the specified tag");
        options.addOption(null, "dispatch-table", true,
                "write generated axioms to several output ontologies according to dispatch table");
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
        OWLGenerator axiomGenerator = new OWLGenerator();

        if ( line.hasOption("check-subject") ) {
            axiomGenerator.setCheckSubjectExistence(ontology);
        }

        if ( line.hasOption("check-object") ) {
            axiomGenerator.setCheckObjectExistence(ontology);
        }

        if ( line.hasOption("cross-species") ) {
            String[] parts = line.getOptionValue("cross-species").split(",", 2);
            IRI taxonIRI = ioHelper.createIRI(parts[0]);
            String taxonName = parts.length > 1 ? parts[1] : null;
            addCrossSpeciesRules(axiomGenerator, ontology, taxonIRI, taxonName);
        }

        if ( line.hasOption("direct") ) {
            axiomGenerator.addRule(null, null, new DirectAxiomGenerator(ontology));
        }

        if ( line.hasOption("dispatch-table") ) {
            dispatchTable = AxiomDispatchTable.readFromFile(line.getOptionValue("dispatch-table"),
                    ontology.getOWLOntologyManager());
            axiomGenerator.addGeneratedListener(this);
        }

        if ( line.hasOption("ruleset") ) {
            SSSOMTransformReader<OWLAxiom> sssomtReader = new SSSOMTransformReader<OWLAxiom>(
                    new AxiomGeneratorFactory(ontology), line.getOptionValue("ruleset"));
            sssomtReader.read();

            if ( sssomtReader.hasErrors() ) {
                sssomtReader.getErrors()
                        .forEach((e) -> logger
                                .error(String.format("Error when parsing SSSOM/T ruleset: %s", e.getMessage())));
            } else {
                axiomGenerator.addRules(sssomtReader.getRules());

            }
        }

        if ( line.hasOption("include-rule") ) {
            axiomGenerator.includeRules(new HashSet<String>(Arrays.asList(line.getOptionValues("include-rule"))));
        } else if ( line.hasOption("exclude-rule") ) {
            axiomGenerator.excludeRules(new HashSet<String>(Arrays.asList(line.getOptionValues("exclude-rule"))));
        }

        Set<OWLAxiom> bridgingAxioms = new HashSet<OWLAxiom>(axiomGenerator.process(mappingSet.getMappings()));

        if ( dispatchTable != null ) {
            dispatchTable.saveAll(ioHelper);
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

    private void addCrossSpeciesRules(OWLGenerator generator, OWLOntology ontology, IRI taxonIRI, String taxonName) {
        IMappingFilter predicateFilter = (mapping) -> mapping.getPredicateId()
                .equals("https://w3id.org/semapv/vocab/crossSpeciesExactMatch");
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

        OWLClassExpression partOfTaxon = factory.getOWLObjectSomeValuesFrom(
                factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050")),
                factory.getOWLClass(taxonIRI));

        generator.addRule(predicateFilter, null, new EquivalentAxiomGenerator(ontology, partOfTaxon));
        if ( taxonName != null ) {
            generator.addRule(predicateFilter, null,
                    new UniqueLabelGenerator(ontology, String.format("%%s (%s)", taxonName)));
        }
    }

    @Override
    public void generated(MappingProcessingRule<OWLAxiom> rule, Mapping mapping, OWLAxiom product) {
        rule.getTags().forEach((t) -> dispatchTable.addAxiom(t, product));
    }
}
