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

package org.incenp.obofoundry.sssom.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformBaseVisitor;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformLexer;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformParser;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformParser.ArgumentContext;

/**
 * A parser to read mapping processing rules in the SSSOM Transform language.
 * <p>
 * This parser knows nothing of the functions that are allowed to be used in
 * SSSOM/T rules and what they are supposed. Client code must extend this class
 * and provide at a minimum an implementation of the
 * {@link #parseGeneratingAction(String, List)} method to recognise functions.
 * 
 * @see <a href="https://incenp.org/dvlpt/sssom-java/sssom-transform.html">The
 *      SSSOM/Transform language</a>
 *
 * @param <T> The type of object that should be produced by the processing rules
 *            for each mapping.
 */
public abstract class SSSOMTransformReaderBase<T> {
    private SSSOMTransformLexer lexer;
    protected List<MappingProcessingRule<T>> rules = new ArrayList<MappingProcessingRule<T>>();
    protected List<SSSOMTransformError> errors = new ArrayList<SSSOMTransformError>();
    protected PrefixManager prefixManager = new PrefixManager();
    private boolean hasRead = false;

    /**
     * Creates a new instance without an input source. Use this constructor to parse
     * SSSOM Transform from something else than a file or file-like source, coupled
     * with the {@link #read(String)} method.
     */
    protected SSSOMTransformReaderBase() {
    }

