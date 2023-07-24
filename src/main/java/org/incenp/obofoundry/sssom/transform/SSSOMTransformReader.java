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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformBaseVisitor;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformLexer;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformParser;

/**
 * A parser to read mapping processing rules in the SSSOM Transform language.
 *
 * @param <T> The type of object that should be produced by the processing rules
 *            for each mapping.
 */
public class SSSOMTransformReader<T> {
    private SSSOMTransformLexer lexer;
    private ITransformationParser<T> transformParser;
    private List<MappingProcessingRule<T>> rules = new ArrayList<MappingProcessingRule<T>>();
    private List<SSSOMTransformError> errors = new ArrayList<SSSOMTransformError>();
    private PrefixManager prefixManager = new PrefixManager();
    private boolean hasRead = false;

    /*
     * Common constructor to throw exception if the parser is null.
     */
    private SSSOMTransformReader(ITransformationParser<T> parser, SSSOMTransformLexer lexer) {
        if ( parser == null ) {
            throw new IllegalArgumentException("Missing valid transformation parser");
        }

        transformParser = parser;
        this.lexer = lexer;
    }

    /**
     * Creates a new instance without an input source. Use this constructor to parse
     * SSSOM Transform from something else than a file or file-like source, coupled
     * with the {@link #read(String)} method.
     * 
     * @param parser The parser for application-specific {@code gen} instructions.
     * @throws IllegalArgumentException If the provided parser is {@code null}.
     */
    public SSSOMTransformReader(ITransformationParser<T> parser) {
        if ( parser == null ) {
            throw new IllegalArgumentException("Missing valid transformation parser");
        }
        transformParser = parser;
    }

    /**
     * Creates a new instance to read from a reader object.
     * 
     * @param parser The parser for application-specific {@code gen} instructions.
     * @param input  The reader to parse the SSSOM/T ruleset from.
     * @throws IOException              If any non-SSSOM/T I/O error occurs when
     *                                  reading from the reader object.
     * @throws IllegalArgumentException If the provided parser is {@code null}.
     */
    public SSSOMTransformReader(ITransformationParser<T> parser, Reader input) throws IOException {
        this(parser, new SSSOMTransformLexer(CharStreams.fromReader(input)));
    }

    /**
     * Creates a new instance to read from a stream.
     * 
     * @param parser The parser for application-specific {@code gen} instructions.
     * @param input  The stream to parse the SSSOM/T ruleset from.
     * @throws IOException              If any non-SSSOM/T I/O error occurs when
     *                                  reading from the stream.
     * @throws IllegalArgumentException If the provided parser is {@code null}.
     */
    public SSSOMTransformReader(ITransformationParser<T> parser, InputStream input) throws IOException {
        this(parser, new SSSOMTransformLexer(CharStreams.fromStream(input)));
    }

    /**
     * Creates a new instance to read from a file.
     * 
     * @param parser The parser for application-specific {@code gen} instructions.
     * @param input  The file to parse the SSSOM/T ruleset from.
     * @throws IOException              If any non-SSSOM/T I/O error occurs when
     *                                  reading from the file.
     * @throws IllegalArgumentException If the provided parser is {@code null}.
     */
    public SSSOMTransformReader(ITransformationParser<T> parser, File input) throws IOException {
        this(parser, new SSSOMTransformLexer(CharStreams.fromFileName(input.getPath())));
    }

