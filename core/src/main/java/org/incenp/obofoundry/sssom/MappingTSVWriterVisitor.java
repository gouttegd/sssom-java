/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024,2026 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.DateSlot;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityReferenceSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;

/**
 * A visitor to to turn the slots of a {@link Mapping} object into a list of
 * values that can be written to a TSV or CSV file.
 * <p>
 * This class is primarily intended to be used by {@link TSVWriter}, but it may
 * be reused whenever mappings need to be serialised into TSV or CSV.
 * <p>
 * After the slots of a {@link Mapping} object have been visited, use the
 * {@link #getValues()} methods to obtain a list of string values ready to be
 * written to a TSV/CSV files. The values will be in the same order as the order
 * in which the slots have been visited.
 */
public class MappingTSVWriterVisitor extends SlotVisitorBase<Mapping> {

    private PrefixManager prefixManager;
    private List<ExtensionDefinition> definitions;
    private List<String> cellValues;
    private boolean isCSV;

    /**
     * Creates a new instance.
     * 
     * @param prefixManager The prefix manager to use to shorten entity references
     *                      into their CURIE forms.
     * @param extraSlots    The definitions for the extension slots; if
     *                      <code>null</code>, any extension slot will be ignored.
     * @param isCSV         If <code>true</code>, the values will be generated so
     *                      that they can written to a CSV file. The default is to
     *                      prepare values suitable for writing to a TSV file.
     */
    public MappingTSVWriterVisitor(PrefixManager prefixManager, List<ExtensionDefinition> extraSlots, boolean isCSV) {
        this.prefixManager = prefixManager;
        definitions = extraSlots;
        cellValues = new ArrayList<>();
        this.isCSV = isCSV;
    }

    /**
     * Gets the values ready for writing.
     * 
     * @return The list of strings representing the values for all the slots of the
     *         visited {@link Mapping}.
     */
    public List<String> getValues() {
        return cellValues;
    }

    /**
     * Prepare this object for visiting a new {@link Mapping} object.
     */
    public void reset() {
        cellValues.clear();
    }

    @Override
    public void visit(Slot<Mapping> slot, Mapping object, Object value) {
        cellValues.add(value == null ? "" : escapeTSV(value.toString()));
    }

    @Override
    public void visit(EntityReferenceSlot<Mapping> slot, Mapping object, String value) {
        cellValues.add(value == null ? "" : escapeTSV(prefixManager.shortenIdentifier(value)));
    }

    @Override
    public void visit(StringSlot<Mapping> slot, Mapping object, List<String> values) {
        if ( values == null ) {
            cellValues.add("");
            return;
        }
        cellValues.add(escapeTSV(values));
    }

    @Override
    public void visit(EntityReferenceSlot<Mapping> slot, Mapping object, List<String> values) {
        if ( values == null ) {
            cellValues.add("");
            return;
        }
        cellValues.add(escapeTSV(prefixManager.shortenIdentifiers(values)));
    }

    @Override
    public void visit(DoubleSlot<Mapping> slot, Mapping object, Double value) {
        cellValues.add(value == null ? "" : SSSOMUtils.format(value));
    }

    @Override
    public void visit(DateSlot<Mapping> slot, Mapping object, LocalDate value) {
        cellValues.add(value == null ? "" : value.format(DateTimeFormatter.ISO_DATE));
    }

    @Override
    public void visit(ExtensionSlot<Mapping> slot, Mapping object, Map<String, ExtensionValue> extensions) {
        if ( definitions == null || extensions == null ) {
            return;
        }

        for ( ExtensionDefinition definition : definitions ) {
            ExtensionValue ev = extensions.get(definition.getProperty());
            if ( ev != null ) {
                cellValues.add(ev.isIdentifier() ? prefixManager.shortenIdentifier(ev.asString()) : ev.toString());
            } else {
                cellValues.add("");
            }
        }
    }

    /*
     * Apply CSV-style escaping rules
     * https://datatracker.ietf.org/doc/html/rfc4180#section-2
     */
    private String escapeTSV(String value) {
        StringBuilder sb = new StringBuilder();
        int len = value.length();
        boolean quotesNeeded = false;
        for ( int i = 0; i < len; i++ ) {
            char c = value.charAt(i);
            switch ( c ) {
            case ',':
                if ( isCSV ) {
                    quotesNeeded = true;
                }
                break;

            case '\t':
                if ( !isCSV ) {
                    quotesNeeded = true;
                }
                break;

            case '\n':
            case '\r':
                quotesNeeded = true;
                break;

            case '"':
                quotesNeeded = true;
                sb.append('"');
                break;
            }
            sb.append(c);
        }

        if ( quotesNeeded ) {
            sb.insert(0, '"');
            sb.append('"');
        }

        return sb.toString();
    }

    /*
     * Likewise, but for multi-valued slots, where in addition we need to escape
     * pipe characters. The duplicated code from the previous method is unfortunate,
     * but we can't simply call that method because we don't want to quote
     * <em>individual</em> values here, it is the entire |-separated multivalue that
     * must be quoted if any single value within it contains quote-triggering
     * characters.
     */
    private String escapeTSV(List<String> values) {
        StringBuilder sb = new StringBuilder();
        boolean quotesNeeded = false;
        int nValues = values.size();
        for ( int i = 0; i < nValues; i++ ) {
            String value = values.get(i);
            if ( i > 0 ) {
                sb.append('|');
            }

            int len = value.length();
            for ( int j = 0; j < len; j++ ) {
                char c = value.charAt(j);
                switch ( c ) {
                case ',':
                    if ( isCSV ) {
                        quotesNeeded = true;
                    }
                    break;

                case '\t':
                    if ( !isCSV ) {
                        quotesNeeded = true;
                    }
                    break;

                case '\r':
                case '\n':
                    quotesNeeded = true;
                    break;

                case '"':
                    quotesNeeded = true;
                    sb.append('"');
                    break;

                case '\\':
                    // The backslash needs escaping only if (1) it is followed by another backslash
                    // or a pipe, or (2) it is the last character of the current value and there are
                    // more values to follow.
                    if ( j < len - 1 ) {
                        char next = value.charAt(j + 1);
                        if ( next == '\\' || next == '|' ) {
                            sb.append('\\');
                        }
                    } else if ( i < nValues - 1 ) {
                        sb.append('\\');
                    }
                    break;

                case '|':
                    sb.append('\\');
                    break;
                }
                sb.append(c);
            }
        }

        if ( quotesNeeded ) {
            sb.insert(0, '"');
            sb.append('"');
        }

        return sb.toString();
    }
}