    /**
     * Creates a new instance to read from a reader object.
     * 
     * @param input The reader to parse the SSSOM/T ruleset from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     reader object.
     */
    protected SSSOMTransformReaderBase(Reader input) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromReader(input));
    }

    /**
     * Creates a new instance to read from a stream.
     * 
     * @param input The stream to parse the SSSOM/T ruleset from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     stream.
     */
    protected SSSOMTransformReaderBase(InputStream input) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromStream(input));
    }

    /**
     * Creates a new instance to read from a file.
     * 
     * @param input The file to parse the SSSOM/T ruleset from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     file.
     */
    protected SSSOMTransformReaderBase(File input) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromFileName(input.getPath()));
    }

    /**
     * Creates a new instance to read from a file.
     * 
     * @param filename The name of the file to read from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     file.
     */
    protected SSSOMTransformReaderBase(String filename) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromFileName(filename));
    }

    /**
     * Adds a prefix to the reader's prefix map. The prefix map is used to expand
     * short identifiers ("CURIEs") that may be found in the SSSOM/T ruleset.
     * 
     * @param prefixName The prefix name to add.
     * @param prefix     The corresponding URL prefix.
     */
    public void addPrefix(String prefixName, String prefix) {
        prefixManager.add(prefixName, prefix);
    }

    /**
     * Adds prefixes to the reader's prefix map.
     * 
     * @param map A map between prefix names and their corresponding URL prefixes.
     */
    public void addPrefixMap(Map<String, String> map) {
        prefixManager.add(map);
    }

    /**
     * Parses the SSSOM/T ruleset from the underlying source. After this methods
     * returns {@code true}, call the {@link #getRules()} method to get the result.
     * <p>
     * This method may only be used if an input source has been specified to the
     * constructor.
     * 
     * @return {@code true} if the ruleset was successfully parsed, or {@code false}
     *         if SSSOM/T syntax errors were found.
     * @exception IllegalArgumentException If the method is called while no input
     *                                     source has been set.
     */
    public boolean read() {
        if ( lexer == null ) {
            throw new IllegalArgumentException("Missing input");
        }

        return doParse(lexer);
    }

    /**
     * Parses the SSSOM/T ruleset from the specified string. After this method
     * returns {@code true}, call the {@link #getRules()} method to get the result.
     * <p>
     * This method does not require that an input has been set and may be called
     * repeatedly on different inputs in the lifetime of the SSSOMTransformReader
     * object.
     * 
     * @param text The SSSOM/T ruleset to parse.
     * @return {@code true} if the ruleset was successfully parsed, or {@code false}
     *         of SSSOM/T syntax errors were found.
     */
    public boolean read(String text) {
        SSSOMTransformLexer lexer = new SSSOMTransformLexer(CharStreams.fromString(text));
        return doParse(lexer);
    }

    /*
     * Helper method to do the actual parsing from the provided source.
     */
    private boolean doParse(SSSOMTransformLexer lexer) {
        if ( !errors.isEmpty() ) {
            errors.clear();
        }

        ErrorListener errorListener = new ErrorListener(errors);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SSSOMTransformParser parser = new SSSOMTransformParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.ruleSet();
        if ( !hasErrors() ) {
            List<SSSOMTransformRule> parsedRules = new ArrayList<SSSOMTransformRule>();
            ParseTree2RuleVisitor visitor = new ParseTree2RuleVisitor(parsedRules, prefixManager);
            visitor.visit(tree);

            for ( SSSOMTransformRule parsedRule : parsedRules ) {
                try {
                    IMappingFilter filter = parsedRule.filter;
                    IMappingTransformer<Mapping> preprocessor = null;
                    IMappingTransformer<T> generator = null;

                    preprocessor = parsePreprocessingAction(parsedRule.instruction, parsedRule.arguments);
                    if ( preprocessor == null ) {
                        generator = parseGeneratingAction(parsedRule.instruction, parsedRule.arguments);
                    }

                    MappingProcessingRule<T> rule = new MappingProcessingRule<T>(filter, preprocessor, generator);
                    rule.getTags().addAll(parsedRule.tags);
                    rules.add(rule);
                } catch ( SSSOMTransformError e ) {
                    errors.add(e);
                }
            }
        }

        for ( String prefixName : prefixManager.getUnresolvedPrefixNames() ) {
            errors.add(new SSSOMTransformError(String.format("Undeclared prefix: %s", prefixName)));
        }

        hasRead = true;

        return !hasErrors();
    }

    /**
     * Gets the SSSOM/T rules that have been parsed from the underlying source. This
     * method sould be called after calling {@link #read()} and checking that it
     * returned {@code true}, indicating that parsing was successful.
     * <p>
     * As a convenience, this method will call {@link #read()} automatically if
     * needed, if an input has been set. The caller should then use
     * {@link #hasErrors()} to check whether syntax errors were found.
     * 
     * @return The SSSOM/T processing rules. May be an empty list if nothing has
     *         been parsed or if syntax errors were found.
     */
    public List<MappingProcessingRule<T>> getRules() {
        if ( !hasRead && lexer != null ) {
            read();
        }
        return rules;
    }

    /**
     * Indicates whether parsing errors occurred. Calling this method after
     * {@link #read()} is another way of checking whether syntax errors were found
     * when parsing.
     * 
     * @return {@code true} if at least one parsing error occured, otherwise
     *         {@code false}.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Gets all syntax errors that were found when parsing, if any.
     * <p>
     * The parser does not throw any exception upon encountering a SSSOM/T syntax
     * error (it only throws {@link java.io.IOException} upon I/O errors unrelated
     * to SSSOM/T. Instead, all syntax errors are collected in the form of
     * {@link SSSOMTransformError} objects, which may be retrieved with this method.
     * 
     * @return A list of objects representing the syntax errors (empty if no errors
     *         occured).
     */
    public List<SSSOMTransformError> getErrors() {
        return errors;
    }

    /**
     * Parses a SSSOM/Transform action into a preprocessor.
     * <p>
     * This parser recognises two preprocessing instructions: {@code stop()} to
     * create a preprocessor that will stop any further treatment for the current
     * mapping, and {@code invert()} to create a preprocessor that will invert the
     * current mapping.
     * <p>
     * Override this method to extend and/or replace the list of supported
     * preprocessing instructions. Note that this method should not throw an
     * exception if the function name is not recognised: it should return
     * {@code null} instead, so that the parser may then try to recognise it as a
     * generating instruction.
     * 
     * @param name      The name of the function, as it appears in the SSSOM/T file.
     * @param arguments The arguments passed to the function.
     * @return The preprocessor to add to the rule that is being parsed, or
     *         {@code null} if the name does not match any allowed preprocessing
     *         instruction name.
     * @throws SSSOMTransformError If the name does match an allowed preprocessing
     *                             instruction name, but there is a problem with the
     *                             list of arguments (e.g. not enough arguments).
     */
    protected IMappingTransformer<Mapping> parsePreprocessingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        if ( name.equals("stop") ) {
            return new NamedMappingTransformer<Mapping>("stop()", (mapping) -> null);
        } else if ( name.equals("invert") ) {
            return new NamedMappingTransformer<Mapping>("invert()", (mapping) -> CommonPredicate.invert(mapping));
        }

        return null;
    }

    /**
     * Parses a SSSOM/Transform action a generator.
     * 
     * @param name      The name of the function, as it appears in the SSSOM/T file.
     * @param arguments The arguments passed to the function.
     * @return The generator to add to the rule that is being parsed.
     * @throws SSSOMTransformError If the name does not match any allowed generating
     *                             instruction name, or if there is a problem with
     *                             the list of arguments.
     */
    protected abstract IMappingTransformer<T> parseGeneratingAction(String name, List<String> arguments)
            throws SSSOMTransformError;

    private class ErrorListener extends BaseErrorListener {
        private List<SSSOMTransformError> errors;

        private ErrorListener(List<SSSOMTransformError> errors) {
            this.errors = errors;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException e) {
            errors.add(new SSSOMTransformError(line, column, message));
        }
    }
}