    /**
     * Creates a new instance to read from a file.
     * 
     * @param parser   The parser for application-specific {@code gen} instructions.
     * @param filename The name of the file to read from.
     * @throws IOException              If any non-SSSOM/T I/O error occurs when
     *                                  reading from the file.
     * @throws IllegalArgumentException If the provided parser is {@code null}.
     */
    public SSSOMTransformReader(ITransformationParser<T> parser, String filename) throws IOException {
        this(parser, new SSSOMTransformLexer(CharStreams.fromFileName(filename)));
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
        if ( transformParser == null ) {

        }

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
            List<ParsedRule> parsedRules = new ArrayList<ParsedRule>();
            ParseTree2RuleVisitor visitor = new ParseTree2RuleVisitor(parsedRules, prefixManager);
            visitor.visit(tree);

            for ( ParsedRule parsedRule : parsedRules ) {
                IMappingTransformer<T> t = null;

                if ( parsedRule.command != null ) {
                    t = transformParser.parse(parsedRule.command);
                    if ( t == null ) {
                        errors.add(new SSSOMTransformError(parsedRule.command));
                        continue;
                    }
                }

                MappingProcessingRule<T> finalRule = new MappingProcessingRule<T>(parsedRule.filter,
                        parsedRule.preprocessor, t);

                rules.add(finalRule);
            }
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
 * Represents a processing rule after it has been parsed from the SSSOM/T
 * source, but before the application-specific instruction has been parsed into
 * an actual IMappingTransformer object.
 */
class ParsedRule {
    IMappingFilter filter;
    IMappingTransformer<Mapping> preprocessor;
    String command;
    Set<String> tags = new HashSet<String>();
}

/*
 * Visitor to convert the ANTLR parse tree for SSSOM/T into a list of ParsedRule
 * objects.
 */
class ParseTree2RuleVisitor extends SSSOMTransformBaseVisitor<Void> {
    List<ParsedRule> rules;
    Deque<IMappingFilter> filters = new ArrayDeque<IMappingFilter>();
    Deque<Set<String>> tags = new ArrayDeque<Set<String>>();
    PrefixManager prefixManager;

    ParseTree2RuleVisitor(List<ParsedRule> rules, PrefixManager prefixManager) {
        this.rules = rules;
        this.prefixManager = prefixManager;
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
    public Void visitStop(SSSOMTransformParser.StopContext ctx) {
        rules.add(makeRule((mapping) -> null, null));
        return null;
    }

    @Override
    public Void visitInvert(SSSOMTransformParser.InvertContext ctx) {
        // TODO: Inversion operation not implemented yet;

        return null;
    }

    @Override
    public Void visitGenerate(SSSOMTransformParser.GenerateContext ctx) {
        rules.add(makeRule(null, unquote(ctx.string().getText())));

        return null;
    }

    private ParsedRule makeRule(IMappingTransformer<Mapping> preprocessor, String command) {
        ParsedRule pr = new ParsedRule();
        pr.preprocessor = preprocessor;
        pr.command = command;

        // Assemble the final filter for the rule by &&-combining the stacked filters.
        for ( IMappingFilter filter : filters ) {
            if ( pr.filter == null ) {
                pr.filter = filter;
            } else {
                pr.filter = (mapping) -> pr.filter.filter(mapping) && filter.filter(mapping);
            }
        }

        // Assemble the final tag set by merging the tag sets from the stack.
        tags.forEach((levelTags) -> pr.tags.addAll(levelTags));

        return pr;
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
 * Visitor to convert the ANTLR parse tree into a mapping filter object.
 */
class ParseTree2FilterVisitor extends SSSOMTransformBaseVisitor<IMappingFilter> {
    private IMappingFilter filter = null;
    private String lastOperator = "&&";
    private PrefixManager prefixManager;

    public ParseTree2FilterVisitor(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    @Override
    public IMappingFilter visitFilterSet(SSSOMTransformParser.FilterSetContext ctx) {
        visitChildren(ctx);

        return filter;
    }

    /*
     * Create a filter object to filter mappings on the value of a single field.
     */
    @Override
    public IMappingFilter visitSingleFilterItem(SSSOMTransformParser.SingleFilterItemContext ctx) {
        String fieldName = ctx.field().getText();
        String value = ctx.value().getText();

        if ( value.equals("*") ) {
            // The entire value is a joker, create a dummy filter that accepts everything
            addFilter((mapping) -> true);
            return null;
        }

        try {
            value = prefixManager.expandIdentifier(value);
        } catch ( SSSOMFormatException e ) {
            // Ignore?
        }

        boolean glob = value.endsWith("*");
        String pattern = glob ? value.substring(0, value.length() - 1) : value;

        // TODO: Implement filtering on other fields
        // This is very repetitive code, but I am reluctant to use reflection here.
        switch ( fieldName ) {
        case "subject":
            addFilter(glob ? (mapping) -> mapping.getSubjectId().startsWith(pattern)
                    : (mapping) -> mapping.getSubjectId().equals(pattern));
            break;

        case "predicate":
            addFilter(glob ? (mapping) -> mapping.getPredicateId().startsWith(pattern)
                    : (mapping) -> mapping.getPredicateId().equals(pattern));
            break;

        case "object":
            addFilter(glob ? (mapping) -> mapping.getObjectId().startsWith(pattern)
                    : (mapping) -> mapping.getObjectId().equals(pattern));
            break;
        }

        return null;
    }

    @Override
    public IMappingFilter visitGroupFilterItem(SSSOMTransformParser.GroupFilterItemContext ctx) {
        ParseTree2FilterVisitor v = new ParseTree2FilterVisitor(prefixManager);
        addFilter(ctx.filterSet().accept(v));
        
        return null;
    }

    @Override
    public IMappingFilter visitNegatedFilterItem(SSSOMTransformParser.NegatedFilterItemContext ctx) {
        ParseTree2FilterVisitor v = new ParseTree2FilterVisitor(prefixManager);
        IMappingFilter f = ctx.filterSet().accept(v);
        addFilter((mapping) -> !f.filter(mapping));

        return null;
    }

    @Override
    public IMappingFilter visitBinaryOp(SSSOMTransformParser.BinaryOpContext ctx) {
        lastOperator = ctx.getText();

        return null;
    }

    private void addFilter(IMappingFilter newFilter) {
        if ( filter == null ) {
            filter = newFilter;
        } else if ( lastOperator.equals("&&") ) {
            filter = (mapping) -> filter.filter(mapping) && newFilter.filter(mapping);
        } else {
            filter = (mapping) -> filter.filter(mapping) || newFilter.filter(mapping);
        }

        lastOperator = "&&";
    }
}
