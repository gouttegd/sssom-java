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
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformBaseVisitor;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformLexer;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformParser;
import org.incenp.obofoundry.sssom.transform.parser.SSSOMTransformParser.ArgumentContext;

/**
 * A parser to read mapping processing rules in the SSSOM Transform language.
 * <p>
 * This parser knows nothing of the functions that are allowed to be used in
 * SSSOM/T rules and what they are supposed to do. Client code must initialise
 * the parser with a {@link ISSSOMTransformApplication} implementation that will
 * provide the required application-specific knowledge.
 * 
 * @see <a href="https://incenp.org/dvlpt/sssom-java/sssom-transform.html">The
 *      SSSOM/Transform language</a>
 *
 * @param <T> The type of object that should be produced by the processing rules
 *            for each mapping.
 */
public class SSSOMTransformReader<T> {
    private SSSOMTransformLexer lexer;
    protected List<MappingProcessingRule<T>> rules = new ArrayList<MappingProcessingRule<T>>();
    protected List<SSSOMTransformError> errors = new ArrayList<SSSOMTransformError>();
    private ISSSOMTransformApplication<T> app;
    protected PrefixManager prefixManager = new PrefixManager();
    private boolean hasRead = false;

    /**
     * Creates a new instance without an input source. Use this constructor to parse
     * SSSOM Transform from something else than a file or file-like source, coupled
     * with the {@link #read(String)} method.
     * 
     */

    /**
     * Creates a new instance without an input source. Use this constructor to parse
     * SSSOM Transform from something else than a file or file-like source, coupled
     * with the {@link #read(String)} method.
     * 
     * @param application The SSSOM/T specialised application.
     */
    public SSSOMTransformReader(ISSSOMTransformApplication<T> application) {
        app = application;
    }

