/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024,2025 Damien Goutte-Gattat
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.catalog.CatalogException;

import org.incenp.obofoundry.sssom.Cardinalizer;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.JSONWriter;
import org.incenp.obofoundry.sssom.MergeOption;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.SSSOMReader;
import org.incenp.obofoundry.sssom.SSSOMUtils;
import org.incenp.obofoundry.sssom.SSSOMWriter;
import org.incenp.obofoundry.sssom.SetMerger;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.TSVWriter;
import org.incenp.obofoundry.sssom.ValidationLevel;
import org.incenp.obofoundry.sssom.extract.ExtractorSyntaxException;
import org.incenp.obofoundry.sssom.extract.IValueExtractor;
import org.incenp.obofoundry.sssom.extract.ValueExtractorFactory;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.owl.OWLHelper;
import org.incenp.obofoundry.sssom.owl.OWLHelper.UpdateMode;
import org.incenp.obofoundry.sssom.rdf.RDFWriter;
import org.incenp.obofoundry.sssom.transform.MappingProcessingRule;
import org.incenp.obofoundry.sssom.transform.MappingProcessor;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplication;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;
import org.incenp.obofoundry.sssom.util.ExtendedPrefixMap;
import org.incenp.obofoundry.sssom.util.ExtensionSlotHelper;
import org.incenp.obofoundry.sssom.util.ReaderFactory;
import org.incenp.obofoundry.sssom.util.SerialisationFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

/**
 * A command-line interface to manipulate mapping sets.
 */
@Command(name = "sssom-cli",
        mixinStandardHelpOptions = true,
        versionProvider = CommandHelper.class,
        description = "Read, manipulate, and write SSSOM mapping sets.",
        footer = "Report bugs to <dgouttegattat@incenp.org>.",
        optionListHeading = "%nGeneral options:%n",
        footerHeading = "%n")
public class SimpleCLI implements Runnable {

    /*
     * Command line options
     */

    @Spec
    private static CommandSpec spec;

    enum EPMMode {
        PRE,
        POST,
        BOTH
    }

    @ArgGroup(validate = false, heading = "%nInput options:%n")
    private InputOptions inputOpts = new InputOptions();

    private static class InputOptions {
        private ArrayList<String> files = new ArrayList<String>();
        private SetMerger merger = new SetMerger();

        @Parameters(index = "0..*",
                paramLabel = "SET[:META]",
                description = "Load a mapping set from the specified file(s). Default is to read from standard input.")
        private void addInputFile(String[] args) {
            files.add(args[args.length - 1]);
        }

        // Keep accepting --input options for backwards compatibility
        @Option(names = { "-i", "--input" }, hidden = true)
        private void addInputFromFromOption(String[] args) {
            files.add(args[args.length - 1]);
        }

        @Option(names = "--epm",
                paramLabel = "EPM",
                description = "Use the specified extended prefix map (EPM).")
        String epmFile;

        @Option(names = "--mangle-iris", hidden = true)
        private void mangleIRIs(String arg) {
            epmFile = arg;
            epmMode = EPMMode.POST;
        }

        @Option(names = "--epm-mode",
                paramLabel = "MODE",
                description = "How to use the Extended Prefix Map set with --epm. Allowed values: ${COMPLETION-CANDIDATES}. Default is BOTH.")
        EPMMode epmMode = EPMMode.BOTH;

        @Option(names = "--no-metadata-merge",
                description = "Do not attempt to merge the set-level metadata of the input sets.")
        private void noMetadataMerge(boolean value) {
            merger.getMergeOptions().remove(MergeOption.MERGE_LISTS);
            merger.getMergeOptions().remove(MergeOption.MERGE_EXTENSIONS);
        }

        public SetMerger getMerger() {
            return merger;
        }

        @Option(names = "--accept-extra-metadata",
                paramLabel = "POLICY",
                description = "Whether to accept non-standard metadata in the input set(s). Allowed values: ${COMPLETION-CANDIDATES}. Default is NONE.")
        ExtraMetadataPolicy acceptExtraMetadata = ExtraMetadataPolicy.NONE;

        @Option(names = "--extensions-from-other",
                description = "Converts 'other' slot values into extension slots.")
        boolean extensionsFromOther;

        @Option(names = "--propagation", negatable = true,
                defaultValue = "true", fallbackValue = "true",
                description = "Enable/disable propagation of propagatable slots. This is enabled by default.")
        boolean enablePropagation;

