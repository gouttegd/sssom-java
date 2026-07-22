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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.MappingTSVWriterVisitor;
import org.incenp.obofoundry.sssom.SSSOMWriter;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.model.ValueType;
import org.incenp.obofoundry.sssom.slots.DateSlot;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.PredicateModifierSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;
import org.incenp.obofoundry.sssom.slots.URISlot;

/**
 * A writer to serialise a SSSOM mapping set into the “CSV on the Web” (CSVW)
 * format.
 * <p>
 * This should be considered <em>experimental</em> for the time being, and
 * continued support for this format is uncertain.
 * 
 * @see <a href="https://w3c.github.io/csvw/primer/">CSV on the Web Primer</a>.
 */
public class CSVWWriter extends SSSOMWriter {

    private BufferedWriter csvWriter, metaWriter;
    private String csvUrl;
    private JSONBuilder json = new JSONBuilder();

    private final static String CSVW_CONTEXT_NAME = "http://www.w3.org/ns/csvw";
    private final static Map<ValueType, String> EXTENSION_TYPE_MAP;

    static {
        Map<ValueType, String> extensionTypeMap = new HashMap<>();
        extensionTypeMap.put(ValueType.BOOLEAN, "boolean");
        extensionTypeMap.put(ValueType.DATE, "date");
        extensionTypeMap.put(ValueType.DATETIME, "datetime");
        extensionTypeMap.put(ValueType.DOUBLE, "decimal");
        extensionTypeMap.put(ValueType.INTEGER, "integer");
        extensionTypeMap.put(ValueType.URI, "anyURI");

        EXTENSION_TYPE_MAP = Collections.unmodifiableMap(extensionTypeMap);
    }

    /**
     * Creates a new instance that will write data to the specified files.
     * 
     * @param csvFile  The file to write the CSV data to.
     * @param metaFile The file to write the CSV metadata to. If <code>null</code>,
     *                 the metadata will be written to a file with the same name as
     *                 the CSV file plus a <code>-metadata.json</code> suffix.
     * @throws IOException If any of the files cannot be opened for any reason.
     */
    public CSVWWriter(File csvFile, File metaFile) throws IOException {
        csvWriter = new BufferedWriter(new FileWriter(csvFile));
        if ( metaFile == null ) {
            metaFile = new File(csvFile.getAbsolutePath() + "-metadata.json");
        }
        metaWriter = new BufferedWriter(new FileWriter(metaFile));
        csvUrl = csvFile.getName();
    }

    /**
     * Creates a new instance that will write data to files with the specified
     * filenames.
     * 
     * @param csvFile  The name of the file to write the CSV data to.
     * @param metaFile The name of the file to write the CSV metadata to. If
     *                 <code>null</code>, the metadata will be written to a file
     *                 with the same name as the CSV file plus a
     *                 <code>-metadata.json</code> suffix.
     * @throws IOException If any of the files cannot be opened for any reason.
     */
    public CSVWWriter(String csvFile, String metaFile) throws IOException {
        this(new File(csvFile), metaFile != null ? new File(metaFile) : null);
    }

    /**
     * Creates a new instance that will write data to the specified streams.
     * 
     * @param csvStream  The stream to write the CSV data to.
     * @param metaStream The stream to write the CSV metadata to.
     */
    public CSVWWriter(OutputStream csvStream, OutputStream metaStream) {
        csvWriter = new BufferedWriter(new OutputStreamWriter(csvStream));
        metaWriter = new BufferedWriter(new OutputStreamWriter(metaStream));
    }

    /**
     * Forces the URL of the CSV file.
     * <p>
     * This is the URL written in the metadata file. This should point to wherever
     * the CSV file is located. By default, this is simply the name of the CSV file,
     * if known.
     * 
     * @param url The URL pointing to the CSV file.
     */
    public void setCSVUrl(String url) {
        csvUrl = url;
    }

    @Override
    protected void doWrite(MappingSet mappingSet) throws IOException {
        // Find the used slots
        SlotHelper<Mapping> helper = SlotHelper.getMappingHelper(true);
        Set<String> usedSlotNames = new HashSet<>();
        for ( Mapping mapping : mappingSet.getMappings() ) {
            usedSlotNames.addAll(helper.visitSlots(mapping, (slot, m, value) -> slot.getName()));
        }
        List<ExtensionDefinition> extraSlots = extensionManager.getDefinitions(true, true);

        // Only visit those slots. We need to explicitly set the list of slots to visit
        // in case some slots are empty in some mappings but not in others.
        helper.setSlots(new ArrayList<String>(usedSlotNames), false);

        // Write the individual mappings
        MappingTSVWriterVisitor mappingVisitor = new MappingTSVWriterVisitor(prefixManager,
                extraPolicy != ExtraMetadataPolicy.NONE ? extraSlots : null, true);
        for ( Mapping mapping : mappingSet.getMappings() ) {
            helper.visitSlots(mapping, mappingVisitor, true);
            csvWriter.append(String.join(",", mappingVisitor.getValues()));
            csvWriter.append('\n');
            mappingVisitor.reset();
        }
        csvWriter.close();

        // Write the metadata file
        json.addKey("@context");
        json.startList();
        json.addValue(CSVW_CONTEXT_NAME);
        json.endList();
        json.addKey("@type");
        json.addValue("Table");
        describeMappingSet(mappingSet);
        if ( csvUrl != null ) {
            json.addKey("url");
            json.addValue(csvUrl);
        }
        json.addKey("dialect");
        json.startDict();
        json.addKey("header");
        json.addValue(false);
        json.endDict();

        // Write the schema for each column
        json.addKey("tableSchema");
        json.startDict();
        json.addKey("columns");
        json.startList();
        helper.excludeSlots(List.of("extensions"));
        for ( Slot<Mapping> slot : helper.getSlots() ) {
            describeColumn(slot);
        }
        if ( extraPolicy != ExtraMetadataPolicy.NONE ) {
            for ( ExtensionDefinition extension : extensionManager.getDefinitions(true, true) ) {
                describeColumn(extension);
            }
        }

        // Assemble and write the final JSON file
        metaWriter.append(json.close());
        metaWriter.close();
    }