/*
 * Visitor to convert the ANTLR parse tree for SSSOM/T into a list of ParsedRule
 * objects.
 */
class ParseTree2RuleVisitor extends SSSOMTransformBaseVisitor<Void> {
    List<SSSOMTransformRule> rules;
    Deque<IMappingFilter> filters = new ArrayDeque<IMappingFilter>();
    Deque<Set<String>> tags = new ArrayDeque<Set<String>>();
    PrefixManager prefixManager;

    ParseTree2RuleVisitor(List<SSSOMTransformRule> rules, PrefixManager prefixManager) {
        this.rules = rules;
        this.prefixManager = prefixManager;
    }

    @Override
    public Void visitPrefixDecl(SSSOMTransformParser.PrefixDeclContext ctx) {
        String prefixName = ctx.PREFIX().getText();
        prefixName = prefixName.substring(0, prefixName.length() - 1);

        String prefix = ctx.IRI().getText();
        prefix = prefix.substring(1, prefix.length() - 1);

        prefixManager.add(prefixName, prefix);

        return null;
    }

    @Override
    public Void visitRule(SSSOMTransformParser.RuleContext ctx) {
        // Get the filters for the rule.
        ctx.filterSet().accept(this);

        // Get the tags.
        if ( ctx.tags() != null ) {
            ctx.tags().accept(this);
        } else {
            // No tags, push an empty tag set.
            tags.add(new HashSet<String>());
        }

        if ( ctx.rule_().isEmpty() ) {
            // This is a "terminal" rule, visit the action part.
            visitChildren(ctx.actionSet());
        } else {
            // There are subrules, visit them.
            for ( SSSOMTransformParser.RuleContext subrule : ctx.rule_() ) {
                subrule.accept(this);
            }
        }

        // Pop the current filters and tags.
        filters.removeLast();
        tags.removeLast();

        return null;
    }

    @Override
    public Void visitFilterSet(SSSOMTransformParser.FilterSetContext ctx) {
        ParseTree2FilterVisitor v = new ParseTree2FilterVisitor(prefixManager);
        filters.add(ctx.accept(v));

        return null;
    }

    @Override
    public Void visitTags(SSSOMTransformParser.TagsContext ctx) {
        Set<String> thisLevelTags = new HashSet<String>();
        ctx.TAG().forEach((tag) -> thisLevelTags.add(tag.getText()));
        tags.add(thisLevelTags);

        return null;
    }

