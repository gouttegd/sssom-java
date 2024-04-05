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

import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Helper class to format mappings into strings.
 * <p>
 * This class is intended to facilitate the creation of strings that may contain
 * values derived from mappings.
 * <p>
 * For example, this initialises a formatter that can substitute
 * {@code $predicate} and {@code justification} in a string by the predicate
 * identifier and the justification of a mapping, respectively:
 * 
 * <pre>
 * MappingFormatter formatter = new MappingFormatter();
 * formatter.addSubstitution("$predicate", (mapping) -&gt; mapping.getPredicateId());
 * formatter.addSubstitution("$justification", (mapping) -$gt; mapping.getMappingJustification());
 * </pre>
 * <p>
 * That formatter may then be used as follows:
 * 
 * <pre>
 * String text = formatter.format("The mapping predicate is '$predicate', the justification is '$justification'",
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
 *         .getTransformer("The mapping predicate is '$predicate', the justification is '$justification'");
 * String text = transformer.transform(mapping);
 * </pre>
 */
public class MappingFormatter {
	private Map<String, IMappingTransformer<String>> patterns = new HashMap<String, IMappingTransformer<String>>();

    /**
     * Defines a placeholder text to be substituted by a value derived from a
     * mapping.
     * 
     * @param placeholder The placeholder value to find and replace in a text.
     * @param transformer The transformer to produce the value the placeholder
     *                    should be replaced with.
     */
	public void addSubstitution(String placeholder, IMappingTransformer<String> transformer) {
		patterns.put(placeholder, transformer);
	}

    /**
     * Formats a string with values derived from a mapping. This method finds and
     * replaces all the placeholders defined by previous calls to
     * {@link #addSubstitution(String, IMappingTransformer)}.
     * 
     * @param text    The text whose placeholders (if any) should be substituted by
     *                mapping-derived values.
     * @param mapping The mapping to format the text with.
     * @return A new string containing the substituted values (or the original
     *         string if no substitution took place).
     */
	public String format(String text, Mapping mapping) {
        for ( String pattern : patterns.keySet() ) {
            if ( text.contains(pattern) ) {
                IMappingTransformer<String> trans = patterns.get(pattern);
                String substitution = trans.transform(mapping);
                if ( substitution != null ) {
                    text = text.replace(pattern, trans.transform(mapping));
                }
            }
		}

        return text;
	}

    /**
     * Gets a mapping transformer that can directly create a formatted string from a
     * mapping by application of all the substitutions defined in this object.
     * 
     * @param text The text whose placeholders (if any) should be substituted by
     *             mapping-derived values.
     * @return A mapping transformer that can transform a mapping into a formatted
     *         string.
     */
    public IMappingTransformer<String> getTransformer(String text) {
        return (mapping) -> format(text, mapping);
    }
}
