/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.MappingHasher;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Helper class to format mappings into strings.
 * <p>
 * This class is intended to facilitate the creation of strings that may contain
 * values derived from mappings, using ”format specifiers” of the form
 * <code>%{placeholder}</code> (or <code>%placeholder</code>, but generally the
 * “bracketed” form should be preferred).
 * <p>
 * For example, this initialises a formatter that can substitute
 * <code>%{predicate}</code> and <code>%{justification}</code> in a string by
 * the predicate identifier and the justification of a mapping, respectively:
 * 
 * <pre>
 * MappingFormatter formatter = new MappingFormatter();
 * formatter.setSubstitution("predicate", (mapping) -&gt; mapping.getPredicateId());
 * formatter.setSubstitution("justification", (mapping) -$gt; mapping.getMappingJustification());
 * </pre>
 * <p>
 * That formatter may then be used as follows:
 * 
 * <pre>
 * String text = formatter.format("The mapping predicate is '%{predicate}', the justification is '%{justification'}",
 *         mapping);
 * </pre>
 * <p>
 * which will return (for example),
 * "{@literal The predicate is 'http://www.w3.org/2004/02/skos/core#exactMatch', the justification is 'https://w3id.org/semapv/vocab/ManualMappingCuration'}".
 * <p>
 * Another way of using the formatter is to obtain a {@link IMappingTransformer}
 * object that can then be used to directly transform a mapping into a formatted
 * string. The following would return the same value as the previous example:
 * 
 * <pre>
 * IMappingTransformer&lt;String&gt; transformer = formatter
 *         .getTransformer("The mapping predicate is '%{predicate}', the justification is '%{justification}'");
 * String text = transformer.transform(mapping);
 * </pre>
 */
public class MappingFormatter {

    private Map<String, IMappingTransformer<Object>> placeholders = new HashMap<String, IMappingTransformer<Object>>();
    private Map<String, IMappingTransformer<String>> cache = new HashMap<String, IMappingTransformer<String>>();
    private Map<String, IFormatModifierFunction> modifiers = new HashMap<String, IFormatModifierFunction>();

    private PrefixManager pfxMgr;
    private MappingHasher hasher = new MappingHasher();

    /**
     * Sets the prefix manager to use when attempting to resolve a placeholder name
     * into the name of an extension slot.
     * 
     * @param prefixManager The prefix manager.
     */
    public void setPrefixManager(PrefixManager prefixManager) {
        pfxMgr = prefixManager;
    }

    /**
     * Defines a placeholder text to be substituted by a value derived from a
     * mapping.
     * <p>
     * The placeholder will be recognised within a format string if it is found:
     * <ul>
     * <li>within curly backets preceded by a single '%' character, as in
     * <code>%{placeholder}</code>, or
     * <li>simply preceded by a single '%' character, as in
     * <code>%placeholder</code>.
     * </ul>
     * <p>
     * The placeholder cannot contain a '{' or a '|' character.
     * <p>
     * Note that in addition, an un-bracketed placeholder can only be recognised if
     * it starts with a letter, and contains only letters and underscores. So while
     * it is possible to define a placeholder named, for example,
     * <code>my-placeholder</code>, such a placeholder can only be used with the
     * bracketed form (<code>%{my-placeholder}</code>); <code>%my-placeholder</code>
     * would be interpreted as the placeholder <code>my</code> (if such a
     * placeholder exists) followed by the plain string <code>-placeholder</code>.
     * 
     * @param placeholder The placeholder value that should be substituted by a
     *                    mapping-derived value.
     * @param transformer The transformer to produce the value that should replace
     *                    the placeholder.
     * 
     * @throws IllegalArgumentException If the placeholder contains illegal
     *                                  characters.
     */
    public void setSubstitution(String placeholder, IMappingTransformer<Object> transformer) {
        if ( placeholder.contains("}") || placeholder.contains("|") ) {
            throw new IllegalArgumentException("Invalid placeholder name");
        }
        if ( placeholders.containsKey(placeholder) ) {
            cache.clear();
        }
        placeholders.put(placeholder, transformer);
    }

