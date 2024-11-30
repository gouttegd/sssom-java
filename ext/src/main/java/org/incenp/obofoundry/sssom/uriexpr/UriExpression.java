/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.uriexpr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.PrefixManager;

/**
 * Represents a “URI Expression ID”, a way to encode composed entities in a URI.
 * 
 * @see <a href=
 *      "https://github.com/monarch-initiative/uri-expression-language">The URI
 *      Expression Language proposal</a>.
 */
public class UriExpression {

    private String schema;

    private Map<String, String> components;

    /**
     * Creates a new blank expression in the given schema.
     * <p>
     * This constructor is mostly intended for internal use. Client code is expected
     * to obtain a UriExpression object by parsing a string using
     * {@link #parse(String, PrefixManager)}.
     * 
     * @param schema The schema this expression conforms to.
     */
    public UriExpression(String schema) {
        this.schema = schema;
        components = new HashMap<String, String>();
    }

    /**
     * Gets the schema part of this expression.
     * 
     * @return The schema.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Inserts a new component into this expression.
     * <p>
     * This is mostly intended for internal use. Client code should refrain from
     * modifying a URI Expression after it has been parsed.
     * 
     * @param key   The name of the slot.
     * @param value The value of the slot.
     */
    public void putComponent(String key, String value) {
        components.put(key, value);
    }

    /**
     * Gets the value of the given slot in this expression.
     * 
     * @param key The name of the slot to retrieve.
     * @return The value associated with that slot, or {@code null} if the
     *         expression has no such slot.
     */
    public String getComponent(String key) {
        return components.get(key);
    }

    /**
     * Gets the name of all slots present in this expression.
     * 
     * @return The slot names.
     */
    public Set<String> getComponentNames() {
        return components.keySet();
    }

    /**
     * Turns this object back to a string representation.
     * 
     * @param prefixManager The prefix manager to use for condensing slot values
     *                      into CURIEs.
     * @return A string containing the encoded expression.
     */
    public String toURI(PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(schema);
        sb.append("/(");

        List<String> keys = new ArrayList<String>(components.keySet());
        keys.sort((s1, s2) -> s1.compareTo(s2));
        boolean isFirst = true;
        for ( String key : keys ) {
            if ( !isFirst ) {
                sb.append(',');
            } else {
                isFirst = false;
            }
            sb.append(key);
            sb.append(":'");
            sb.append(prefixManager.shortenIdentifier(components.get(key)));
            sb.append("'");
        }
        sb.append(')');

        return sb.toString();
    }

    /**
     * Parses a string into a URI Expression object.
     * 
     * @param uri           The string to parse.
     * @param prefixManager The prefix manager to use to expand CURIEs found within
     *                      the expression.
     * @return The parsed expression, or {@code null} if the string does not contain
     *         a valid URI Expression.
     */
    public static UriExpression parse(String uri, PrefixManager prefixManager) {
        int lastSlash = uri.lastIndexOf('/');
        if ( lastSlash == -1 || lastSlash == uri.length() - 1 ) {
            // No slash (is that even a URI?) or nothing after the schema part, it is not a
            // URI Expression.
            return null;
        }

        UriExpression expr = new UriExpression(uri.substring(0, lastSlash));
        String rest = uri.substring(lastSlash + 1);

        // First try Base64 decoding
        if ( rest.charAt(0) != '(' ) {
            try {
                byte[] b64 = Base64.getDecoder().decode(rest);
                rest = new String(b64, StandardCharsets.UTF_8);
            } catch ( IllegalArgumentException iae ) {
                return null;
            }
        }

        // If we still do not have the beginning of a JSON object, this is not a URI
        // Expression.
        if ( rest.charAt(0) != '(' ) {
            return null;
        }

        /*
         * Actual parsing. For now, we are assuming that Expression URIs can only
         * contain a small subset of JSON-URL, with a single top-level dictionary
         * containing only string values. Therefore, we can get away with a small,
         * ad-hoc parser in which we error out at the first hint that the expression
         * does not match what we expect.
         * 
         * Should it be decided instead that URI Expressions can be as complex as
         * JSON-URL allows, we will then need to switch to using a real JSON-URL parser
         * (e.g. <https://github.com/jsonurl/jsonurl-java>).
         * 
         * See <https://github.com/monarch-initiative/uri-expression-language/issues/4>
         * for the discussion about what exactly should be allowed in a URI Expression.
         */

        int len = rest.length();
        ParserState state = ParserState.INIT;
        StringBuffer buffer = new StringBuffer();
        String currentKey = null;
        String currentValue = null;

        for ( int i = 0; i < len; i++ ) {
            char c = rest.charAt(i);

            switch ( state ) {
            case INIT:
                if ( c == '(' ) {
                    state = ParserState.OPEN_PAREN;
                } else {
                    return null;
                }
                break;

            case OPEN_PAREN:
                if ( Character.isLetter(c) ) {
                    state = ParserState.KEY;
                    buffer.append(c);
                } else {
                    return null;
                }
                break;

            case KEY:
                if ( c == ':' ) {
                    state = ParserState.VALUE;
                    currentKey = buffer.toString();
                    buffer.delete(0, buffer.length());
                } else if ( Character.isLetterOrDigit(c) || c == '_' ) {
                    buffer.append(c);
                } else {
                    return null;
                }
                break;

            case VALUE:
                if ( c == '\'' ) {
                    state = ParserState.QUOTED_VALUE;
                } else if ( c == ',' ) {
                    currentValue = buffer.toString();
                    buffer.delete(0, buffer.length());
                    expr.putComponent(currentKey, prefixManager.expandIdentifier(currentValue));
                } else if ( c == ')' ) {
                    currentValue = buffer.toString();
                    buffer.delete(0, buffer.length());
                    expr.putComponent(currentKey, prefixManager.expandIdentifier(currentValue));
                    state = ParserState.CLOSE_PAREN;
                } else {
                    buffer.append(c);
                }
                break;

            case QUOTED_VALUE:
                if ( c == '\'' ) {
                    currentValue = buffer.toString();
                    buffer.delete(0, buffer.length());
                    expr.putComponent(currentKey, prefixManager.expandIdentifier(currentValue));
                    state = ParserState.CLOSE_QUOTE;
                } else {
                    buffer.append(c);
                }
                break;

            case CLOSE_QUOTE:
                if ( c == ',' ) {
                    state = ParserState.KEY;
                } else if ( c == ')' ) {
                    state = ParserState.CLOSE_PAREN;
                } else {
                    return null;
                }
                break;

            case CLOSE_PAREN:
                // We ignore any trailing stuff
                break;
            }
        }

        if ( state != ParserState.CLOSE_PAREN ) {
            return null;
        }

        return expr;
    }

    private enum ParserState {
        INIT,
        OPEN_PAREN,
        CLOSE_PAREN,
        KEY,
        VALUE,
        CLOSE_QUOTE,
        QUOTED_VALUE
    }
}
