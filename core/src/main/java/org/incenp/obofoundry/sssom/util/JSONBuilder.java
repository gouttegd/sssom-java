/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.util;

import java.util.ArrayDeque;
import java.util.Deque;

import org.incenp.obofoundry.sssom.SSSOMUtils;

/**
 * A helper class to assemble a JSON string.
 */
public class JSONBuilder {

    private final static String INDENT = "  ";

    private StringBuilder buffer = new StringBuilder();
    private Deque<JsonStruct> structs = new ArrayDeque<>();
    boolean firstItem = false;

    /**
     * Starts a new dictionary at the current position.
     */
    public void startDict() {
        start(JsonStruct.DICT);
    }

    /**
     * Closes the current dictionary.
     * <p>
     * This is a no-op if the current structure is not, in fact, a dictionary.
     */
    public void endDict() {
        JsonStruct s = structs.pollFirst();
        if ( s == JsonStruct.DICT ) {
            end(s);
        }
    }

    /**
     * Starts a new list at the current position.
     */
    public void startList() {
        start(JsonStruct.LIST);
    }

    /**
     * Closes the current list.
     * <p>
     * This is a no-op if the current structure is not, in fact, a list.
     */
    public void endList() {
        JsonStruct s = structs.pollFirst();
        if ( s == JsonStruct.LIST ) {
            end(s);
        }
    }

    /**
     * Closes the current list or dictionary, whichever it is.
     */
    public void end() {
        JsonStruct s = structs.pollFirst();
        if ( s != null ) {
            end(s);
        }
    }

    /**
     * Adds a key in the current dictionary.
     * <p>
     * If the current structure is not already a dictionary, a new dictionary is
     * automatically started.
     * 
     * @param key The name of the key to add.
     */
    public void addKey(String key) {
        if ( structs.peekFirst() != JsonStruct.DICT ) {
            start(JsonStruct.DICT);
        } else if ( !firstItem ) {
            buffer.append(',');
        }
        indent();
        buffer.append('"');
        buffer.append(key);
        buffer.append("\": ");
        firstItem = false;
    }

    /**
     * Adds a string value at the current position.
     * 
     * @param value The value to add.
     */
    public void addValue(String value) {
        maybeAddItem();
        buffer.append('"');
        // Escaping string value according to JSON rules
        // (https://www.ietf.org/rfc/rfc4627.html#section-2.5)
        for ( int i = 0, n = value.length(); i < n; i++ ) {
            int c = value.codePointAt(i);
            switch ( c ) {
            case '"':
            case '\\':
                buffer.append('\\');
                buffer.append(Character.toString(c));
                break;

            case 0x08: // Backspace
                buffer.append("\\b");
                break;

            case 0x09: // Horizontal tab
                buffer.append("\\t");
                break;

            case 0x0A: // Line feed
                buffer.append("\\n");
                break;

            case 0x0C: // Form feed
                buffer.append("\\f");
                break;

            case 0x0D: // Carriage return
                buffer.append("\\r");
                break;

            default:
                if ( c <= 0x1F ) {
                    buffer.append(String.format("\\u%04x", c));
                } else {
                    buffer.append(Character.toString(c));
                }
            }
        }
        buffer.append('"');
    }

    /**
     * Adds a boolean value at the current position.
     * 
     * @param value The value to add.
     */
    public void addValue(boolean value) {
        maybeAddItem();
        buffer.append(value ? "true" : "false");
    }

    /**
     * Adds a floating point value at the current position.
     * 
     * @param value The value to add.
     */
    public void addValue(Double value) {
        maybeAddItem();
        buffer.append(SSSOMUtils.format(value));
    }

    /**
     * Adds an arbitrary value at the current position.
     * 
     * @param value The value to add.
     */
    public void addValue(Object value) {
        maybeAddItem();
        buffer.append(value.toString());
    }

    /**
     * Closes all open structures and returns the resulting JSON string.
     * <p>
     * This automatically clears the builder, which can then be reused to build a
     * new JSON string.
     * 
     * @return The assembled JSON string.
     */
    public String close() {
        JsonStruct s = null;
        while ( (s = structs.pollFirst()) != null ) {
            end(s);
        }
        buffer.append('\n');

        String json = buffer.toString();
        buffer.delete(0, buffer.length());
        return json;
    }

    private void start(JsonStruct struct) {
        maybeAddItem();
        structs.addFirst(struct);
        buffer.append(struct.openChar);
        firstItem = true;
    }

    private void end(JsonStruct struct) {
        if ( !firstItem ) {
            indent();
        }
        buffer.append(struct.closeChar);
    }

    private void maybeAddItem() {
        if ( structs.peekFirst() == JsonStruct.LIST ) {
            if ( !firstItem ) {
                buffer.append(',');
            }
            indent();
            firstItem = false;
        }
    }

    private void indent() {
        buffer.append('\n');
        buffer.append(INDENT.repeat(structs.size()));
    }

    private enum JsonStruct {
        LIST('[', ']'),
        DICT('{', '}');

        char openChar;
        char closeChar;

        JsonStruct(char open, char close) {
            openChar = open;
            closeChar = close;
        }
    }
}