    private void describeMappingSet(MappingSet ms) {
        if ( ms.getMappingSetTitle() != null ) {
            json.addKey("dc:title");
            json.addValue(ms.getMappingSetTitle());
        }
        if ( ms.getMappingSetDescription() != null ) {
            json.addKey("dc:description");
            json.addValue(ms.getMappingSetDescription());
        }
        if ( ms.getLicense() != null ) {
            json.addKey("dc:license");
            json.addValue(ms.getLicense());
        }
        if ( ms.getPublicationDate() != null ) {
            json.addKey("dc:issued");
            json.addValue(ms.getPublicationDate().format(DateTimeFormatter.ISO_DATE));
        }
        if ( ms.getCreatorId() != null && !ms.getCreatorId().isEmpty() ) {
            json.addKey("dc:creator");
            json.startList();
            for ( String creator : ms.getCreatorId() ) {
                json.addValue(creator);
            }
            json.endList();
        }
    }

    private void describeColumn(Slot<Mapping> slot) {
        json.startDict();
        describeColumn(slot.getName(), slot.getURI());
        slot.accept(new SlotDatatypeDescriptionVisitor(), null, null);
        json.endDict();
    }

    private void describeColumn(ExtensionDefinition extension) {
        json.startDict();
        describeColumn(extension.getSlotName(), extension.getProperty());
        describeDatatype(EXTENSION_TYPE_MAP.getOrDefault(extension.getEffectiveType(), "string"), false);
        json.endDict();
    }

    private void describeColumn(String name, String property) {
        json.addKey("name");
        json.addValue(name);
        json.addKey("propertyUrl");
        json.addValue(property);
    }

    private void describeDatatype(String type, boolean multiValued) {
        json.addKey("datatype");
        json.addValue(type);
        if ( multiValued ) {
            json.addKey("separator");
            json.addValue("|");
        }
    }

    private void describeDatatype(Double min, Double max) {
        json.addKey("datatype");
        json.startDict();
        json.addKey("base");
        json.addValue("decimal");
        json.addKey("minInclusive");
        json.addValue(min);
        json.addKey("maxInclusive");
        json.addValue(max);
        json.endDict();
    }

    private void describeDatatype(Collection<String> values) {
        json.addKey("datatype");
        json.startDict();
        json.addKey("base");
        json.addValue("string");
        json.addKey("format");
        json.addValue(String.format("|", values));
        json.endDict();
    }

    private class SlotDatatypeDescriptionVisitor extends SlotVisitorBase<Mapping> {

        @Override
        public void visit(StringSlot<Mapping> slot, Mapping object, String value) {
            describeDatatype("string", false);
        }

        @Override
        public void visit(StringSlot<Mapping> slot, Mapping object, List<String> values) {
            describeDatatype("string", true);
        }

        @Override
        public void visit(URISlot<Mapping> slot, Mapping object, String value) {
            describeDatatype("anyURI", false);
        }

        @Override
        public void visit(URISlot<Mapping> slot, Mapping object, List<String> values) {
            describeDatatype("anyURI", true);
        }

        @Override
        public void visit(DoubleSlot<Mapping> slot, Mapping object, Double value) {
            describeDatatype(slot.getName().equals("reviewer_confidence") ? -1.0 : 0.0, 1.0);
        }

        @Override
        public void visit(DateSlot<Mapping> slot, Mapping object, LocalDate value) {
            describeDatatype("date", false);
        }

        @Override
        public void visit(EntityTypeSlot<Mapping> slot, Mapping object, EntityType value) {
            List<String> values = new ArrayList<>();
            for ( EntityType type : EntityType.values() ) {
                values.add(type.toString());
            }
            describeDatatype(values);
        }

        @Override
        public void visit(MappingCardinalitySlot<Mapping> slot, Mapping object, MappingCardinality value) {
            List<String> values = new ArrayList<>();
            for ( MappingCardinality card : MappingCardinality.values() ) {
                values.add(card.toString());
            }
            describeDatatype(values);
        }

        @Override
        public void visit(PredicateModifierSlot<Mapping> slot, Mapping object, PredicateModifier value) {
            List<String> values = new ArrayList<>();
            for ( PredicateModifier mod : PredicateModifier.values() ) {
                values.add(mod.toString());
            }
            describeDatatype(values);
        }
    }
}
