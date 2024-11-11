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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Helper class to format mappings into strings.
 * <p>
 * This class is intended to facilitate the creation of strings that may contain
 * values derived from mappings, using ”format specifiers” of the form
 * <code>%{placeholder}</code>.
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

    private static final Pattern legacyPlaceholderNamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z_]+$");

    private Map<String, IMappingTransformer<String>> legacyPlaceholders = new HashMap<String, IMappingTransformer<String>>();
    private Map<String, IMappingTransformer<String>> placeholders = new HashMap<String, IMappingTransformer<String>>();
    private Map<String, IMappingTransformer<String>> cache = new HashMap<String, IMappingTransformer<String>>();
    private Map<String, Function<String, String>> modifiers = new HashMap<String, Function<String, String>>();

    /**
     * Defines a simple placeholder text to be substituted by a mapping-derived
     * value if it is preceded by a single '%' character in a format string.
     * <p>
     * This is for compatibility with earlier versions of SSSOM/T-OWL only, which
     * specifically allowed the use of a few placeholders like
     * <code>%subject_id</code>. The use of such placeholders is deprecated in
     * favour of the “bracketed” placeholders (<code>%{subject_id}</code>) that may
     * be defined using {@link #setSubstitution(String, IMappingTransformer)}.
     * <p>
     * The placeholder must comprises only letters and underscores, and cannot start
     * with an underscore.
     * 
     * @param placeholder The placeholder value to find and replace in a text.
     * @param transformer The transformer to produce the value the placeholder
     *                    should be replaced with.
     * @throws IllegalArgumentException If the placeholder name contains characters
     *                                  other than letters and underscores.
     * 
     * @deprecated Use {@link #setLegacySubstitution(String, IMappingTransformer)}
     *             instead, unless you need expansion of un-bracketed placeholders
     *             for backwards compatibility.
     */
    @Deprecated
	public void addSubstitution(String placeholder, IMappingTransformer<String> transformer) {
        // The earlier, SSSOM/T-OWL-specific implementation expected the leading '%' to
        // be supplied by the caller. For compatibility, we still allow that here, so we
        // must remove any leading '%'.
        if ( placeholder.length() > 1 && placeholder.charAt(0) == '%' ) {
            placeholder = placeholder.substring(1);
        }
        if ( !legacyPlaceholderNamePattern.matcher(placeholder).matches() ) {
            throw new IllegalArgumentException("Invalid placeholder name");
        }
        if ( legacyPlaceholders.containsKey(placeholder) ) {
            cache.clear();
        }
        legacyPlaceholders.put(placeholder, transformer);
	}

    /**
     * Defines a placeholder text to be substituted by a value derived from a
     * mapping.
     * <p>
     * The placeholder will be recognised within a format string if it is found
     * within curly brackets preceded by a single '%' character, as in
     * <code>%{placeholder}</code>. The placeholder cannot contain a '}' character
     * or a '|' character.
     * 
     * @param placeholder The placeholder value that should be substituted by a
     *                    mapping-derived value.
     * @param transformer The transformer to produce the value that should replace
     *                    the placeholder.
     * 
     * @throws IllegalArgumentException If the placeholder contains illegal
     *                                  characters.
     */
    public void setSubstitution(String placeholder, IMappingTransformer<String> transformer) {
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
        placeholders.put("author_id", (m) -> format(m.getAuthorId()));
        placeholders.put("author_label", (m) -> format(m.getAuthorLabel()));
        placeholders.put("comment", (m) -> format(m.getComment()));
        placeholders.put("confidence", (m) -> format(m.getConfidence()));
        placeholders.put("creator_id", (m) -> format(m.getCreatorId()));
        placeholders.put("creator_label", (m) -> format(m.getCreatorLabel()));
        placeholders.put("curation_rule", (m) -> format(m.getCurationRule()));
        placeholders.put("curation_rule_text", (m) -> format(m.getCurationRuleText()));
        placeholders.put("issue_tracker_item", (m) -> format(m.getIssueTrackerItem()));
        placeholders.put("license", (m) -> format(m.getLicense()));
        placeholders.put("mapping_cardinality", (m) -> format(m.getMappingCardinality()));
        placeholders.put("mapping_date", (m) -> format(m.getMappingDate()));
        placeholders.put("mapping_justification", (m) -> format(m.getMappingJustification()));
        placeholders.put("mapping_provider", (m) -> format(m.getMappingProvider()));
        placeholders.put("mapping_source", (m) -> format(m.getMappingSource()));
        placeholders.put("mapping_tool", (m) -> format(m.getMappingTool()));
        placeholders.put("mapping_tool_version", (m) -> format(m.getMappingToolVersion()));
        placeholders.put("match_string", (m) -> format(m.getMatchString()));
        placeholders.put("object_category", (m) -> format(m.getObjectCategory()));
        placeholders.put("object_id", (m) -> format(m.getObjectId()));
        placeholders.put("object_label", (m) -> format(m.getObjectLabel()));
        placeholders.put("object_match_field", (m) -> format(m.getObjectMatchField()));
        placeholders.put("object_preprocessing", (m) -> format(m.getObjectPreprocessing()));
        placeholders.put("object_source", (m) -> format(m.getObjectSource()));
        placeholders.put("object_source_version", (m) -> format(m.getObjectSourceVersion()));
        placeholders.put("object_type", (m) -> format(m.getObjectType()));
        placeholders.put("other", (m) -> format(m.getOther()));
        placeholders.put("predicate_id", (m) -> format(m.getPredicateId()));
        placeholders.put("predicate_label", (m) -> format(m.getPredicateLabel()));
        placeholders.put("predicate_modifier", (m) -> format(m.getPredicateModifier()));
        placeholders.put("publication_date", (m) -> format(m.getPublicationDate()));
        placeholders.put("reviewer_id", (m) -> format(m.getReviewerId()));
        placeholders.put("reviewer_label", (m) -> format(m.getReviewerLabel()));
        placeholders.put("see_also", (m) -> format(m.getSeeAlso()));
        placeholders.put("similarity_measure", (m) -> format(m.getSimilarityMeasure()));
        placeholders.put("similarity_score", (m) -> format(m.getSimilarityScore()));
        placeholders.put("subject_category", (m) -> format(m.getSubjectCategory()));
        placeholders.put("subject_id", (m) -> format(m.getSubjectId()));
        placeholders.put("subject_label", (m) -> format(m.getSubjectLabel()));
        placeholders.put("subject_match_field", (m) -> format(m.getSubjectMatchField()));
        placeholders.put("subject_preprocessing", (m) -> format(m.getSubjectPreprocessing()));
        placeholders.put("subject_source", (m) -> format(m.getSubjectSource()));
        placeholders.put("subject_source_version", (m) -> format(m.getSubjectSourceVersion()));
        placeholders.put("subject_type", (m) -> format(m.getSubjectType()));

        // Don't bother checking if we replaced existing substitutions, always clear the
        // cache.
        cache.clear();
    }

    /**
     * Defines a modifier that can be used to alter the value of a substitution.
     * <p>
     * In a format string, a modifier can be inserted within the brackets after the
     * name of the placeholder, separated from it by a '|' character, as in
     * <code>%{placeholder|modifier}</code>. The modifier must be a function that
     * expects a string and returns a string. It will receive the mapping-derived
     * substitution value for the placeholder, and must returned the value to
     * actually insert into the formatted string.
     * 
     * @param name     The name of the modifier function. It must not contain any
     *                 '|' or '}' character.
     * @param modifier The actual modifier function to call when substituting a
     *                 placeholder where this modifier has been used.
     * 
     * @throws IllegalArgumentException If the name contains '}' or '|' characters.
     */
    public void setModifier(String name, Function<String, String> modifier) {
        if ( name.contains("}") || name.contains("|") ) {
            throw new IllegalArgumentException("Invalid modifier name");
        }
        if ( modifiers.containsKey(name) ) {
            cache.clear();
        }
        modifiers.put(name, modifier);
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
            transformer = parse(format);
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
        return parse(format);
    }

    /*
     * Gets the transformer for a single format specifier.
     */
    private IMappingTransformer<String> getTransformer(String name, boolean legacy) {
        IMappingTransformer<String> transformer = null;

        // Lookup in registered placeholder patterns
        transformer = legacy ? legacyPlaceholders.get(name) : placeholders.get(name);

        // Unregistered placeholder, assume it is the name of an extension field (only
        // for a bracketed, non-legacy placeholder)
        if ( transformer == null && !legacy ) {
            transformer = (mapping) -> {
                if ( mapping.getExtensions() != null ) {
                    ExtensionValue ev = mapping.getExtensions().get(name);
                    if ( ev != null ) {
                        return ev.toString();
                    }
                }

                // No extension with that name, return the original format string
                return String.format("%%{%s}", name);
            };
        }

        return transformer;
    }

    /*
     * Actual parsing of the format string.
     */
    private IMappingTransformer<String> parse(String format) {
        List<IMappingTransformer<String>> components = new ArrayList<IMappingTransformer<String>>();

        int len = format.length();
        ParserState state = ParserState.PLAIN;
        StringBuilder buffer = new StringBuilder();
        String name = null;
        String modifier = null;

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
                        String component = buffer.toString();
                        components.add((mapping) -> component);
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
                    name = buffer.toString();
                    buffer.delete(0, buffer.length());
                    IMappingTransformer<String> component = getTransformer(name, true);
                    if ( component != null ) {
                        components.add(component);
                    } else {
                        // This was not a valid legacy placeholder, re-insert it into the buffer (with
                        // the initial '%') and treat as plain
                        buffer.append('%');
                        buffer.append(name);
                    }

                    buffer.append(c);
                    state = ParserState.PLAIN;
                } else {
                    buffer.append(c);
                }
                break;

            case PLACEHOLDER:
                if ( c == '}' ) {
                    name = buffer.toString();
                    buffer.delete(0, buffer.length());
                    components.add(getTransformer(name, false));
                    state = ParserState.PLAIN;
                } else if ( c == '|' ) {
                    name = buffer.toString();
                    buffer.delete(0, buffer.length());
                    state = ParserState.MODIFIER;
                } else {
                    buffer.append(c);
                }
                break;

            case MODIFIER:
                if ( c == '}' ) {
                    modifier = buffer.toString();
                    buffer.delete(0, buffer.length());
                    Function<String, String> mod = modifiers.get(modifier);
                    if ( mod == null ) {
                        throw new IllegalArgumentException(String.format("Unknown modifier: %s", modifier));
                    }
                    IMappingTransformer<String> transformer = getTransformer(name, false);
                    components.add((mapping) -> mod.apply(transformer.transform(mapping)));
                    state = ParserState.PLAIN;
                } else {
                    buffer.append(c);
                }
                break;

            }
        }

        // Deal with last component that may still be in the buffer
        if ( buffer.length() > 0 ) {
            if ( state == ParserState.LEGACY_PLACEHOLDER ) {
                IMappingTransformer<String> transformer = getTransformer(buffer.toString(), true);
                if ( transformer != null ) {
                    components.add(transformer);
                } else {
                    // Unrecognised legacy placeholder, re-insert initial '%' and treat as plain
                    buffer.insert(0, '%');
                    String last = buffer.toString();
                    components.add((mapping) -> last);
                }
            } else if ( state == ParserState.PLAIN ) {
                String last = buffer.toString();
                components.add((mapping) -> last);
            } else if ( state == ParserState.PERCENT ) {
                // String terminated on '%'
                buffer.append('%');
                String last = buffer.toString();
                components.add((mapping) -> last);
            } else {
                // Format string ended in the middle of a bracketed placeholder
                throw new IllegalArgumentException("Unterminated placeholder in format string");
            }
        }

        // Assemble the final transformer object
        IMappingTransformer<String> transformer;
        if ( components.size() == 1 ) {
            transformer = components.get(0);
        } else {
            transformer = (mapping) -> {
                StringBuilder sb = new StringBuilder();
                for ( IMappingTransformer<String> component : components ) {
                    sb.append(component.transform(mapping));
                }
                return sb.toString();
            };
        }

        return transformer;
    }

    /*
     * Helper formatting functions used by the standard substitutions.
     */

    private String format(String s) {
        return s != null ? s : "";
    }

    private String format(Double v) {
        if ( v == null ) {
            return "";
        }
        DecimalFormat fmt = new DecimalFormat("#.##");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(v);
    }

    private String format(List<String> l) {
        return l != null ? String.join("|", l) : "";
    }

    private String format(Object o) {
        return o != null ? o.toString() : "";
    }

    /* Used to keep track of the current state when parsing a format string. */
    private enum ParserState {
        PLAIN,
        PERCENT,
        LEGACY_PLACEHOLDER,
        PLACEHOLDER,
        MODIFIER
    }
}