    /**
     * Defines “standard” substitutions using the name of mapping fields as defined
     * in the SSSOM specification. That is, this defines, for example,
     * <code>%{subject_id}</code> as a placeholder for the mapping’s subject ID, and
     * so on for all the standard fields.
     */
    public void setStandardSubstitutions() {
        // This is somewhat cumbersome, but only needs to be done once, so it would not
        // really be worth it to resort to reflection to automatically generate those
        // substitutions.
        placeholders.put("author_id", (m) -> m.getAuthorId());
        placeholders.put("author_label", (m) -> m.getAuthorLabel());
        placeholders.put("comment", (m) -> m.getComment());
        placeholders.put("confidence", (m) -> m.getConfidence());
        placeholders.put("creator_id", (m) -> m.getCreatorId());
        placeholders.put("creator_label", (m) -> m.getCreatorLabel());
        placeholders.put("curation_rule", (m) -> m.getCurationRule());
        placeholders.put("curation_rule_text", (m) -> m.getCurationRuleText());
        placeholders.put("issue_tracker_item", (m) -> m.getIssueTrackerItem());
        placeholders.put("license", (m) -> m.getLicense());
        placeholders.put("mapping_cardinality", (m) -> m.getMappingCardinality());
        placeholders.put("mapping_date", (m) -> m.getMappingDate());
        placeholders.put("mapping_justification", (m) -> m.getMappingJustification());
        placeholders.put("mapping_provider", (m) -> m.getMappingProvider());
        placeholders.put("mapping_source", (m) -> m.getMappingSource());
        placeholders.put("mapping_tool", (m) -> m.getMappingTool());
        placeholders.put("mapping_tool_id", (m) -> m.getMappingToolId());
        placeholders.put("mapping_tool_version", (m) -> m.getMappingToolVersion());
        placeholders.put("match_string", (m) -> m.getMatchString());
        placeholders.put("object_category", (m) -> m.getObjectCategory());
        placeholders.put("object_id", (m) -> m.getObjectId());
        placeholders.put("object_label", (m) -> m.getObjectLabel());
        placeholders.put("object_match_field", (m) -> m.getObjectMatchField());
        placeholders.put("object_preprocessing", (m) -> m.getObjectPreprocessing());
        placeholders.put("object_source", (m) -> m.getObjectSource());
        placeholders.put("object_source_version", (m) -> m.getObjectSourceVersion());
        placeholders.put("object_type", (m) -> m.getObjectType());
        placeholders.put("other", (m) -> m.getOther());
        placeholders.put("predicate_id", (m) -> m.getPredicateId());
        placeholders.put("predicate_label", (m) -> m.getPredicateLabel());
        placeholders.put("predicate_modifier", (m) -> m.getPredicateModifier());
        placeholders.put("publication_date", (m) -> m.getPublicationDate());
        placeholders.put("record_id", (m) -> m.getRecordId());
        placeholders.put("reviewer_id", (m) -> m.getReviewerId());
        placeholders.put("reviewer_label", (m) -> m.getReviewerLabel());
        placeholders.put("see_also", (m) -> m.getSeeAlso());
        placeholders.put("similarity_measure", (m) -> m.getSimilarityMeasure());
        placeholders.put("similarity_score", (m) -> m.getSimilarityScore());
        placeholders.put("subject_category", (m) -> m.getSubjectCategory());
        placeholders.put("subject_id", (m) -> m.getSubjectId());
        placeholders.put("subject_label", (m) -> m.getSubjectLabel());
        placeholders.put("subject_match_field", (m) -> m.getSubjectMatchField());
        placeholders.put("subject_preprocessing", (m) -> m.getSubjectPreprocessing());
        placeholders.put("subject_source", (m) -> m.getSubjectSource());
        placeholders.put("subject_source_version", (m) -> m.getSubjectSourceVersion());
        placeholders.put("subject_type", (m) -> m.getSubjectType());

        placeholders.put("hash", (m) -> hasher.hash(m));

        // Don't bother checking if we replaced existing substitutions, always clear the
        // cache.
        cache.clear();
    }

