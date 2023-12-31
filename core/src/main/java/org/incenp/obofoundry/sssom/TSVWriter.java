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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
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
 * } catch ( IOException ioe ) {
 *     // Generic I/O error
 * }
 * </pre>
 */
public class TSVWriter {

    private BufferedWriter writer;
    private PrefixManager prefixManager = new PrefixManager();
    private Set<String> usedPrefixes = new HashSet<String>();
    private boolean customMap = false;
    private ExtraMetadataPolicy extraPolicy = ExtraMetadataPolicy.NONE;

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
     * Creates a new instance that will write data to the specified stream.
     * 
     * @param stream The stream to write to.
     */
    public TSVWriter(OutputStream stream) {
        writer = new BufferedWriter(new OutputStreamWriter(stream));
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
     * Sets the Curie map to use to shorten identifiers. The Curie map associated
     * with the mapping set ({@link MappingSet#getCurieMap()}) will then be
     * completely ignored in favour of the specified map.
     * 
     * @param map A map associating prefix names to prefixes.
     */
    public void setCurieMap(Map<String, String> map) {
        prefixManager.add(map);
        customMap = true;
    }

    /**
     * Sets the policy to deal with non-standard metadata in the mapping set to
     * write.
     * 
     * @param policy The policy instructing the writer about what to do with any
     *               non-standard metadata. The default policy is
     *               {@link ExtraMetadataPolicy#NONE}, meaning that no non-standard
     *               metadata is ever written.
     */
    public void setExtraMetadataPolicy(ExtraMetadataPolicy policy) {
        extraPolicy = policy;
    }

    /**
     * Serialises a mapping set into the underlying file.
     * 
     * @param mappingSet The mapping set to serialise.
     * @throws IOException If an I/O error occurs.
     */
    public void write(MappingSet mappingSet) throws IOException {
        if ( !customMap ) {
            prefixManager.add(mappingSet.getCurieMap());
        }

        // The "license" slot MUST be present.
        if ( mappingSet.getLicense() == null || mappingSet.getLicense().isEmpty() ) {
            mappingSet.setLicense("https://w3id.org/sssom/license/all-rights-reserved");
        }

        // Ditto for the mapping set ID.
        if ( mappingSet.getMappingSetId() == null || mappingSet.getMappingSetId().isEmpty() ) {
            mappingSet.setMappingSetId("http://sssom.invalid/" + UUID.randomUUID().toString());
        }

        // Find out which prefixes are actually needed
        SlotHelper.getMappingSetHelper().visitSlots(mappingSet, new MappingSetPrefixVisitor());
        MappingPrefixVisitor prefixVisitor = new MappingPrefixVisitor();
        mappingSet.getMappings().forEach(m -> SlotHelper.getMappingHelper().visitSlots(m, prefixVisitor));
        for ( BuiltinPrefix bp : BuiltinPrefix.values() ) {
            usedPrefixes.remove(bp.getPrefixName());
        }

        // Condense the set
        Set<String> condensedSlots = new SlotPropagator(PropagationPolicy.NeverReplace).condense(mappingSet, true);

        // Figure out if we need to write extra columns
        List<String> extraColumnNames = new ArrayList<String>();
        Set<String> extraColumnNameSet = new HashSet<String>();
        mappingSet.setExtraColumns(null); // Ignore the provided list
        if ( extraPolicy != ExtraMetadataPolicy.NONE ) {
            for ( Mapping mapping : mappingSet.getMappings() ) {
                if ( mapping.getExtraMetadata() != null ) {
                    for ( String key : mapping.getExtraMetadata().keySet() ) {
                        extraColumnNameSet.add(key);
                    }
                }
            }
            if ( !extraColumnNameSet.isEmpty() ) {
                if ( extraPolicy == ExtraMetadataPolicy.DECLARED ) {
                    mappingSet.setExtraColumns(new ArrayList<String>(extraColumnNameSet));
                }
                // Make sure extra columns will be written in predictable order
                extraColumnNames.addAll(extraColumnNameSet);
                extraColumnNames.sort((s1, s2) -> s1.compareTo(s2));
            }
        }

        // Write the metadata
        // FIXME: Support writing them in a separate file
        for ( String meta : SlotHelper.getMappingSetHelper().visitSlots(mappingSet,
                new MappingSetSlotVisitor(prefixManager)) ) {
            writer.append(meta);
        }

        // Find the used slots
        SlotHelper<Mapping> helper = SlotHelper.getMappingHelper(true);
        Set<String> usedSlotNames = new HashSet<String>();
        for ( Mapping mapping : mappingSet.getMappings() ) {
            usedSlotNames.addAll(helper.visitSlots(mapping, (slot, m, value) -> slot.getName()));
        }

        // Remove the slots that have been condensed
        usedSlotNames.removeAll(condensedSlots);

        // Don't bother visiting the extra metadata slot if there are no extra metadata
        // to write
        if ( extraColumnNameSet.isEmpty() ) {
            usedSlotNames.remove("extra_metadata");
        }

        // Only visit those slots. We need to explicitly set the list of slots to visit
        // in case some slots are empty in some mappings but not in others.
        helper.setSlots(new ArrayList<String>(usedSlotNames), false);

        // Write the column headers
        List<Slot<Mapping>> usedSlots = helper.getSlots();
        for ( int i = 0, n = usedSlots.size(); i < n; i++ ) {
            String name = usedSlots.get(i).getName();
            if ( name.equals("extra_metadata") ) {
                writer.append(String.join("\t", extraColumnNames));
            } else {
                writer.append(name);
            }
            if ( i < n - 1 ) {
                writer.append('\t');
            }
        }
        writer.append('\n');

        // Write the individual mappings
        MappingSlotVisitor mappingVisitor = new MappingSlotVisitor(prefixManager, extraColumnNames);
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
            String name = slot.getName();
            boolean isExtraMetadata = false;
            if ( name.equals("curie_map") ) {
                // We ignore the Curie map provided in the mapping set to write the effective
                // map instead.
                values = new HashMap<String, String>();
                for ( String prefixName : usedPrefixes ) {
                    if ( prefixName != null ) {
                        values.put(prefixName, prefixManager.getPrefix(prefixName));
                    }
                }
            } else if ( name.equals("extra_metadata") ) {
                isExtraMetadata = true;
            }

            if ( !values.isEmpty() && (!isExtraMetadata || extraPolicy != ExtraMetadataPolicy.NONE) ) {
                return mapToString(name, values, isExtraMetadata && extraPolicy == ExtraMetadataPolicy.ALL);
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
        public String visit(Slot<MappingSet> slot, MappingSet set, Object value) {
            return value != null ? value.toString() : "";
        }

        /*
         * Format a dictionary into a commented YAML block. If flat is true, the
         * contents of the dictionary is written as if its keys were first-level slots.
         */
        private String mapToString(String name, Map<String, String> map, boolean flat) {
            StringBuilder sb = new StringBuilder();
            if ( !flat ) {
                sb.append("#");
                sb.append(name);
                sb.append(":\n");
            }
            List<String> keys = new ArrayList<String>(map.keySet());
            keys.sort((s1, s2) -> s1.compareTo(s2));
            for ( String key : keys ) {
                sb.append("#");
                if ( !flat ) {
                    sb.append("  ");
                }
                sb.append(key);
                sb.append(": \"");
                sb.append(map.get(key));
                sb.append("\"\n");
            }
            return sb.toString();
        }
    }

    /*
     * Visits all slots in a mapping to render their values as a TSV cell.
     */
    private class MappingSlotVisitor extends SlotVisitorBase<Mapping, String> {
        private PrefixManager pm;
        private List<String> extraSlots;

        MappingSlotVisitor(PrefixManager prefixManager, List<String> extraSlots) {
            pm = prefixManager;
            this.extraSlots = extraSlots;
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
        public String visit(Slot<Mapping> slot, Mapping object, Object value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, Map<String, String> values) {
            List<String> items = new ArrayList<String>();
            for ( String key : extraSlots ) {
                items.add(values.getOrDefault(key, ""));
            }
            return String.join("\t", items);
        }
    }

    /*
     * Visits all string slots in a mapping set to compile a list of used prefix
     * names.
     */
    private class MappingSetPrefixVisitor extends SlotVisitorBase<MappingSet, Void> {
        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet object, String value) {
            if ( slot.isEntityReference() ) {
                usedPrefixes.add(prefixManager.getPrefixName(value));
            }
            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet object, List<String> values) {
            if ( slot.isEntityReference() ) {
                for ( String value : values ) {
                    usedPrefixes.add(prefixManager.getPrefixName(value));
                }
            }
            return null;
        }
    }

    /*
     * Visits all string slots in a mapping to compile a list of used prefix names.
     */
    private class MappingPrefixVisitor extends SlotVisitorBase<Mapping, Void> {

        @Override
        public Void visit(Slot<Mapping> slot, Mapping object, String value) {
            if ( slot.isEntityReference() ) {
                usedPrefixes.add(prefixManager.getPrefixName(value));
            }
            return null;
        }

        @Override
        public Void visit(Slot<Mapping> slot, Mapping object, List<String> values) {
            if ( slot.isEntityReference() ) {
                for ( String value : values ) {
                    usedPrefixes.add(prefixManager.getPrefixName(value));
                }
            }
            return null;
        }
    }
}