    @Override
    public Void visitAction(SSSOMTransformParser.ActionContext ctx) {
        // Assemble the final filter
        IMappingFilter filter;
        if ( filters.size() == 1 ) {
            filter = filters.peekLast();
        } else {
            FilterSet fs = new FilterSet();
            filters.forEach((f) -> fs.addFilter(f, true));
            filter = fs;
        }

        // Get action name
        String name = ctx.FUNCTION().getText();
        int nameLen = name.length();
        name = name.substring(0, nameLen - 1);

        SSSOMTransformRule rule = new SSSOMTransformRule(filter, name);

        // Assemble the final tag set
        tags.forEach((levelTags) -> rule.tags.addAll(levelTags));

        // Assemble the arguments list
        if ( ctx.arglist() != null ) {
            for ( ArgumentContext argCtx : ctx.arglist().argument() ) {
                if ( argCtx.string() != null ) {
                    rule.arguments.add(unquote(argCtx.string().getText()));
                } else if ( argCtx.IRI() != null ) {
                    String iri = argCtx.IRI().getText();
                    int iriLen = iri.length();
                    rule.arguments.add(iri.substring(1, iriLen - 1));
                } else if ( argCtx.CURIE() != null ) {
                    rule.arguments.add(prefixManager.expandIdentifier(argCtx.CURIE().getText()));
                }
            }
        }

        rules.add(rule);

        return null;
    }