        @Option(names = "--assume-version",
                paramLabel="VERSION",
                converter=VersionConverter.class,
                description = "Default SSSOM version the input set(s) should be assumed to be compliant with, in the absence of an explicit sssom_version slot. Default is 1.0.")
        Version assumedVersion = Version.SSSOM_1_0;

        @Option(names = "--lax",
                description = "Silently accept some invalid mapping sets.")
        boolean laxMode;

        @Option(names = "--input-format", paramLabel = "FMT",
                description = "Expect input in the specified format. Allowed values: ${COMPLETION-CANDIDATES}. Default is inferred whenever possible.",
                converter = SerialisationFormatConverter.class,
                completionCandidates = SerialisationFormatCompletionCandidates.class)
        SerialisationFormat inputFormat = null;
    }

    @ArgGroup(validate = false, heading = "%nOutput options:%n")
    private OutputOptions outputOpts = new OutputOptions();

    private static class OutputOptions {
        @Option(names = { "-o", "--output" },
                paramLabel = "FILE",
                description = "Write the mapping set to FILE. Default is to write to standard output.",
                defaultValue = "-")
        String file;

        @Option(names = { "-O", "--metadata-output" },
                paramLabel = "FILE",
                description = "Write the metadata block to separate FILE.")
        String metaFile;

        @Option(names = "--split",
                paramLabel = "DIRECTORY",
                description = "Split the set along subject and object prefix names and write the split sets in the specified directory.")
        String splitDirectory;

        @Option(names = "--split-with-predicates",
                description = "When splitting, include the predicate CURIE in the split identifier.")
        boolean splitWithPredicates;


        @Option(names = { "-c", "--force-cardinality" },
                hidden = true,
                description = "Include mapping cardinality values.")
        private void forceCardinality(boolean value) {
            cardinality = CardinalityOption.FORCE;
        }

        @Option(names = {"-C", "--cardinality" },
                paramLabel = "OPT",
                description = "What to do with mapping cardinality values. Possible options: ${COMPLETION-CANDIDATES}. Default is REMOVE.")
        CardinalityOption cardinality = CardinalityOption.REMOVE;

        @Option(names = "--output-prefix-map",
                paramLabel = "SRC",
                description = "Specify the source of the output prefix map. Possible values: ${COMPLETION-CANDIDATES}. Default is BOTH.")
        OutputMapSource prefixMapSource = OutputMapSource.BOTH;

        @Option(names = { "-m", "--output-metadata" },
                paramLabel = "META",
                description = "Use metadata from specified file.")
        String extraMetadataFile;

        @Option(names = "--write-extra-metadata",
                paramLabel = "POLICY",
                description = "How to write non-standard metadata in the output set. Allowed values: ${COMPLETION-CANDIDATES}. Default is DEFINED, except for RDF output where it is UNDEFINED.")
        ExtraMetadataPolicy writeExtraMetadata = null;

        ExtraMetadataPolicy defaultWriteExtraMetadata = ExtraMetadataPolicy.DEFINED;

        ExtraMetadataPolicy getExtraMetadataPolicy() {
            return writeExtraMetadata != null ? writeExtraMetadata : defaultWriteExtraMetadata;
        }

        @Option(names = "--extensions-to-other",
                description = "Encode extension slots into 'other' slots.")
        boolean extensionsToOther;

        @Option(names = "--condensation", negatable = true,
                defaultValue = "true", fallbackValue = "true",
                description = "Enable/disable condensation of propagatable slots. This is enabled by default, except for RDF output.")
        boolean enableCondensation;

        boolean defaultEnableCondensation = true;

        boolean isCondensationEnabled() {
            ParseResult pr = spec.commandLine().getParseResult();
            return pr.hasMatchedOption("condensation") ? enableCondensation : defaultEnableCondensation;
        }

        @Option(names = { "-f", "--output-format" },
                paramLabel = "FMT",
                description = "Write output in the specified format. Allowed values: ${COMPLETION-CANDIDATES}. Default is tsv.",
                converter = SerialisationFormatConverter.class,
                completionCandidates = SerialisationFormatCompletionCandidates.class)
        SerialisationFormat outputFormat = null;

        @Option(names = { "-j", "--json-output" }, hidden = true)
        private void useJSON(boolean value) {
            outputFormat = SerialisationFormat.JSON;
        }