    /**
     * Registers a modifier function that can be used to alter the value of a
     * substitution.
     * <p>
     * In a format string, a modifier function can called within the brackets after
     * the name of the placeholder, separated from it by a '|' character, as in
     * <code>%{placeholder|modifier(arg1, arg2)}</code>.
     * <p>
     * The modifier function will receive as its first argument the mapping-derived
     * substitution value for the placeholder (optionally modified by other
     * modifiers). Remaining arguments are those explicitly passed to the function,
     * if any (<em>arg1</em> and <em>arg2</em> in the example above). The function
     * must return the value that should effectively be inserted into the formatted
     * string.
     * <p>
     * If the function does not need any additional argument beyond the mandatory
     * substitution value, the parentheses may be omitted, as in
     * <code>%{placeholder|modifier}</code>.
     * <p>
     * Modifier functions can only be used with the bracketed syntax.
     * 
     * @param modifier The modifier function to register.
     */
    public void setModifier(IFormatModifierFunction modifier) {
        String name = modifier.getName();
        if ( modifiers.containsKey(name) ) {
            cache.clear();
        }
        modifiers.put(name, modifier);
    }

    /**
     * Register the "standards" modifier functions available in this package.
     * <p>
     * The modifier functions that require a prefix manager to work are only
     * registered if a prefix manager has been set with
     * {@link #setPrefixManager(PrefixManager)}.
     */
    public void setStandardModifiers() {
        setModifier(new SSSOMTFormatFunction());
        setModifier(new SSSOMTListItemFunction());
        setModifier(new SSSOMTFlattenFunction());
        setModifier(new SSSOMTDefaultModifierFunction());
        setModifier(new SSSOMTReplaceModifierFunction());
        setModifier(new SimpleStringModifierFunction("upper", (s) -> s.toUpperCase()));
        setModifier(new SimpleStringModifierFunction("lower", (s) -> s.toLowerCase()));
        if ( pfxMgr != null ) {
            setModifier(new SSSOMTShortFunction(pfxMgr));
            setModifier(new SSSOMTPrefixFunction(pfxMgr));
            setModifier(new SSSOMTSuffixFunction(pfxMgr));
        }
    }

    /**
     * Formats a string with values derived from a mapping. This method finds and
     * replaces all the placeholders defined by previous calls to
     * {@link #setSubstitution(String, IMappingTransformer)}.
     * 
     * @param format  The format string containing placeholders to substitute by
     *                mapping-derived values.
     * @param mapping The mapping to format the text with.
     * @return A new string containing the substituted values (or the original
     *         string if no substitution took place).
     * 
     * @throws IllegalArgumentException If the format string contains invalid format
     *                                  specifiers.
     */
    public String format(String format, Mapping mapping) {
        IMappingTransformer<String> transformer = cache.get(format);
        if ( transformer == null ) {
            transformer = parse(format, null, null);
            cache.put(format, transformer);
        }

        return transformer.transform(mapping);
	}

    /**
     * Gets a mapping transformer that can directly create a formatted string from a
     * mapping by application of all the substitutions defined in this object.
     * 
     * @param format The format string containing placeholders to substitute by
     *               mapping-derived values.
     * @return A mapping transformer that can transform a mapping into a formatted
     *         string.
     * 
     * @throws IllegalArgumentException If the format string contains invalid format
     *                                  specifiers.
     */
    public IMappingTransformer<String> getTransformer(String format) {
        return parse(format, null, null);
    }

    /**
     * Gets a mapping transformer that can directly create a formatted string from a
     * mapping by application of all the substitutions defined in this object.
     * 
     * @param format            The format string containing placeholders to
     *                          substitute by mapping-derived values.
     * @param defaultModifier   A default modifier function to apply to all simple
     *                          (un-bracketed) placeholders. May be {@code null}.
     * @param modifierArguments Arguments to the default modifier function, if any.
     *                          May be {@code null}.
     * @return A mapping transformer that can transform a mapping into a formatted
     *         string.
     * 
     * @throws IllegalArgumentException If the format string contains invalid format
     *                                  specifiers.
     */
    public IMappingTransformer<String> getTransformer(String format, IFormatModifierFunction defaultModifier,
            List<String> modifierArguments) {
        return parse(format, defaultModifier, modifierArguments);
    }