    /**
     * Creates a new instance to read from a reader object.
     * 
     * @param application The SSSOM/T specialised application.
     * @param input       The reader to parse the SSSOM/T ruleset from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     reader object.
     */
    public SSSOMTransformReader(ISSSOMTransformApplication<T> application, Reader input) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromReader(input));
        app = application;
    }

    /**
     * Creates a new instance to read from a stream.
     * 
     * @param application The SSSOM/T specialised application.
     * @param input       The stream to parse the SSSOM/T ruleset from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     stream.
     */
    public SSSOMTransformReader(ISSSOMTransformApplication<T> application, InputStream input) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromStream(input));
        app = application;
    }

    /**
     * Creates a new instance to read from a file.
     * 
     * @param application The SSSOM/T specialised application.
     * @param input       The file to parse the SSSOM/T ruleset from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     file.
     */
    public SSSOMTransformReader(ISSSOMTransformApplication<T> application, File input) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromFileName(input.getPath()));
        app = application;
    }

    /**
     * Creates a new instance to read from a file.
     * 
     * @param application The SSSOM/T specialised application.
     * @param filename    The name of the file to read from.
     * @throws IOException If any non-SSSOM/T I/O error occurs when reading from the
     *                     file.
     */
    public SSSOMTransformReader(ISSSOMTransformApplication<T> application, String filename) throws IOException {
        lexer = new SSSOMTransformLexer(CharStreams.fromFileName(filename));
        app = application;
    }

    /**
     * Sets the prefix manager to use. Calling this method voids all prefixes
     * previously declared with {@link #addPrefix(String, String)} or
     * {@link #addPrefixMap(Map)}.
     * <p>
     * This method is intended to be used in situations where the calling code may
     * need more than just the prefix map itself -- for example, if it needs to know
     * whether the manager has encountered any prefix that it could not expand or
     * any IRI that it could not shorten.
     * 
     * @param prefixManager The prefix manager used by this reader.
     */
    public void setPrefixManager(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
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
     * Gets the effective prefix map used by this reader. It contains all prefixes
     * known to the reader, whether they were read from a SSSOM/T file or explicitly
     * added by {@link #addPrefix(String, String)}.
     * 
     * @return The prefix map as used by the reader.
     */
    public Map<String, String> getPrefixMap() {
        return prefixManager.getPrefixMap();
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
     * <p>
     * For convenience, this method does not require a single-action rule to be
     * terminated by a semi-colon. That is, an input of
     * 
     * <pre>
     * "subject==UBERON:* -&gt; stop()"
     * </pre>
     * 
     * will be accepted as equivalent to
     * 
     * <pre>
     * "subject==UBERON:* -&gt; stop();"
     * </pre>
     * 
     * even though the former is, strictly speaking, incorrect as per the SSSOM/T
     * syntax. This is only true if the input has no trailing whitespace, though.
     * 
     * @param text The SSSOM/T ruleset to parse.
     * @return {@code true} if the ruleset was successfully parsed, or {@code false}
     *         of SSSOM/T syntax errors were found.
     */
    public boolean read(String text) {
        int len = text.length();
        if ( len == 0 ) {
            return true; // Just ignore empty string
        }

        /*
         * The parser is primarily designed to read from files, so the syntax mandates a
         * whitespace at the end of the ruleset. Callers of that method may not expect
         * that the input string needs to be terminated by a whitespace though, so for
         * convenience we forcibly add a terminating whitespace if needed. This is a bit
         * hacky but simpler than amending the syntax to allow for the absence of
         * whitespace.
         * 
         * For the same reason, we also add the ';' terminator if required.
         */
        char last = text.charAt(len - 1);
        if ( last == ' ' || last == '\t' || last == '\n' || last == '\r' ) {
            // Terminating whitespace is there, so we assume the caller took care of
            // everything
        } else if ( last == ';' || last == '}' ) {
            // Rule has a terminator, but still needs a whitespace
            text += "\n";
        } else {
            // Rule needs both a terminator and a whitespace
            text += ";\n";
        }

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
            app.onInit(prefixManager);
            ParseTree2RuleVisitor<T> visitor = new ParseTree2RuleVisitor<T>(rules, errors, prefixManager, app);
            visitor.visit(tree);
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
     * <p>
     * When {@link #read(String)} is called repeatedly on different inputs, this
     * method always returns <em>all</em> the rules that have been parsed since this
     * object was created, not only the rules resulting from the last
     * {@link #read(String)} call.
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
 * Visitor to convert the ANTLR parse tree for SSSOM/T into a list of ParsedRule
 * objects.
 */
class ParseTree2RuleVisitor<T> extends SSSOMTransformBaseVisitor<Void> {

    List<MappingProcessingRule<T>> rules;
    List<SSSOMTransformError> errors;
    Deque<IMappingFilter> filters = new ArrayDeque<IMappingFilter>();
    Deque<Set<String>> tags = new ArrayDeque<Set<String>>();
    PrefixManager prefixManager;
    ISSSOMTransformApplication<T> application;

    ParseTree2RuleVisitor(List<MappingProcessingRule<T>> rules, List<SSSOMTransformError> errors,
            PrefixManager prefixManager,
            ISSSOMTransformApplication<T> application) {
        this.rules = rules;
        this.errors = errors;
        this.prefixManager = prefixManager;
        this.application = application;
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
    public Void visitHeaderDecl(SSSOMTransformParser.HeaderDeclContext ctx) {
        String name = ctx.action().FUNCTION().getText();
        int nameLen = name.length();
        name = name.substring(0, nameLen - 1);

        ArrayList<String> arguments = new ArrayList<String>();

        if ( ctx.action().arglist() != null ) {
            for ( ArgumentContext argCtx : ctx.action().arglist().argument() ) {
                if ( argCtx.string() != null ) {
                    arguments.add(unescape(argCtx.string().getText()));
                } else if ( argCtx.IRI() != null ) {
                    String iri = argCtx.IRI().getText();
                    int iriLen = iri.length();
                    arguments.add(iri.substring(1, iriLen - 1));
                } else if ( argCtx.CURIE() != null ) {
                    arguments.add(prefixManager.expandIdentifier(argCtx.CURIE().getText()));
                }
            }
        }

        try {
            application.onHeaderAction(name, arguments);
        } catch ( SSSOMTransformError e ) {
            errors.add(e);
        }

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

        // Get function name
        String name = ctx.FUNCTION().getText();
        int nameLen = name.length();
        name = name.substring(0, nameLen - 1);

        // Assemble the arguments list
        List<String> arguments = new ArrayList<String>();
        if ( ctx.arglist() != null ) {
            for ( ArgumentContext argCtx : ctx.arglist().argument() ) {
                if ( argCtx.string() != null ) {
                    arguments.add(unescape(argCtx.string().getText()));
                } else if ( argCtx.IRI() != null ) {
                    String iri = argCtx.IRI().getText();
                    int iriLen = iri.length();
                    arguments.add(iri.substring(1, iriLen - 1));
                } else if ( argCtx.CURIE() != null ) {
                    arguments.add(prefixManager.expandIdentifier(argCtx.CURIE().getText()));
                }
            }
        }

        // Get the action from the application
        IMappingTransformer<Mapping> preprocessor = null;
        IMappingTransformer<T> generator = null;
        try {
            preprocessor = application.onPreprocessingAction(name, arguments);
            generator = null;
            if ( preprocessor == null ) {
                generator = application.onGeneratingAction(name, arguments);
            }
            ;
        } catch ( SSSOMTransformError e ) {
            errors.add(e);
        }

        // Assemble the final rule
        if ( preprocessor != null || generator != null ) {
            MappingProcessingRule<T> rule = new MappingProcessingRule<T>(filter, preprocessor, generator);

            // Assemble the final tag set
            tags.forEach((levelTags) -> rule.getTags().addAll(levelTags));

            /* TODO: There should be a cleaner way to do this. */
            if ( filter.toString().contains("cardinality==") ) {
                rule.setCardinalityNeeded(true);
            }

            rules.add(rule);
        }

        return null;
    }

    // Un-quote and un-escape the string as provided by the parser
    // This is static because it does not depend on any state and is re-used below
    // by the filter visitor.
    static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for ( int i = 1, n = s.length(); i < n - 1; i++ ) {
            char c = s.charAt(i);
            if ( c != '\\' || escaped ) {
                sb.append(c);
                escaped = false;
            } else if ( i < n - 2 ) {
                char next = s.charAt(i + 1);
                if ( next == '"' || next == '\'' || next == '\\' ) {
                    escaped = true;
                } else {
                    sb.append(c);
                    escaped = false;
                }
            } else {
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

    @Override
    public IMappingFilter visitIdFilterItem(SSSOMTransformParser.IdFilterItemContext ctx) {
        return handleTextBasedFilter(ctx.idField().getText(), ctx.idValue().getText(), true);
    }

    @Override
    public IMappingFilter visitTextFilterItem(SSSOMTransformParser.TextFilterItemContext ctx) {
        return handleTextBasedFilter(ctx.txField().getText(), ParseTree2RuleVisitor.unescape(ctx.string().getText()),
                false);
    }

    /*
     * Handle filters on ID and text fields. Both types of filters are handled in
     * almost exactly the same way, the only difference is that for ID fields, the
     * value to test against must be expanded.
     */
    private IMappingFilter handleTextBasedFilter(String fieldName, String value, boolean expand) {
        if ( value.equals("*") ) {
            // The entire value is a joker, create a dummy filter that accepts everything
            return addFilter(new NamedFilter("*", (mapping) -> true));
        }

        boolean empty = expand ? value.equals("~") : value.isEmpty();
        if ( expand && !empty ) {
            value = prefixManager.expandIdentifier(value);
        }

        boolean glob = !empty && value.endsWith("*");
        String pattern = glob ? value.substring(0, value.length() - 1) : value;
        Function<String, Boolean> testValue;
        if ( empty ) {
            testValue = (v) -> v == null || v.isEmpty();
        } else if ( glob ) {
            testValue = (v) -> v != null && v.startsWith(pattern);
        } else {
            testValue = (v) -> v != null && v.equals(pattern);
        }

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "comment":
            filter = (mapping) -> testValue.apply(mapping.getComment());
            break;

        case "issue_tracker_item":
            filter = (mapping) -> testValue.apply(mapping.getIssueTrackerItem());
            break;

        case "license":
            filter = (mapping) -> testValue.apply(mapping.getLicense());
            break;

        case "mapping_justification":
        case "justification":
            filter = (mapping) -> testValue.apply(mapping.getMappingJustification());
            break;

        case "mapping_provider":
            filter = (mapping) -> testValue.apply(mapping.getMappingProvider());
            break;

        case "mapping_source":
            filter = (mapping) -> testValue.apply(mapping.getMappingSource());
            break;

        case "mapping_tool":
            filter = (mapping) -> testValue.apply(mapping.getMappingTool());
            break;

        case "mapping_tool_version":
            filter = (mapping) -> testValue.apply(mapping.getMappingToolVersion());
            break;

        case "object":
            filter = (mapping) -> testValue.apply(mapping.getObjectId());
            break;

        case "object_category":
            filter = (mapping) -> testValue.apply(mapping.getObjectCategory());
            break;

        case "object_label":
            filter = (mapping) -> testValue.apply(mapping.getObjectLabel());
            break;

        case "object_source":
            filter = (mapping) -> testValue.apply(mapping.getObjectSource());
            break;

        case "object_source_version":
            filter = (mapping) -> testValue.apply(mapping.getObjectSourceVersion());
            break;

        case "other":
            filter = (mapping) -> testValue.apply(mapping.getOther());
            break;

        case "predicate":
            filter = (mapping) -> testValue.apply(mapping.getPredicateId())
                    && mapping.getPredicateModifier() != PredicateModifier.NOT;
            break;

        case "predicate_label":
            filter = (mapping) -> testValue.apply(mapping.getPredicateLabel())
                    && mapping.getPredicateModifier() != PredicateModifier.NOT;
            break;

        case "similarity_measure":
            filter = (mapping) -> testValue.apply(mapping.getSimilarityMeasure());
            break;

        case "subject":
            filter = (mapping) -> testValue.apply(mapping.getSubjectId());
            break;

        case "subject_category":
            filter = (mapping) -> testValue.apply(mapping.getSubjectCategory());
            break;

        case "subject_label":
            filter = (mapping) -> testValue.apply(mapping.getSubjectLabel());
            break;

        case "subject_source":
            filter = (mapping) -> testValue.apply(mapping.getSubjectSource());
            break;

        case "subject_source_version":
            filter = (mapping) -> testValue.apply(mapping.getSubjectSourceVersion());
            break;
        }

        return addFilter(new NamedFilter(String.format("%s==%s", fieldName, value), filter));
    }

    @Override
    public IMappingFilter visitMultiIdFilterItem(SSSOMTransformParser.MultiIdFilterItemContext ctx) {
        return handleTextBasedListFilter(ctx.mulIdField().getText(), ctx.idValue().getText(), true);
    }

    @Override
    public IMappingFilter visitMultiTextFilterItem(SSSOMTransformParser.MultiTextFilterItemContext ctx) {
        return handleTextBasedListFilter(ctx.mulTxField().getText(),
                ParseTree2RuleVisitor.unescape(ctx.string().getText()), false);
    }

    /*
     * Handle filters on fields containing a list of either ID or text values.
     * Again, both types of filters are handled in almost exactly the same way, the
     * only difference is that for ID fields, the value to test against must be
     * expanded.
     */
    private IMappingFilter handleTextBasedListFilter(String fieldName, String value, boolean expand) {
        if ( value.equals("*") ) {
            return addFilter(new NamedFilter("*", (mapping) -> true));
        }

        boolean empty = expand ? value.equals("~") : value.isEmpty();
        if ( expand && !empty ) {
            value = prefixManager.expandIdentifier(value);
        }

        boolean glob = value.endsWith("*");
        String pattern = glob ? value.substring(0, value.length() - 1) : value;

        Function<List<String>, Boolean> testValue;
        if ( empty ) {
            testValue = (v) -> v == null || v.isEmpty();
        } else if ( glob ) {
            testValue = (v) -> {
                if ( v != null ) {
                    for ( String item : v ) {
                        if ( item.startsWith(pattern) ) {
                            return true;
                        }
                    }
                }
                return false;
            };
        } else {
            testValue = (v) -> v != null && v.contains(pattern);
        }

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "author":
            filter = (mapping) -> testValue.apply(mapping.getAuthorId());
            break;

        case "author_label":
            filter = (mapping) -> testValue.apply(mapping.getAuthorLabel());
            break;

        case "creator":
            filter = (mapping) -> testValue.apply(mapping.getCreatorId());
            break;

        case "creator_label":
            filter = (mapping) -> testValue.apply(mapping.getCreatorLabel());
            break;

        case "curation_rule":
            filter = (mapping) -> testValue.apply(mapping.getCurationRule());
            break;

        case "curation_rule_text":
            filter = (mapping) -> testValue.apply(mapping.getCurationRuleText());
            break;

        case "object_match_field":
            filter = (mapping) -> testValue.apply(mapping.getObjectMatchField());
            break;

        case "object_preprocessing":
            filter = (mapping) -> testValue.apply(mapping.getObjectPreprocessing());
            break;

        case "reviewer":
            filter = (mapping) -> testValue.apply(mapping.getReviewerId());
            break;

        case "reviewer_label":
            filter = (mapping) -> testValue.apply(mapping.getReviewerLabel());
            break;

        case "see_also":
            filter = (mapping) -> testValue.apply(mapping.getSeeAlso());
            break;

        case "subject_match_field":
            filter = (mapping) -> testValue.apply(mapping.getSubjectMatchField());
            break;

        case "subject_preprocessing":
            filter = (mapping) -> testValue.apply(mapping.getSubjectPreprocessing());
            break;
        }

        return addFilter(new NamedFilter(String.format("%s==%s", fieldName, value), filter));
    }

    @Override
    public IMappingFilter visitNumFilterItem(SSSOMTransformParser.NumFilterItemContext ctx) {
        String fieldName = ctx.numField().getText();
        String asText;

        Function<Double, Boolean> testValue;

        if ( ctx.EMPTY() != null ) {
            asText = String.format("%s==~", fieldName);
            testValue = (v) -> v == null;
        } else {
            String operator = ctx.numOp().getText();
            Double value = Double.valueOf(ctx.DOUBLE().getText());
            asText = String.format("%s%s%.2f", fieldName, operator, value);

            switch ( operator ) {
            case "==":
            default:
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
        }

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "confidence":
            filter = (mapping) -> testValue.apply(mapping.getConfidence());
            break;

        case "semantic_similarity_score":
        case "similarity_score":
            filter = (mapping) -> testValue.apply(mapping.getSimilarityScore());
            break;
        }

        return addFilter(new NamedFilter(asText, filter));
    }

    @Override
    public IMappingFilter visitCardFilterItem(SSSOMTransformParser.CardFilterItemContext ctx) {
        String value = ctx.CARDVALUE() != null ? ctx.CARDVALUE().getText() : ctx.EMPTY().getText();
        IMappingFilter filter = null;
        switch ( value ) {
        case "*:n":
            filter = (mapping) -> mapping.getMappingCardinality() == MappingCardinality.ONE_TO_MANY
                    || mapping.getMappingCardinality() == MappingCardinality.MANY_TO_MANY;
            break;

        case "*:1":
            filter = (mapping) -> mapping.getMappingCardinality() == MappingCardinality.ONE_TO_ONE
                    || mapping.getMappingCardinality() == MappingCardinality.MANY_TO_ONE;
            break;

        case "n:*":
            filter = (mapping) -> mapping.getMappingCardinality() == MappingCardinality.MANY_TO_ONE
                    || mapping.getMappingCardinality() == MappingCardinality.MANY_TO_MANY;
            break;

        case "1:*":
            filter = (mapping) -> mapping.getMappingCardinality() == MappingCardinality.ONE_TO_ONE
                    || mapping.getMappingCardinality() == MappingCardinality.ONE_TO_MANY;
            break;

        case "~":
            filter = (mapping) -> mapping.getMappingCardinality() == null;
            break;

        default:
            MappingCardinality mc = MappingCardinality.fromString(value);
            filter = (mapping) -> mapping.getMappingCardinality() == mc;
            break;
        }
        return addFilter(new NamedFilter(String.format("cardinality==%s", value), filter));
    }

    @Override
    public IMappingFilter visitPredicateModifierFilterItem(
            SSSOMTransformParser.PredicateModifierFilterItemContext ctx) {
        return addFilter(new NamedFilter("predicate_modifier==Not",
                (mapping) -> mapping.getPredicateModifier() == PredicateModifier.NOT));
    }

    @Override
    public IMappingFilter visitEntityTypeFilterItem(SSSOMTransformParser.EntityTypeFilterItemContext ctx) {
        String fieldName = ctx.entField().getText();
        String value = ParseTree2RuleVisitor.unescape(ctx.string().getText());

        EntityType et;
        if ( value.isEmpty() ) {
            et = null;
        } else if ( value.equals("*") ) {
            // Accept anything
            return addFilter(new NamedFilter("*", (mapping) -> true));
        } else {
            et = EntityType.fromString(value);
            if ( et == null ) {
                // Illegal value, reject everything
                return addFilter(new NamedFilter("*", (mapping) -> false));
            }
            value = et.toString();
        }

        IMappingFilter filter = null;
        switch ( fieldName ) {
        case "object_type":
            filter = (mapping) -> mapping.getObjectType() == et;
            break;

        case "subject_type":
            filter = (mapping) -> mapping.getSubjectType() == et;
            break;
        }

        return addFilter(new NamedFilter(String.format("%s==%s", fieldName, value), filter));
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
