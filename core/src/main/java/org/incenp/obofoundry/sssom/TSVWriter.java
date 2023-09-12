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

package org.incenp.obofoundry.sssom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A writer to serialise a SSSOM mapping set into the TSV format. For now, only
 * the “embedded metadata” variant is supported.
 * <p>
 * If the mapping set has a CURIE map ({@link MappingSet#getCurieMap()}), it is
 * automatically used to shorten identifiers when they are written to the file.
 * <p>
 * Usage:
 * 
 * <pre>
 * MappingSet mappingSet = ...;
 * try {
 *     TSVWriter writer = new TSVWriter("my-mappings.sssom.tsv");
 *     writer.write(mappingSet);
 *     writer.close();
 * } catch ( IOException ioe ) {
 *     // Generic I/O error
 * }
 * </pre>
 */
public class TSVWriter {

    private BufferedWriter writer;
    private PrefixManager prefixManager = new PrefixManager();

    /**
     * Creates a new instance that will write data to the specified file.
     * 
     * @param file The file to write to.
     * @throws IOException If the file cannot be opened for any reason.
     */
    public TSVWriter(File file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    /**
     * Creates a new instance that will write data to a file with the specified
     * filename.
     * 
     * @param filename The name of the file to write to.
     * @throws IOException If the file cannot be opened for any reason.
     */
    public TSVWriter(String filename) throws IOException {
        this(new File(filename));
    }

    /**
     * Serialises a mapping set into the underlying file.
     * 
     * @param mappingSet The mapping set to serialise.
     * @throws IOException If an I/O error occurs.
     */
    public void write(MappingSet mappingSet) throws IOException {
        prefixManager.add(mappingSet.getCurieMap());

        // Write the metadata
        // FIXME: Support writing them in a separate file
        SlotHelper<MappingSet> setHelper = SlotHelper.getMappingSetHelper(true);
        setHelper.setAlphabeticalOrder();
        for ( String meta : setHelper.visitSlots(mappingSet, new MappingSetSlotVisitor(prefixManager)) ) {
            writer.append(meta);
        }

        // Find the used slots
        SlotHelper<Mapping> helper = SlotHelper.getMappingHelper(true);
        MappingSlotUsageVisitor usageVisitor = new MappingSlotUsageVisitor();
        Set<String> usedSlotNames = new HashSet<String>();
        for ( Mapping mapping : mappingSet.getMappings() ) {
            usedSlotNames.addAll(helper.visitSlots(mapping, usageVisitor));
        }

        // Only visit those slots. We need to explicitly set the list of slots to visit
        // in case some slots are empty in some mappings but not in others.
        helper.setSlots(new ArrayList<String>(usedSlotNames), false);

        // Write the column headers
        List<Slot<Mapping>> usedSlots = helper.getSlots();
        for ( int i = 0, n = usedSlots.size(); i < n; i++ ) {
            writer.append(usedSlots.get(i).getName());
            if ( i < n - 1 ) {
                writer.append('\t');
            }
        }
        writer.append('\n');

        // Write the individual mappings
        MappingSlotVisitor mappingVisitor = new MappingSlotVisitor(prefixManager);
        mappingSet.getMappings().sort(new DefaultMappingComparator());
        for ( Mapping mapping : mappingSet.getMappings() ) {
            List<String> values = helper.visitSlots(mapping, mappingVisitor, true);
            for ( int i = 0, n = values.size(); i < n; i++ ) {
                writer.append(values.get(i));
                if ( i < n - 1 ) {
                    writer.append('\t');
                }
            }

            writer.append('\n');
        }

        writer.close();
    }

    /*
     * Visits all slots in a MappingSet to get the lines that will make up the
     * metadata block.
     */
    private class MappingSetSlotVisitor extends SlotVisitorBase<MappingSet, String> {
        PrefixManager pm;

        MappingSetSlotVisitor(PrefixManager prefixManager) {
            pm = prefixManager;
        }

        @Override
        public String visit(Slot<MappingSet> slot, MappingSet set, String value) {
            return String.format("#%s: \"%s\"\n", slot.getName(),
                    slot.isEntityReference() ? pm.shortenIdentifier(value) : value);
        }

        @Override
        public String visit(Slot<MappingSet> slot, MappingSet set, List<String> values) {
            if ( values.size() > 0 ) {
                StringBuilder sb = new StringBuilder();
                sb.append("#");
                sb.append(slot.getName());
                sb.append(":\n");
                for ( String value : values ) {
                    sb.append("#  - \"");
                    sb.append(slot.isEntityReference() ? pm.shortenIdentifier(value) : value);
                    sb.append("\"\n");
                }
                return sb.toString();
            }
            return null;
        }

        @Override
        public String visit(Slot<MappingSet> slot, MappingSet set, Map<String, String> values) {
            if ( values.size() > 0 ) {
                StringBuilder sb = new StringBuilder();
                sb.append("#");
                sb.append(slot.getName());
                sb.append(":\n");
                for ( String key : values.keySet() ) {
                    sb.append("#  ");
                    sb.append(key);
                    sb.append(": \"");
                    sb.append(values.get(key));
                    sb.append("\"\n");
                }
                return sb.toString();
            }
            return null;
        }

        @Override
        public String visit(Slot<MappingSet> slot, MappingSet set, Double value) {
            return String.format("%f", value);
        }

        @Override
        public String visit(Slot<MappingSet> slot, MappingSet set, LocalDate value) {
            // The SSSOM specification says nothing on how to serialise dates, but LinkML
            // says “for xsd dates, datetimes, and times, AtomicValue must be a string
            // conforming to the relevant ISO type”. I assume this means ISO-8601.
            return String.format("#%s: \"%s\"\n", slot.getName(), value.format(DateTimeFormatter.ISO_DATE));
        }

        @Override
        public String getDefault(Slot<MappingSet> slot, MappingSet set, Object value) {
            return value != null ? value.toString() : "";
        }
    }

    /*
     * Visits all slots in a mapping to render their values as a TSV cell.
     */
    private class MappingSlotVisitor extends SlotVisitorBase<Mapping, String> {
        private PrefixManager pm;

        MappingSlotVisitor(PrefixManager prefixManager) {
            pm = prefixManager;
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, String value) {
            if ( value == null ) {
                return "";
            } else if ( slot.isEntityReference() ) {
                return pm.shortenIdentifier(value);
            } else {
                return value;
            }
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, List<String> values) {
            if ( values == null ) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for ( int i = 0, n = values.size(); i < n; i++ ) {
                String value = values.get(i);
                sb.append(slot.isEntityReference() ? pm.shortenIdentifier(value) : value);
                if ( i < n - 1 ) {
                    sb.append('|');
                }
            }
            return sb.toString();
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, Double value) {
            if ( value == null ) {
                return "";
            } else {
                return String.format("%f", value);
            }
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, LocalDate value) {
            if ( value == null ) {
                return "";
            } else {
                return value.format(DateTimeFormatter.ISO_DATE);
            }
        }

        @Override
        protected String getDefault(Slot<Mapping> slot, Mapping object, Object value) {
            return value != null ? value.toString() : "";
        }
    }

    /*
     * Visits non-null slots to get a list of all slots that do have a value.
     */
    private class MappingSlotUsageVisitor extends SlotVisitorBase<Mapping, String> {
        @Override
        protected String getDefault(Slot<Mapping> slot, Mapping object, Object value) {
            return slot.getName();
        }
    }
}