    private String unquote(String s) {
        StringBuilder sb = new StringBuilder();
        for ( int i = 1, n = s.length(); i < n - 1; i++ ) {
            char c = s.charAt(i);
            if ( c != '\\' ) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}

/*
 * Represents a processing rule after it has been parsed from the SSSOM/T
 * source, but before the application-specific action part has been parsed into
 * an actual preprocessor or generator object.
 */
class SSSOMTransformRule {
    IMappingFilter filter;
    String instruction;
    List<String> arguments = new ArrayList<String>();
    Set<String> tags = new HashSet<String>();

    SSSOMTransformRule(IMappingFilter filter, String instruction) {
        this.filter = filter;
        this.instruction = instruction;
    }
}

/*
 * Visitor to convert the ANTLR parse tree into a mapping filter object.
 */
class ParseTree2FilterVisitor extends SSSOMTransformBaseVisitor<IMappingFilter> {
    private FilterSet filterSet = new FilterSet();
    private String lastOperator = "&&";
    private PrefixManager prefixManager;

    public ParseTree2FilterVisitor(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    @Override
    public IMappingFilter visitFilterSet(SSSOMTransformParser.FilterSetContext ctx) {
        visitChildren(ctx);

        return filterSet;
    }

    /*
     * Create a filter object to filter mappings on the value of a single ID field.
     */
    @Override
    public IMappingFilter visitIdFilterItem(SSSOMTransformParser.IdFilterItemContext ctx) {
        String fieldName = ctx.idField().getText();
        String value = ctx.idValue().getText();

        if ( value.equals("*") ) {
            // The entire value is a joker, create a dummy filter that accepts everything
            return addFilter(new NamedFilter("*", (mapping) -> true));
        }

        value = prefixManager.expandIdentifier(value);
        boolean glob = value.endsWith("*");
        String pattern = glob ? value.substring(0, value.length() - 1) : value;

        Function<String, Boolean> testValue = glob ? (v) -> v != null && v.startsWith(pattern)
                : (v) -> v != null && v.equals(pattern);

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "subject":
            filter = (mapping) -> testValue.apply(mapping.getSubjectId());
            break;

        case "object":
            filter = (mapping) -> testValue.apply(mapping.getObjectId());
            break;

        case "predicate":
            filter = (mapping) -> testValue.apply(mapping.getPredicateId())
                    && mapping.getPredicateModifier() != PredicateModifier.NOT;
            break;

        case "mapping_justification":
            filter = (mapping) -> testValue.apply(mapping.getMappingJustification());
            break;

        case "subject_source":
            filter = (mapping) -> testValue.apply(mapping.getSubjectSource());
            break;

        case "object_source":
            filter = (mapping) -> testValue.apply(mapping.getObjectSource());
            break;

        case "mapping_source":
            filter = (mapping) -> testValue.apply(mapping.getMappingSource());
            break;
        }

        return addFilter(new NamedFilter(String.format("%s==%s", fieldName, value), filter));
    }

    @Override
    public IMappingFilter visitMultiIdFilterItem(SSSOMTransformParser.MultiIdFilterItemContext ctx) {
        String fieldName = ctx.mulIdField().getText();
        String value = ctx.idValue().getText();

        if ( value.equals("*") ) {
            return addFilter(new NamedFilter("*", (mapping) -> true));
        }

        value = prefixManager.expandIdentifier(value);
        boolean glob = value.endsWith("*");
        String pattern = glob ? value.substring(0, value.length() - 1) : value;

        Function<List<String>, Boolean> testValue = glob ? (v) -> v != null && globListValue(v, pattern)
                : (v) -> v != null && v.contains(pattern);

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "creator":
            filter = (mapping) -> testValue.apply(mapping.getCreatorId());
            break;

        case "author":
            filter = (mapping) -> testValue.apply(mapping.getAuthorId());
            break;

        case "reviewer":
            filter = (mapping) -> testValue.apply(mapping.getReviewerId());
            break;

        case "curation_rule":
            filter = (mapping) -> testValue.apply(mapping.getCurationRule());
            break;

        case "subject_match_field":
            filter = (mapping) -> testValue.apply(mapping.getSubjectMatchField());
            break;

        case "object_match_field":
            filter = (mapping) -> testValue.apply(mapping.getObjectMatchField());
            break;

        case "subject_preprocessing":
            filter = (mapping) -> testValue.apply(mapping.getSubjectPreprocessing());
            break;

        case "object_preprocessing":
            filter = (mapping) -> testValue.apply(mapping.getObjectPreprocessing());
            break;
        }

        return addFilter(new NamedFilter(String.format("%s==%s", fieldName, value), filter));
    }

    public static boolean globListValue(List<String> values, String pattern) {
        for ( String value : values ) {
            if ( value.startsWith(pattern) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IMappingFilter visitNumFilterItem(SSSOMTransformParser.NumFilterItemContext ctx) {
        String fieldName = ctx.numField().getText();
        String operator = ctx.numOp().getText();
        Double value = Double.valueOf(ctx.DOUBLE().getText());

        Function<Double, Boolean> testValue = null;
        switch ( operator ) {
        case "==":
            testValue = (v) -> v != null && v == value;
            break;

        case ">":
            testValue = (v) -> v != null && v > value;
            break;

        case ">=":
            testValue = (v) -> v != null && v >= value;
            break;

        case "<":
            testValue = (v) -> v != null && v < value;
            break;

        case "<=":
            testValue = (v) -> v != null && v <= value;
            break;
        }
        Function<Double, Boolean> finalTestValue = testValue;

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "confidence":
            filter = (mapping) -> finalTestValue.apply(mapping.getConfidence());
            break;

        case "semantic_similarity_score":
            filter = (mapping) -> finalTestValue.apply(mapping.getSemanticSimilarityScore());
            break;
        }

        return addFilter(new NamedFilter(String.format("%s%s%.2f", fieldName, operator, value), filter));
    }

    @Override
    public IMappingFilter visitPredicateModifierFilterItem(
            SSSOMTransformParser.PredicateModifierFilterItemContext ctx) {
        return addFilter(new NamedFilter("predicate_modifier==Not",
                (mapping) -> mapping.getPredicateModifier() == PredicateModifier.NOT));
    }

    @Override
    public IMappingFilter visitGroupFilterItem(SSSOMTransformParser.GroupFilterItemContext ctx) {
        ParseTree2FilterVisitor v = new ParseTree2FilterVisitor(prefixManager);
        return addFilter(ctx.filterSet().accept(v));
    }

    @Override
    public IMappingFilter visitNegatedFilterItem(SSSOMTransformParser.NegatedFilterItemContext ctx) {
        ParseTree2FilterVisitor v = new ParseTree2FilterVisitor(prefixManager);
        IMappingFilter negatedFilter = ctx.filterItem().accept(v);
        return addFilter(new NamedFilter(String.format("!%s", negatedFilter.toString()),
                (mapping) -> !negatedFilter.filter(mapping)));
    }

    @Override
    public IMappingFilter visitBinaryOp(SSSOMTransformParser.BinaryOpContext ctx) {
        lastOperator = ctx.getText();

        return null;
    }

    private IMappingFilter addFilter(IMappingFilter newFilter) {
        filterSet.addFilter(newFilter, lastOperator.equals("&&"));

        lastOperator = "&&";

        return newFilter;
    }
}