    /*
     * Gets the transformer for a single format specifier.
     */
    private IMappingTransformer<Object> getTransformer(String name, boolean legacy) {
        IMappingTransformer<Object> transformer = null;

        // Lookup in registered placeholder patterns
        transformer = placeholders.get(name);

        // Unregistered placeholder, assume it is the name of an extension field (only
        // for a bracketed, non-legacy placeholder)
        if ( transformer == null && !legacy ) {
            transformer = (mapping) -> {
                if ( mapping.getExtensions() != null ) {
                    ExtensionValue ev = mapping.getExtensions().get(name);
                    if ( ev == null && pfxMgr != null ) {
                        ev = mapping.getExtensions().get(pfxMgr.expandIdentifier(name));
                    }
                    if ( ev != null ) {
                        return ev.toString();
                    }
                }

                // No extension with that name, return an empty string
                return "";
            };
        }

        return transformer;
    }

    /*
     * Actual parsing of the format string.
     */
    private IMappingTransformer<String> parse(String format, IFormatModifierFunction defaultModifier,
            List<String> modifierArguments) {
        int len = format.length();
        ParserState state = ParserState.PLAIN;
        StringBuilder buffer = new StringBuilder();
        FormatBuilder fb = new FormatBuilder();

        for ( int i = 0; i < len; i++ ) {
            char c = format.charAt(i);

            switch ( state ) {
            case PLAIN:
                if ( c == '%' ) {
                    state = ParserState.PERCENT;
                } else {
                    buffer.append(c);
                }
                break;

            case PERCENT:
                if ( c == '%' ) {
                    // Escaped '%', treat as plain
                    buffer.append(c);
                    state = ParserState.PLAIN;
                } else if ( c == '{' || Character.isLetter(c) ) {
                    // Add current buffer as a plain component
                    if ( buffer.length() > 0 ) {
                        fb.appendText(buffer.toString());
                        buffer.delete(0, buffer.length());
                    }
                    if ( c == '{' ) {
                        state = ParserState.PLACEHOLDER;
                    } else {
                        state = ParserState.LEGACY_PLACEHOLDER;
                        buffer.append(c);
                    }
                } else {
                    // Anything else, just treat as plain (and we need to re-insert the initial '%')
                    buffer.append('%');
                    buffer.append(c);
                    state = ParserState.PLAIN;
                }
                break;

            case LEGACY_PLACEHOLDER:
                if ( !(Character.isLetter(c) || c == '_') ) {
                    // End of a (putative) legacy placeholder
                    fb.appendLegacyTransformer(buffer.toString(), defaultModifier, modifierArguments);
                    buffer.delete(0, buffer.length());
                    buffer.append(c);
                    state = ParserState.PLAIN;
                } else {
                    buffer.append(c);
                }
                break;

            case PLACEHOLDER:
                if ( c == '}' ) {
                    fb.appendTransformer(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.PLAIN;
                } else if ( c == '|' ) {
                    fb.appendTransformer(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER;
                } else {
                    buffer.append(c);
                }
                break;

            case MODIFIER:
                if ( c == '}' ) {
                    fb.appendModifier(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.PLAIN;
                } else if ( c == '(' ) {
                    fb.appendModifier(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER_ARGUMENT_LIST;
                } else if ( c == '|' ) {
                    fb.appendModifier(buffer.toString());
                    buffer.delete(0, buffer.length());
                } else {
                    buffer.append(c);
                }
                break;

            case MODIFIER_ARGUMENT_LIST:
                if ( c == '\'' ) {
                    state = ParserState.MODIFIER_SINGLE_QUOTED_ARGUMENT;
                } else if ( c == '"' ) {
                    state = ParserState.MODIFIER_DOUBLE_QUOTED_ARGUMENT;
                } else if ( c == ')' ) {
                    state = ParserState.MODIFIER_END_ARGUMENT_LIST;
                } else if ( c != ',' && !Character.isWhitespace(c) ) {
                    buffer.append(c);
                    state = ParserState.MODIFIER_UNQUOTED_ARGUMENT;
                }
                break;

            case MODIFIER_UNQUOTED_ARGUMENT:
                if ( c == ',' ) {
                    fb.appendModifierArgument(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER_ARGUMENT_LIST;
                } else if ( c == ')' ) {
                    if ( buffer.length() > 0 ) {
                        fb.appendModifierArgument(buffer.toString());
                    }
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER_END_ARGUMENT_LIST;
                } else {
                    buffer.append(c);
                }
                break;

            case MODIFIER_SINGLE_QUOTED_ARGUMENT:
                if ( c == '\'' ) {
                    fb.appendModifierArgument(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER_POST_QUOTED_ARGUMENT;
                } else {
                    buffer.append(c);
                }
                break;

            case MODIFIER_DOUBLE_QUOTED_ARGUMENT:
                if ( c == '"' ) {
                    fb.appendModifierArgument(buffer.toString());
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER_POST_QUOTED_ARGUMENT;
                } else {
                    buffer.append(c);
                }
                break;

            case MODIFIER_POST_QUOTED_ARGUMENT:
                if ( c == ',' ) {
                    state = ParserState.MODIFIER_ARGUMENT_LIST;
                } else if ( c == ')' ) {
                    state = ParserState.MODIFIER_END_ARGUMENT_LIST;
                }
                break;

            case MODIFIER_END_ARGUMENT_LIST:
                if ( c == '}' ) {
                    state = ParserState.PLAIN;
                } else if ( c == '|' ) {
                    state = ParserState.MODIFIER;
                } else if ( !Character.isWhitespace(c) ) {
                    throw new IllegalArgumentException("Extra text after modifier call");
                }
                break;
            }
        }

        // Deal with last component that may still be in the buffer
        if ( buffer.length() > 0 ) {
            if ( state == ParserState.LEGACY_PLACEHOLDER ) {
                fb.appendLegacyTransformer(buffer.toString(), defaultModifier, modifierArguments);
            } else if ( state == ParserState.PLAIN ) {
                fb.appendText(buffer.toString());
            } else if ( state == ParserState.PERCENT ) {
                // String terminated on '%'
                buffer.append('%');
                fb.appendText(buffer.toString());
            } else {
                // Format string ended in the middle of a bracketed placeholder
                throw new IllegalArgumentException("Unterminated placeholder in format string");
            }
        } else if ( state != ParserState.PLAIN ) {
            throw new IllegalArgumentException("Unterminated placeholder in format string");
        }

        fb.validate();

        return fb;
    }

    /* Used to keep track of the current state when parsing a format string. */
    private enum ParserState {
        PLAIN,
        PERCENT,
        LEGACY_PLACEHOLDER,
        PLACEHOLDER,
        MODIFIER,
        MODIFIER_ARGUMENT_LIST,
        MODIFIER_UNQUOTED_ARGUMENT,
        MODIFIER_SINGLE_QUOTED_ARGUMENT,
        MODIFIER_DOUBLE_QUOTED_ARGUMENT,
        MODIFIER_POST_QUOTED_ARGUMENT,
        MODIFIER_END_ARGUMENT_LIST
    }

    /*
     * Helper classes to represent the elements that make up a format string.
     */

    /*
     * A call to a modifier function with its arguments.
     */
    private class Modifier {
        IFormatModifierFunction function;
        List<String> arguments;

        Modifier(IFormatModifierFunction modifier) {
            function = modifier;
            arguments = new ArrayList<String>();
        }
    }

    /*
     * A building block of a format string. Either a text fragment that should be
     * produced verbatim, or a transformer that must be invoked to obtain the text
     * to produce.
     */
    private class Component {
        String text;
        IMappingTransformer<Object> transformer;
        List<Modifier> modifiersList;
        int nModifiers = 0;

        /*
         * Creates a text component.
         */
        Component(String text) {
            this.text = text;
        }

        /*
         * Creates a transformer component.
         */
        Component(IMappingTransformer<Object> transformer) {
            this.transformer = transformer;
            modifiersList = new ArrayList<Modifier>();
        }

        /*
         * Adds a modifier to the last added component.
         */
        void addModifier(IFormatModifierFunction modifier) {
            if ( modifiersList == null ) {
                throw new AssertionError("Parser error: modifier added without a transformer");
            }
            modifiersList.add(new Modifier(modifier));
            nModifiers += 1;
        }

        /*
         * Adds an argument to the last added modifier.
         */
        void addModifierArgument(String argument) {
            if ( modifiersList == null || nModifiers > modifiersList.size() ) {
                throw new AssertionError("Parser error: argument added without a modifier");
            }
            modifiersList.get(nModifiers - 1).arguments.add(argument);
        }
    }

    /*
     * Represents the "compiled" version of a format string, as a list of format
     * components.
     */
    private class FormatBuilder implements IMappingTransformer<String> {
        List<Component> components;
        int nComponents;

        /*
         * Initialises the format string.
         */
        FormatBuilder() {
            components = new ArrayList<Component>();
            nComponents = 0;
        }

        /*
         * Appends a verbatim text fragment.
         */
        void appendText(String text) {
            components.add(new Component(text));
            nComponents += 1;
        }

        /*
         * Tries appending a legacy transformer; if the provided name is not the name of
         * a valid legacy placeholder, it is added as a verbatim text component
         * (preceded with a '%' character).
         */
        void appendLegacyTransformer(String name, IFormatModifierFunction modifier, List<String> modifierArguments) {
            IMappingTransformer<Object> transformer = getTransformer(name, true);
            if ( transformer != null ) {
                Component component = new Component(transformer);
                if ( modifier != null ) {
                    component.addModifier(modifier);
                    if ( modifierArguments != null ) {
                        for ( String argument : modifierArguments ) {
                            component.addModifierArgument(argument);
                        }
                    }
                }
                components.add(component);
            } else {
                components.add(new Component("%" + name));
            }
            nComponents += 1;
        }

        /*
         * Appends a transformer component.
         */
        void appendTransformer(String name) {
            components.add(new Component(getTransformer(name, false)));
            nComponents += 1;
        }

        /*
         * Appends a modifier to the last added component.
         */
        void appendModifier(String name) {
            if ( nComponents > components.size() ) {
                throw new AssertionError("Parser error: modifier added without a component");
            }
            IFormatModifierFunction modifier = modifiers.get(name);
            if ( modifier == null ) {
                throw new IllegalArgumentException(String.format("Unknown modifier: %s", name));
            }
            components.get(nComponents - 1).addModifier(modifier);
        }

        /*
         * Appends an argument to the last added modifier.
         */
        void appendModifierArgument(String argument) {
            if ( nComponents > components.size() ) { // Should not happen
                throw new AssertionError("Parser error: argument added without a component");
            }
            components.get(nComponents - 1).addModifierArgument(argument);
        }

        /*
         * Ensure that all modifier functions called within this object are called with
         * the expected number of arguments.
         */
        void validate() {
            for ( Component c : components ) {
                if ( c.transformer != null ) {
                    for ( Modifier m : c.modifiersList ) {
                        StringBuilder sb = new StringBuilder();
                        int nArgs = m.arguments.size();
                        for ( int i = 0; i < nArgs; i++ ) {
                            sb.append('S');
                        }
                        if ( !Pattern.matches(m.function.getSignature(), sb) ) {
                            throw new IllegalArgumentException(
                                    String.format("Invalid call for function %s", m.function.getName()));
                        }
                    }
                }
            }
        }

        /*
         * Applies the format string to a mapping.
         */
        @Override
        public String transform(Mapping mapping) {

            /*
             * Avoid going through the loop below if there is only component that does not
             * call any function.
             */
            if ( components.size() == 1 ) {
                Component c = components.get(0);
                if ( c.text != null ) {
                    return c.text;
                } else if ( c.nModifiers == 0 ) {
                    Object o = c.transformer.transform(mapping);
                    return o != null ? o.toString() : "";
                }
            }

            /*
             * Builds the string by assembling all the components, calling the modifier
             * functions as needed.
             */
            StringBuilder sb = new StringBuilder();
            for ( Component component : components ) {
                if ( component.text != null ) {
                    sb.append(component.text);
                } else if ( component.modifiersList.isEmpty() ) {
                    Object value = component.transformer.transform(mapping);
                    if ( value != null ) {
                        sb.append(value.toString());
                    }
                } else {
                    Object value = component.transformer.transform(mapping);
                    if ( value == null ) {
                        value = "";
                    }

                    for ( Modifier modifier : component.modifiersList ) {
                        value = modifier.function.call(value, modifier.arguments);
                        if ( value == null ) {
                            value = "";
                        }
                    }

                    sb.append(value.toString());
                }
            }

            return sb.toString();
        }
    }
}