        @Option(names = { "--json-short-iris" }, description = "Shorten IRIs when writing in JSON format.")
        boolean jsonShortenIRIs;

        @Option(names = { "--json-write-ld-context" },
                description = "Store the CURIE map in a JSON-LD @context key when writing in JSON format.")
        boolean jsonWriteContext;

        @Option(names = { "--json-sssompy" },
                description = "Write the mapping set in the JSON-LD format expected by SSSOM-Py.")
        private void enableSSSOMPyJSON(boolean arg) {
            outputFormat = SerialisationFormat.JSON;
            jsonShortenIRIs = arg;
            jsonWriteContext = arg;
        }

        @Option(names = { "--sssompy-json" }, hidden = true)
        private void oldEnableSSSOMPyJSON(boolean arg) {
            enableSSSOMPyJSON(arg);
        }

        @Option(names = {"--rdf-direct-triples" },
                description = "Inject direct triples when writing in RDF/TTL format.")
        boolean rdfDirectTriples;

        @Option(names = {"--sorting" }, negatable = true, defaultValue = "true", fallbackValue = "true",
                description = "Enable/disable sorting of mappings. This is enabled by default.")
        boolean sortMappings;

        IValueExtractor extractor;

        @Option(names = { "--extract" }, paramLabel = "EXPRESSION",
                description = "Extract a single value from the result set and print it on standard output.")
        private void setExtractor(String arg) {
            try {
                extractor = new ValueExtractorFactory().parse(arg);
            } catch ( ExtractorSyntaxException e ) {
                throw new ParameterException(spec.commandLine(),
                        String.format("Invalid --extract expression: %s", e.getMessage()));
            }
        }

        @Option(names = { "--no-stdout" },
                description = "Do not write the result set on standard output.")
        boolean noStandardOutput;

        @Option(names = "--force-version",
                paramLabel = "VERSION",
                converter = VersionConverter.class,
                description = "The version of the SSSOM specification the result set must be compliant with. Default is the latest supported version.")
        Version forceVersion = Version.LATEST;
    }

    enum OutputMapSource {
        INPUT,
        SSSOMT,
        BOTH;
    }

    @ArgGroup(validate = false, heading = "%nSSSOM/transform options:%n")
    private TransformOptions transOpts = new TransformOptions();

    private static class TransformOptions {
        private ArrayList<String> rules = new ArrayList<String>();

        @Option(names = { "-r", "--ruleset" }, paramLabel = "RULESET", description = "Apply a SSSOM/T ruleset.")
        String rulesetFile;

        @Option(names = { "-R", "--rule" }, paramLabel = "RULE", description = "Apply a single SSSOM/T rule.")
        private void addRule(String[] args) {
            // Picocli provides us with the accumulated list of values for all occurences of
            // the option, we only want the last one each time.
            rules.add(args[args.length - 1]);
        }

        @Option(names = { "-I", "--include" }, paramLabel = "FILTER", description = "Apply a SSSOM/T inclusion filter.")
        private void addIncludeRule(String[] args) {
            rules.add(args[args.length - 1] + " -> include()");
        }

        @Option(names = { "-E", "--exclude" }, paramLabel = "FILTER", description = "Apply a SSSOM/T exclusion filter.")
        private void addExcludeRule(String[] args) {
            rules.add(args[args.length - 1] + " -> stop()");
        }

        @Option(names = { "-A", "--blanket-rule" }, paramLabel = "ACTION",
                description = "Apply a rule to all mappings.")
        private void addGeneralRule(String[] args) {
            rules.add("predicate==* -> " + args[args.length - 1]);
        }

        @Option(names = "--prefix", paramLabel = "NAME=PREFIX", description = "Declare a prefix for use in SSSOM/T.")
        Map<String, String> prefixMap = new HashMap<String, String>();

        @Option(names = "--prefix-map",
                paramLabel = "METAFILE",
                description = "Use the prefix map from specified metadata file for SSSOM/T.")
        String externalPrefixMap;

        @Option(names = { "-p", "--prefix-map-from-input" },
                description = "Use the prefix map of the input set(s) for SSSOM/T.")
        boolean useInputPrefixMap;

        @Option(names = { "-a", "--include-all" },
                description = "Add a default include rule at the end of the processing set.")
        boolean includeAll;
    }

