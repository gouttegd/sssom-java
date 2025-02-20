/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2025 Damien Goutte-Gattat
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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SSSOMReader;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.owl.AnnotatedAxiomGenerator;
import org.incenp.obofoundry.sssom.owl.AnnotationAxiomGenerator;
import org.incenp.obofoundry.sssom.owl.EquivalentAxiomGenerator;
import org.incenp.obofoundry.sssom.owl.OWLGenerator;
import org.incenp.obofoundry.sssom.owl.SSSOMTOwlApplication;
import org.incenp.obofoundry.sssom.owl.XrefExtractor;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingProcessorListener;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.MappingProcessingRule;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;
import org.incenp.obofoundry.sssom.util.ReaderFactory;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
        options.addOption("I", "input-iri", true, "load ontology from an IRI");
        options.addOption("o", "output", true, "save ontology to file");
        options.addOption("s", "sssom", true, "load SSSOM mapping set from file");
        options.addOption(null, "sssom-metadata", true, "load mapping set metadata from specified file");
        options.addOption(null, "extract", false, "extract SSSOM mapping set from cross-references");
        options.addOption(null, "cross-species", true, "inject cross-species bridging axioms for specified taxon");
        options.addOption(null, "direct", false,
                "inject axioms produced by direct, standard-specified translation of the mappings");
        options.addOption(null, "hasdbxref", false, "inject oboInOwl:hasDbXref annotations from the mappings");
        options.addOption(null, "no-merge", false, "do not merge mapping-derived axioms into the ontology");
        options.addOption(null, "bridge-file", true, "write mapping-derived axioms into the specified file");
        options.addOption(null, "bridge-iri", true, "specify the ontology IRI for the bridge file");
        options.addOption(null, "bridge-format", true, "force the format of the bridge file");
        options.addOption(null, "create", false, "create a new ontology with the mappings-derived axioms");
        options.addOption(null, "check-subject", false, "ignore mappings whose subject does not exist in the ontology");
        options.addOption(null, "check-object", false, "ignore mappings whose subject does not exist in the ontology");
        options.addOption(null, "ruleset", true, "inject axioms specified in ruleset file");
        options.addOption(null, "no-default-prefixes", false, "do not use prefixes known to ROBOT");
        options.addOption(null, "use-input-prefix-map", false, "add input set(s) prefixes to the SSSOM/T prefix map");
        options.addOption(null, "include-rule", true, "Only run rules with the specified tag");
        options.addOption(null, "exclude-rule", true, "Do not run rules with the specified tag");
        options.addOption(null, "dispatch-table", true,
                "write generated axioms to several output ontologies according to dispatch table");
        options.addOption("r", "reasoner", true, "reasoner to use");
        options.addOption(null, "invert", false, "invert the mapping set prior to any processing");
        options.addOption(null, "only-subject-in", true, "Only process mappings whose subject has the given prefix");
        options.addOption(null, "only-object-in", true, "Only process mappings whose object has the given prefix");
        options.addOption(null, "drop-duplicate-subjects", false, "Drop any mapping with a cardinality of *:n");
        options.addOption(null, "drop-duplicate-objects", false, "Drop any mapping with a cardinality of n:*");
        options.addOption(null, "error-on-unshortenable-iris", false,
                "Error out if some IRIs could not be shortened as needed");
    }

    @Override
    public String getName() {
        return "inject";
    }

    @Override
    public String getDescription() {
        return "inject axioms from a SSSOM mapping set into the ontology";
    }

    @Override
    public String getUsage() {
        return "robot inject -i <INPUT> -s <SSSOM_FILE> [axiom type options] -o <OUTPUT>";
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
        if ( ! line.hasOption("create") ) {
            state = CommandLineHelper.updateInputOntology(ioHelper, state, line);
        } else {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            state.setOntology(ontology);
        }

        OWLOntology ontology = state.getOntology();
        OWLGenerator axiomGenerator = new OWLGenerator();

        PrefixManager pm = new PrefixManager();
        if ( !line.hasOption("no-default-prefixes") ) {
            pm.add(ioHelper.getPrefixes());
        }

        MappingSet mappingSet = null;
        ReaderFactory readerFactory = new ReaderFactory(true);
        if ( line.hasOption("sssom") ) {
            for ( String sssomFile : line.getOptionValues("sssom") ) {
                SSSOMReader reader = readerFactory.getReader(sssomFile, line.getOptionValue("sssom-metadata"));
                if ( mappingSet == null ) {
                    mappingSet = reader.read();
                } else {
                    mappingSet.getMappings().addAll(reader.read().getMappings());
                }
            }
        }
        if ( line.hasOption("extract") ) {
            XrefExtractor extractor = new XrefExtractor();
            extractor.setPrefixMap(pm.getPrefixMap());
            extractor.fillPrefixToPredicateMap(ontology);
            if ( mappingSet == null ) {
                mappingSet = extractor.extract(ontology);
            } else {
                mappingSet.getMappings().addAll(extractor.extract(ontology).getMappings());
            }
        }
        if ( mappingSet == null ) {
            throw new IllegalArgumentException("Missing SSSOM mapping set");
        }

        if ( line.hasOption("use-input-prefix-map") ) {
            pm.add(mappingSet.getCurieMap());
        }

        MappingCardinality.inferCardinality(mappingSet.getMappings());

        if ( line.hasOption("invert") ) {
            axiomGenerator.addRule(null, (mapping) -> mapping.invert(), null);
        }

        if ( line.hasOption("only-subject-in") ) {
            String pr = pm.expandIdentifier(line.getOptionValue("only-subject-in"));
            axiomGenerator.addStopingRule(
                    (mapping) -> mapping.getSubjectId() == null || !mapping.getSubjectId().startsWith(pr));
        }

        if ( line.hasOption("only-object-in") ) {
            String pr = pm.expandIdentifier(line.getOptionValue("only-object-in"));
            axiomGenerator.addStopingRule(
                    (mapping) -> mapping.getObjectId() == null || !mapping.getObjectId().startsWith(pr));
        }

        if ( line.hasOption("check-subject") ) {
            axiomGenerator.setCheckSubjectExistence(ontology);
        }

        if ( line.hasOption("check-object") ) {
            axiomGenerator.setCheckObjectExistence(ontology);
        }

        if ( line.hasOption("drop-duplicate-subjects") ) {
            axiomGenerator
                    .addStopingRule((mapping) -> mapping.getMappingCardinality() == MappingCardinality.MANY_TO_MANY
                            || mapping.getMappingCardinality() == MappingCardinality.ONE_TO_MANY);
        }

        if ( line.hasOption("drop-duplicate-objects") ) {
            axiomGenerator
                    .addStopingRule((mapping) -> mapping.getMappingCardinality() == MappingCardinality.MANY_TO_MANY
                            || mapping.getMappingCardinality() == MappingCardinality.MANY_TO_ONE);
        }

        if ( line.hasOption("cross-species") ) {
            String[] parts = line.getOptionValue("cross-species").split(",", 2);
            IRI taxonIRI = ioHelper.createIRI(parts[0]);
            String taxonName = parts.length > 1 ? parts[1] : null;
            addCrossSpeciesRules(axiomGenerator, ontology, taxonIRI, taxonName);
        }

        if ( line.hasOption("direct") ) {
            axiomGenerator.addRule(null, null, new AnnotatedAxiomGenerator(ontology));
        }

        if (line.hasOption("hasdbxref")) {
            IMappingTransformer<String> texter = (mapping) -> pm.shortenIdentifier(mapping.getObjectId());
            axiomGenerator.addRule(null, null, new AnnotationAxiomGenerator(ontology,
                    IRI.create("http://www.geneontology.org/formats/oboInOwl#hasDbXref"), texter, false));
        }

        if ( line.hasOption("ruleset") ) {
            SSSOMTOwlApplication sssomApplication = new SSSOMTOwlApplication(ontology,
                    CommandLineHelper.getReasonerFactory(line));
            SSSOMTransformReader<OWLAxiom> sssomtReader = new SSSOMTransformReader<OWLAxiom>(sssomApplication,
                    line.getOptionValue("ruleset"));
            sssomtReader.setPrefixManager(pm);
            sssomtReader.read();

            if ( sssomtReader.hasErrors() ) {
                sssomtReader.getErrors()
                        .forEach((e) -> logger
                                .error(String.format("Error when parsing SSSOM/T ruleset: %s", e.getMessage())));
            } else {
                axiomGenerator.addRules(sssomtReader.getRules());
            }

            if ( line.hasOption("dispatch-table") ) {
                dispatchTable = AxiomDispatchTable.readFromFile(line.getOptionValue("dispatch-table"),
                        ontology.getOWLOntologyManager(), sssomApplication.getEntityChecker());
                axiomGenerator.addGeneratedListener(this);
            }
        }

        if ( line.hasOption("include-rule") ) {
            axiomGenerator.includeRules(new HashSet<String>(Arrays.asList(line.getOptionValues("include-rule"))));
        } else if ( line.hasOption("exclude-rule") ) {
            axiomGenerator.excludeRules(new HashSet<String>(Arrays.asList(line.getOptionValues("exclude-rule"))));
        }

        Set<OWLAxiom> bridgingAxioms = new HashSet<OWLAxiom>(axiomGenerator.process(mappingSet.getMappings()));

        boolean errorOnUnshortenableIRIs = line.hasOption("error-on-unshortenable-iris");
        if ( !pm.getUnshortenedIRIs().isEmpty() ) {
            for ( String unshortenedIRI : pm.getUnshortenedIRIs() ) {
                String msg = String.format("IRI could not be shortened: %s", unshortenedIRI);
                if ( errorOnUnshortenableIRIs ) {
                    logger.error(msg);
                } else {
                    logger.warn(msg);
                }
            }
            if ( errorOnUnshortenableIRIs ) {
                throw new IllegalStateException(String.format("Some IRIs could not be shortened (%d in total)",
                        pm.getUnshortenedIRIs().size()));
            }
        }

        if ( dispatchTable != null ) {
            dispatchTable.saveAll(ioHelper);
        }

        if ( !line.hasOption("no-merge") && !bridgingAxioms.isEmpty() ) {
            ontology.getOWLOntologyManager().addAxioms(ontology, bridgingAxioms);
        }

        if ( line.hasOption("bridge-file") && !bridgingAxioms.isEmpty() ) {
            OWLOntologyManager mgr = ontology.getOWLOntologyManager();
            OWLOntology bridgeOntology = null;
            if ( line.hasOption("bridge-iri") ) {
                IRI bridgeIRI = IRI.create(line.getOptionValue("bridge-iri"));
                if ( mgr.contains(bridgeIRI) ) {
                    bridgeOntology = mgr.getOntology(bridgeIRI);
                    mgr.removeAxioms(bridgeOntology, bridgeOntology.getAxioms());
                } else {
                    bridgeOntology = mgr.createOntology(bridgeIRI);
                }
            } else {
                bridgeOntology = mgr.createOntology();
            }
            mgr.addAxioms(bridgeOntology, bridgingAxioms);
            if ( line.hasOption("bridge-format") ) {
                ioHelper.saveOntology(bridgeOntology, IOHelper.getFormat(line.getOptionValue("bridge-format")),
                        new File(line.getOptionValue("bridge-file")));
            } else {
                ioHelper.saveOntology(bridgeOntology, line.getOptionValue("bridge-file"));
            }
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
                    new AnnotationAxiomGenerator(ontology, IRI.create("http://purl.obolibrary.org/obo/IAO_0000589"),
                            (mapping) -> String.format("%s (%s)", mapping.getSubjectLabel(), taxonName)));
        }
    }

    @Override
    public void generated(MappingProcessingRule<OWLAxiom> rule, Mapping mapping, OWLAxiom product) {
        rule.getTags().forEach((t) -> dispatchTable.addAxiom(t, product));
    }
}