    @ArgGroup(validate = false, heading = "%nOntology-related options:%n")
    private OntologyOptions ontOptions = new OntologyOptions();

    private static class OntologyOptions {
        @Option(names = "--update-from-ontology",
                paramLabel = "ONTOLOGY[:subject,object,label,source,existence]",
                description = "Update the set using data from the specified ontology.")
        String[] ontologiesForUpdate;

        @Option(names = "--catalog",
                paramLabel = "CATALOG",
                description = "Use the specified catalog to resolve imports when reading the ontology.")
        String xmlCatalog;

        @Option(names = "--ignore-missing-imports",
                description = "Silently ignore missing imports when reading the ontology.")
        boolean ignoreMissingImports = false;

        final String OWLAPI_MISSING_IMPORT_PROPERTY = "org.semanticweb.owlapi.model.parameters.ConfigurationOptions.MISSING_IMPORT_HANDLING_STRATEGY";
    }

    private static class SerialisationFormatConverter implements ITypeConverter<SerialisationFormat> {

        @Override
        public SerialisationFormat convert(String value) throws Exception {
            return SerialisationFormat.fromName(value);
        }
    }

    private static class SerialisationFormatCompletionCandidates extends ArrayList<String> {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        SerialisationFormatCompletionCandidates() {
            super(SerialisationFormat.getShortNames());
        }
    }

    private CommandHelper helper = new CommandHelper();

    public static void main(String[] args) {
        System.exit(run(args));
    }

    /*
     * The real entry point. It is separate from the main method so that it can be
     * called from the test suite.
     */
    public static int run(String[] args) {
        SimpleCLI cli = new SimpleCLI();
        int rc = new picocli.CommandLine(cli).setExecutionExceptionHandler(cli.helper)
                .setCaseInsensitiveEnumValuesAllowed(true).setUsageHelpLongOptionsMaxWidth(23)
                .setUsageHelpAutoWidth(true).execute(args);
        return rc;
    }

    @Override
    public void run() {
        MappingSet ms = loadInputs();
        transform(ms);
        postProcess(ms);
        extract(ms);
        writeOutput(ms);
    }

    /*
     * Input
     */

    private MappingSet loadInputs() {
        MappingSet ms = null;
        if ( inputOpts.files.isEmpty() ) {
            inputOpts.files.add("-");
        }
        ReaderFactory readerFactory = new ReaderFactory(true);
        ExtendedPrefixMap epm = readExtendedPrefixMap(inputOpts.epmFile);
        for ( String input : inputOpts.files ) {
            String[] items = input.split(":", 2);
            String tsvFile = items[0];
            String metaFile = items.length == 2 ? items[1] : null;
            try {
                SSSOMReader reader = readerFactory.getReader(tsvFile, metaFile, true, inputOpts.inputFormat);
                reader.setExtraMetadataPolicy(inputOpts.acceptExtraMetadata);
                reader.setPropagationEnabled(inputOpts.enablePropagation);
                reader.setAssumedVersion(inputOpts.assumedVersion);
                reader.setValidation(inputOpts.laxMode ? ValidationLevel.MINIMAL : ValidationLevel.FULL);
                if ( epm != null && inputOpts.epmMode != EPMMode.POST ) {
                    reader.fillPrefixMap(epm.getFullPrefixMap());
                }
                if ( ms == null ) {
                    ms = reader.read();
                } else {
                    inputOpts.getMerger().merge(ms, reader.read());
                }
            } catch ( IOException ioe ) {
                helper.error("Cannot read file %s: %s", input, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                helper.error("Invalid SSSOM data in file %s: %s", input, sfe.getMessage());
            }
        }

        if ( epm != null && inputOpts.epmMode != EPMMode.PRE ) {
            epm.canonicalise(ms);
        }

        if ( outputOpts.extraMetadataFile != null ) {
            try {
                // Read the metadata as a new mapping set and exchange it with the real combined
                // input set.
                TSVReader reader = new TSVReader(null, outputOpts.extraMetadataFile);
                MappingSet tmpSet = reader.read(true);
                inputOpts.getMerger().merge(tmpSet, ms);
                ms = tmpSet;
            } catch ( IOException ioe ) {
                helper.error("Cannot read file %s: %s", outputOpts.extraMetadataFile, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                helper.error("Invalid SSSOM data in file %s: %s", outputOpts.extraMetadataFile, sfe.getMessage());
            }
        }

        if ( inputOpts.extensionsFromOther ) {
            ExtensionSlotHelper.fromOther(ms, true);
        }

        return ms;
    }

    private ExtendedPrefixMap readExtendedPrefixMap(String epmFile) {
        ExtendedPrefixMap epm = null;
        if ( epmFile != null ) {
            try {
                if ( epmFile.equals("obo") ) {
                    epm = new ExtendedPrefixMap(ExtendedPrefixMap.class.getResourceAsStream("/obo.epm.json"));
                } else {
                    epm = new ExtendedPrefixMap(epmFile);
                }
            } catch ( IOException ioe ) {
                helper.error("Cannot read extended prefix map: %s", ioe.getMessage());
            }
        }
        return epm;
    }

    /*
     * Output
     */

    private File getCatalogFile(String userSpecified, String defaultFile) {
        File catalog = null;

        if ( userSpecified != null ) {
            if ( !userSpecified.equals("none") ) {
                catalog = new File(userSpecified);
                if ( !catalog.exists() ) {
                    helper.error("Specified catalog %s not found", userSpecified);
                }
            }
        } else {
            catalog = new File(defaultFile);
            if ( !catalog.exists() ) {
                catalog = null;
            }
        }

        return catalog;
    }

    private void postProcess(MappingSet ms) {
        if ( outputOpts.cardinality == CardinalityOption.FORCE ) {
            new Cardinalizer().fillCardinality(ms.getMappings());
        } else if ( outputOpts.cardinality == CardinalityOption.REMOVE ) {
            for ( Mapping mapping : ms.getMappings() ) {
                mapping.setMappingCardinality(null);
                mapping.setCardinalityScope(null);
            }
        }

        if ( ontOptions.ontologiesForUpdate != null ) {
            OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();

            File catalog = getCatalogFile(ontOptions.xmlCatalog, "catalog-v001.xml");
            if ( catalog != null ) {
                try {
                    mgr.getIRIMappers().add(new XMLCatalogIRIMapper(catalog));
                } catch ( CatalogException | IllegalArgumentException e ) {
                    helper.error("Cannot parse catalog: %s", e.getMessage());
                }
            }

            if ( ontOptions.ignoreMissingImports ) {
                /*
                 * FIXME: I don't understand why the following does not work:
                 * 
                 * mgr.setOntologyLoaderConfiguration(mgr.getOntologyLoaderConfiguration()
                 * .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));
                 */
                System.setProperty(ontOptions.OWLAPI_MISSING_IMPORT_PROPERTY, "SILENT");
            }

            for ( String ontFile : ontOptions.ontologiesForUpdate ) {
                EnumSet<UpdateMode> mode = EnumSet.of(UpdateMode.UPDATE_LABEL, UpdateMode.UPDATE_SOURCE);

                String[] parts = ontFile.split(":", 2);
                if ( parts.length == 2 ) {
                    boolean replace = false;
                    EnumSet<UpdateMode> setMode = EnumSet.noneOf(UpdateMode.class);
                    for ( String flag : parts[1].split(",") ) {
                        switch ( flag ) {
                        case "subject":
                            setMode.add(UpdateMode.ONLY_SUBJECT);
                            break;

                        case "object":
                            setMode.add(UpdateMode.ONLY_OBJECT);
                            break;

                        case "label":
                            replace = true;
                            setMode.add(UpdateMode.UPDATE_LABEL);
                            break;

                        case "source":
                            replace = true;
                            setMode.add(UpdateMode.UPDATE_SOURCE);
                            break;

                        case "existence":
                            replace = true;
                            setMode.add(UpdateMode.DELETE_MISSING);
                            setMode.add(UpdateMode.DELETE_OBSOLETE);
                            break;
                        }
                    }

                    if ( setMode.contains(UpdateMode.ONLY_SUBJECT) && setMode.contains(UpdateMode.ONLY_OBJECT) ) {
                        // Accept "subject,object" as meaning that we want to check both sides
                        setMode.remove(UpdateMode.ONLY_SUBJECT);
                        setMode.remove(UpdateMode.ONLY_OBJECT);
                    }

                    if ( replace ) {
                        mode = setMode;
                    } else {
                        mode.addAll(setMode);
                    }
                }

                try {
                    OWLOntology ont = mgr.loadOntologyFromOntologyDocument(new File(parts[0]));
                    OWLHelper.updateMappingSet(ms, ont, null, false, mode);
                } catch ( OWLOntologyCreationException e ) {
                    helper.error("cannot read ontology %s: %s", ontFile, e.getMessage());
                }
            }
        }
    }

    private void extract(MappingSet set) {
        if ( outputOpts.extractor != null ) {
            Object value = outputOpts.extractor.extract(set);
            if ( value != null ) {
                System.out.printf("%s\n", SSSOMUtils.format(value));
            }
        }
    }

    private void writeOutput(MappingSet set) {
        if ( outputOpts.prefixMapSource == OutputMapSource.SSSOMT ) {
            // Replace the input map with the SSSOM/T map
            set.setCurieMap(transOpts.prefixMap);
        } else if ( outputOpts.prefixMapSource == OutputMapSource.BOTH ) {
            // Add the SSSOM/T map to the input map (the SSSOM/T map takes precedence)
            set.getCurieMap().putAll(transOpts.prefixMap);
        }

        if ( outputOpts.extensionsToOther ) {
            ExtensionSlotHelper.toOther(set, false);
        }

        if ( outputOpts.splitDirectory != null ) {
            writeSplitSet(set, outputOpts.splitDirectory, outputOpts.splitWithPredicates);
            return; // Skip writing the full set when writing splits
        }
        boolean stdout = outputOpts.file.equals("-");
        if ( stdout && outputOpts.noStandardOutput ) {
            return;
        }
        try {
            getWriter(outputOpts.file, outputOpts.metaFile, outputOpts.outputFormat).write(set);
        } catch ( IOException ioe ) {
            helper.error("cannot write to file %s: %s", stdout ? "-" : outputOpts.file, ioe.getMessage());
        }
    }

    private void writeSplitSet(MappingSet ms, String directory, boolean splitWithPredicates) {
        File dir = new File(directory);
        if ( !dir.isDirectory() && !dir.mkdirs() ) {
            helper.error("cannot create directory %s", directory);
        }

        HashMap<String, List<Mapping>> mappingsBySplit = new HashMap<String, List<Mapping>>();
        PrefixManager pm = new PrefixManager();
        pm.add(ms.getCurieMap());

        for ( Mapping mapping : ms.getMappings() ) {
            if ( mapping.isLiteral() ) {
                continue;
            }
            String subjectPrefixName = pm.getPrefixName(mapping.getSubjectId());
            String objectPrefixName = pm.getPrefixName(mapping.getObjectId());
            if ( subjectPrefixName != null && objectPrefixName != null ) {
                String splitId;
                if ( splitWithPredicates ) {
                    String predicatePrefixName = pm.getPrefixName(mapping.getPredicateId());
                    if ( predicatePrefixName != null ) {
                        splitId = subjectPrefixName + "-" + pm.shortenIdentifier(mapping.getPredicateId()) + "-" + objectPrefixName;
                        splitId = splitId.replace(":", "_");
                    } else {
                        splitId = subjectPrefixName + "-to-" + objectPrefixName;
                    }
                } else {
                    splitId = subjectPrefixName + "-to-" + objectPrefixName;
                }

                mappingsBySplit.computeIfAbsent(splitId, k -> new ArrayList<Mapping>()).add(mapping);
            }
        }

        for ( String splitId : mappingsBySplit.keySet() ) {
            MappingSet splitSet = ms.toBuilder().mappings(null).build();
            splitSet.setMappings(mappingsBySplit.get(splitId));

            SerialisationFormat fmt = outputOpts.outputFormat != null ? outputOpts.outputFormat
                    : SerialisationFormat.TSV;
            File output = new File(dir, splitId + fmt.getExtension());
            try {
                getWriter(output.getPath(), null, fmt).write(splitSet);
            } catch ( IOException ioe ) {
                helper.error("cannot write to file %s: %s", output.getName(), ioe.getMessage());
            }
        }
    }

    private SSSOMWriter getWriter(String filename, String metaFilename, SerialisationFormat fmt) throws IOException {
        boolean stdout = filename.equals("-");
        if ( fmt == null ) {
            fmt = getOutputFormat(filename);
        }
        SSSOMWriter writer;
        switch ( fmt ) {
        case JSON:
            if ( stdout ) {
                writer = new JSONWriter(System.out);
            } else {
                writer = new JSONWriter(filename);
            }
            ((JSONWriter) writer).setShortenIRIs(outputOpts.jsonShortenIRIs);
            ((JSONWriter) writer).setWriteCurieMapInContext(outputOpts.jsonWriteContext);
            break;

        case RDF_TURTLE:
            if ( stdout ) {
                writer = new RDFWriter(System.out);
            } else {
                writer = new RDFWriter(filename);
            }
            outputOpts.defaultWriteExtraMetadata = ExtraMetadataPolicy.UNDEFINED;
            outputOpts.defaultEnableCondensation = false;
            ((RDFWriter) writer).setInjectDirectTriples(outputOpts.rdfDirectTriples);
            break;

        case TSV:
        case CSV:
        default:
            if ( stdout ) {
                FileOutputStream metaStream = null;
                if ( metaFilename != null ) {
                    metaStream = new FileOutputStream(metaFilename);
                }
                writer = new TSVWriter(System.out, metaStream);
            } else {
                writer = new TSVWriter(filename, metaFilename);
            }
            ((TSVWriter) writer).enableCSV(fmt == SerialisationFormat.CSV);
            break;
        }

        writer.setExtraMetadataPolicy(outputOpts.getExtraMetadataPolicy());
        writer.setCondensationEnabled(outputOpts.isCondensationEnabled());
        writer.setSortingEnabled(outputOpts.sortMappings);
        writer.setTargetVersion(outputOpts.forceVersion);
        return writer;
    }

    private SerialisationFormat getOutputFormat(String filename) {
        for ( SerialisationFormat fmt : SerialisationFormat.values() ) {
            if ( filename.endsWith(fmt.getExtension()) ) {
                return fmt;
            }
        }
        return SerialisationFormat.TSV;
    }

    /*
     * Transformations
     */

    private void loadTransformRules(MappingProcessor<Mapping> processor) {
        SSSOMTransformReader<Mapping> reader = null;
        SSSOMTransformApplication<Mapping> application = new SSSOMTransformApplication<Mapping>();
        application.registerGenerator(new SSSOMTIncludeFunction());

        if ( transOpts.rulesetFile != null ) {
            try {
                reader = new SSSOMTransformReader<Mapping>(application, transOpts.rulesetFile);
                reader.addPrefixMap(transOpts.prefixMap);
                reader.read();
            } catch (IOException ioe) {
                helper.error("Cannot read ruleset from file %s: %s", transOpts.rulesetFile, ioe.getMessage());
            }
        }

        if ( !transOpts.rules.isEmpty() ) {
            if ( reader == null ) {
                reader = new SSSOMTransformReader<Mapping>(application);
                reader.addPrefixMap(transOpts.prefixMap);
            }
            for ( String rule : transOpts.rules ) {
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
            transOpts.prefixMap.putAll(reader.getPrefixMap());
        }
    }

    private void setTransformPrefixMap(MappingSet ms) {
        HashMap<String, String> map = new HashMap<String, String>();
        if ( transOpts.useInputPrefixMap ) {
            map.putAll(ms.getCurieMap());
        }
        if ( transOpts.externalPrefixMap != null ) {
            try {
                TSVReader reader = new TSVReader(null, transOpts.externalPrefixMap);
                reader.setValidationEnabled(false);
                map.putAll(reader.read(true).getCurieMap());
            } catch ( IOException ioe ) {
                helper.error("Cannot read file %s: %s", transOpts.externalPrefixMap, ioe.getMessage());
            } catch ( SSSOMFormatException sfe ) {
                helper.error("Invalid SSSOM data in file %s: %s", transOpts.externalPrefixMap, sfe.getMessage());
            }
        }

        // Prefixes declared on the command line always take precedence over the input
        // maps and the external prefix map
        map.forEach((k, v) -> transOpts.prefixMap.putIfAbsent(k, v));
    }

    private void transform(MappingSet ms) {
        MappingProcessor<Mapping> processor = new MappingProcessor<Mapping>();

        setTransformPrefixMap(ms);
        loadTransformRules(processor);

        if ( processor.hasRules() ) {
            if ( transOpts.includeAll ) {
                processor.addRule(new MappingProcessingRule<Mapping>(null, null, (mapping) -> mapping));
            }
            ms.setMappings(processor.process(ms.getMappings()));
        }
    }
}
