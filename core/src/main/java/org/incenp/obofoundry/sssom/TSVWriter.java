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

package org.incenp.obofoundry.sssom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.ValueType;

/**
 * A writer to serialise a SSSOM mapping set into the TSV format.
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
public class TSVWriter extends BaseWriter {

    private static final Pattern tsvSpecialChars = Pattern.compile("[\t\n\r\"]");

    private BufferedWriter tsvWriter, metaWriter;
    private Set<String> usedPrefixes = new HashSet<String>();

    /**
     * Creates a new instance that will write data to the specified files.
     * 
     * @param tsvFile  The file to write the TSV section to.
     * @param metaFile The file to write the metadata block to. If {@code null}, the
     *                 metadata block will be embedded in the same file as the TSV
     *                 section.
     * @throws IOException If any of the files cannot be opened for any reason.
     */
    public TSVWriter(File tsvFile, File metaFile) throws IOException {
        tsvWriter = new BufferedWriter(new FileWriter(tsvFile));
        if ( metaFile != null ) {
            metaWriter = new BufferedWriter(new FileWriter(metaFile));
        }
    }

    /**
     * Creates a new instance that will write data to the specified file in embedded
     * mode (metadata block and TSV section in the same file).
     * 
     * @param file The file to write to.
     * @throws IOException If the file cannot be opened for any reason.
     */
    public TSVWriter(File file) throws IOException {
        this(file, null);
    }

    /**
     * Creates a new instance that will write data to the specified streams.
     * 
     * @param tsvStream  The stream to write the TSV section to.
     * @param metaStream The stream to write the metadata block to. If {@code null},
     *                   the metadata block will be embedded in the same stream as
     *                   the TSV section.
     */
    public TSVWriter(OutputStream tsvStream, OutputStream metaStream) {
        tsvWriter = new BufferedWriter(new OutputStreamWriter(tsvStream));
        if ( metaStream != null ) {
            metaWriter = new BufferedWriter(new OutputStreamWriter(metaStream));
        }
    }

    /**
     * Creates a new instance that will write data to the specified stream in
     * embedded mode.
     * 
     * @param stream The stream to write to.
     */
    public TSVWriter(OutputStream stream) {
        this(stream, null);
    }

    /**
     * Creates a new instance that will write data to files with the specified
     * filenames.
     * 
     * @param tsvFilename  The name of the file to write the TSV section to.
     * @param metaFilename The name of the file to write the metadata block to. If
     *                     {@code null}, the metadata block will be embedded in the
     *                     same file as the TSV section.
     * @throws IOException If any of the files cannot be opened for any reason.
     */
    public TSVWriter(String tsvFilename, String metaFilename) throws IOException {
        tsvWriter = new BufferedWriter(new FileWriter(new File(tsvFilename)));
        if ( metaFilename != null ) {
            metaWriter = new BufferedWriter(new FileWriter(new File(metaFilename)));
        }
    }

    /**
     * Creates a new instance that will write data to a file with the specified
     * filename, in embedded mode.
     * 
     * @param filename The name of the file to write to.
     * @throws IOException If the file cannot be opened for any reason.
     */
    public TSVWriter(String filename) throws IOException {
        this(new File(filename));
    }

    @Override
    protected void doWrite(MappingSet mappingSet) throws IOException {
        // Find out which prefixes are actually needed
        usedPrefixes = getUsedPrefixes(mappingSet);

        // Condense the set
        Set<String> condensedSlots = condenseSet(mappingSet);

        // Write the metadata
        MappingSetSlotVisitor v = new MappingSetSlotVisitor(metaWriter == null ? "#" : "");
        SlotHelper.getMappingSetHelper().visitSlots(mappingSet, v);
        if ( metaWriter != null ) {
            metaWriter.append(v.getMetadataBlock());
            metaWriter.close();
        } else {
            tsvWriter.append(v.getMetadataBlock());
        }

        // Find the used slots
        SlotHelper<Mapping> helper = SlotHelper.getMappingHelper(true);
        Set<String> usedSlotNames = new HashSet<String>();
        for ( Mapping mapping : mappingSet.getMappings() ) {
            usedSlotNames.addAll(helper.visitSlots(mapping, (slot, m, value) -> slot.getName()));
        }
        List<ExtensionDefinition> extraSlots = extensionManager.getDefinitions(true, true);

        // Remove the slots that have been condensed
        usedSlotNames.removeAll(condensedSlots);

        // Only visit those slots. We need to explicitly set the list of slots to visit
        // in case some slots are empty in some mappings but not in others.
        helper.setSlots(new ArrayList<String>(usedSlotNames), false);

        // Write the column headers
        List<Slot<Mapping>> usedSlots = helper.getSlots();
        ArrayList<String> headers = new ArrayList<String>();
        for ( Slot<Mapping> slot : usedSlots ) {
            if ( slot.getName().equals("extensions") ) {
                for ( ExtensionDefinition definition : extraSlots ) {
                    headers.add(definition.getSlotName());
                }
            } else {
                headers.add(slot.getName());
            }
        }
        if ( headers.isEmpty() ) {
            // May happen if the set is itself empty (no mappings). In that case, we write
            // default headers so that the TSV file can be read back without errors.
            headers.add("subject_id");
            headers.add("predicate_id");
            headers.add("object_id");
            headers.add("mapping_justification");
        }
        tsvWriter.append(String.join("\t", headers));
        tsvWriter.append('\n');

        // Write the individual mappings
        MappingSlotVisitor mappingVisitor = new MappingSlotVisitor(extraSlots);
        mappingSet.getMappings().sort(new DefaultMappingComparator());
        for ( Mapping mapping : mappingSet.getMappings() ) {
            List<String> values = helper.visitSlots(mapping, mappingVisitor, true);
            tsvWriter.append(String.join("\t", values));
            tsvWriter.append('\n');
        }

        tsvWriter.close();
    }

    /*
     * Visits all slots in a MappingSet to get the lines that will make up the
     * metadata block.
     */
    private class MappingSetSlotVisitor extends SlotVisitorBase<MappingSet, Void> {
        StringBuilder sb = new StringBuilder();
        DecimalFormat floatFormatter;
        String linePrefix;

        MappingSetSlotVisitor(String linePrefix) {
            floatFormatter = new DecimalFormat("#.##");
            floatFormatter.setRoundingMode(RoundingMode.HALF_UP);
            this.linePrefix = linePrefix;
        }

        /*
         * Get the collected metadata as a single string.
         */
        public String getMetadataBlock() {
            return sb.toString();
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, String value) {
            sb.append(linePrefix);
            sb.append(slot.getName());
            sb.append(": ");
            escapeYAML(sb, slot.isEntityReference() ? prefixManager.shortenIdentifier(value) : value);
            sb.append('\n');
            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, List<String> values) {
            if ( values.size() > 0 ) {
                sb.append(linePrefix);
                sb.append(slot.getName());
                sb.append(":\n");
                for ( String value : values ) {
                    sb.append(linePrefix);
                    sb.append("  - ");
                    escapeYAML(sb, slot.isEntityReference() ? prefixManager.shortenIdentifier(value) : value);
                    sb.append("\n");
                }
            }
            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, Map<String, String> values) {
            String name = slot.getName();
            if ( name.equals("curie_map") ) {
                // We ignore the Curie map provided in the mapping set to write the effective
                // map instead.
                values = new HashMap<String, String>();
                for ( String prefixName : usedPrefixes ) {
                    values.put(prefixName, prefixManager.getPrefix(prefixName));
                }
            }

            if ( !values.isEmpty() ) {
                sb.append(linePrefix);
                sb.append(name);
                sb.append(":\n");
                List<String> keys = new ArrayList<String>(values.keySet());
                keys.sort((s1, s2) -> s1.compareTo(s2));
                for ( String key : keys ) {
                    sb.append(linePrefix);
                    sb.append("  ");
                    sb.append(key);
                    sb.append(": ");
                    escapeYAML(sb, values.get(key));
                    sb.append("\n");
                }
            }

            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, Double value) {
            sb.append(floatFormatter.format(value));
            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, LocalDate value) {
            // The SSSOM specification says nothing on how to serialise dates, but LinkML
            // says “for xsd dates, datetimes, and times, AtomicValue must be a string
            // conforming to the relevant ISO type”. I assume this means ISO-8601.
            sb.append(
                    String.format("%s%s: %s\n", linePrefix, slot.getName(), value.format(DateTimeFormatter.ISO_DATE)));
            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, Object value) {
            sb.append(value != null ? value.toString() : "");
            return null;
        }

        @Override
        public Void visitExtensionDefinitions(MappingSet set, List<ExtensionDefinition> definitions) {
            if ( extraPolicy == ExtraMetadataPolicy.DEFINED && !definitions.isEmpty() ) {
                sb.append(linePrefix);
                sb.append("extension_definitions:\n");
                for ( ExtensionDefinition definition : definitions ) {
                    sb.append(linePrefix);
                    sb.append("  - slot_name: ");
                    sb.append(definition.getSlotName());
                    sb.append('\n');
                    sb.append(linePrefix);
                    sb.append("    property: ");
                    escapeYAML(sb, prefixManager.shortenIdentifier(definition.getProperty()));
                    sb.append("\n");
                    if ( definition.getEffectiveType() != ValueType.STRING ) {
                        sb.append(linePrefix);
                        sb.append("    type_hint: ");
                        escapeYAML(sb, prefixManager.shortenIdentifier(definition.getTypeHint()));
                        sb.append("\n");
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitExtensions(MappingSet set, Map<String, ExtensionValue> extensions) {
            if (extraPolicy != ExtraMetadataPolicy.NONE &&  extensions != null) {
                for ( ExtensionDefinition definition : extensionManager.getDefinitions(true, false) ) {
                    ExtensionValue value = extensions.get(definition.getProperty());
                    if ( value != null ) {
                        sb.append(linePrefix);
                        sb.append(definition.getSlotName());
                        sb.append(": ");
                        escapeYAML(sb, value.isIdentifier() ? prefixManager.shortenIdentifier(value.asString())
                                : value.toString());
                        sb.append("\n");
                    }
                }
            }

            return null;
        }

        /*
         * Maybe escape a string into YAML double-quotes.
         *
         * We try to always write strings in YAML plain (unquoted) style
         * (https://yaml.org/spec/1.2.2/#733-plain-style), and fallback to double-quoted
         * style (https://yaml.org/spec/1.2.2/#double-quoted-style) if:
         *
         * - the string is empty;
         *
         * - the string starts with a white space or a YAML indicator character;
         *
         * - the string ends with a white space;
         *
         * - the string contains a ": "or a " #" sequence;
         *
         * - the string contain any character that needs to be escaped.
         */
        private void escapeYAML(StringBuilder sb, String s) {
            int start = sb.length();
            boolean quote = false;
            ArrayList<Integer> delayedEscapes = new ArrayList<Integer>();

            if ( s.isEmpty() ) {
                quote = true;
            } else {
                // Check if first character is allowed in plain style
                int c = s.codePointAt(0);
                switch ( c ) {
                // YAML indicator characters (https://yaml.org/spec/1.2.2/#rule-c-indicator)
                case ',':
                case '[':
                case ']':
                case '{':
                case '}':
                case '#':
                case '&':
                case '*':
                case '!':
                case '|':
                case '>':
                case '\'':
                case '"':
                case '%':
                case '@':
                case '`':
                    quote = true;
                    break;

                // Initial white space would not be preserved in plain style
                case ' ':
                    quote = true;
                    break;

                // Allowed as the first character if followed by a non-white space character
                case ':':
                case '?':
                case '-':
                    if ( s.length() >= 2 ) {
                        int c2 = s.codePointAt(1);
                        if ( c2 == 0xFEFF || c2 == '\n' || c2 == '\r' || c2 == ' ' || c2 == '\t' ) {
                            quote = true;
                        }
                    } else {
                        quote = true;
                    }
                    break;
                }
            }

            for ( int i = 0, n = s.length(); i < n; i++ ) {
                int c = s.codePointAt(i);
                switch ( c ) {
                case 0x00: // Null
                    sb.append("\\0");
                    quote = true;
                    break;

                case 0x07: // Bell
                    sb.append("\\a");
                    quote = true;
                    break;

                case 0x08: // Backspace
                    sb.append("\\b");
                    quote = true;
                    break;

                case 0x09: // Horizontal tab
                    sb.append("\\t");
                    quote = true;
                    break;

                case 0x0A: // Line feed
                    sb.append("\\n");
                    quote = true;
                    break;

                case 0x0B: // Vertical tab
                    sb.append("\\v");
                    quote = true;
                    break;

                case 0x0C: // Form feed
                    sb.append("\\f");
                    quote = true;
                    break;

                case 0x0D: // Carriage return
                    sb.append("\\r");
                    quote = true;
                    break;

                case 0x1B: // Escape
                    sb.append("\\e");
                    quote = true;
                    break;

                case 0x22: // Double quote (only needs escaping if we are quoting, but does not, in itself,
                           // trigger quoting)
                    sb.append('"');
                    delayedEscapes.add(i);
                    break;

                case 0x5C: // Backslash
                    sb.append("\\\\");
                    quote = true;
                    break;

                case 0x85: // Unicode next line
                    sb.append("\\N");
                    quote = true;
                    break;

                case 0xA0: // Non-breakable space
                    sb.append("\\_");
                    quote = true;
                    break;

                case 0x2028: // Unicode line separator
                    sb.append("\\L");
                    quote = true;
                    break;

                case 0x2029: // Unicode paragraph separator
                    sb.append("\\N");
                    quote = true;
                    break;

                case ':': // Forbidden in plain style if at the end the string, or followed by spaces
                    if ( i < n - 1 ) {
                        if ( s.codePointAt(i + 1) == ' ' ) {
                            quote = true;
                        }
                    } else {
                        quote = true;
                    }
                    sb.append(':');
                    break;

                case ' ': // Forbidden in plain style if followed by '#' or at the end of the string
                    if ( i < n - 1 ) {
                        if ( s.codePointAt(i + 1) == '#' ) {
                            quote = true;
                        }
                    } else {
                        quote = true;
                    }
                    sb.append(' ');
                    break;

                default:
                    if ( c <= 0x1F || (c >= 0x7F && c <= 0x9F) ) {
                        sb.append(String.format("\\x%02x", c));
                        quote = true;
                    } else if ( (c >= 0xD800 && c <= 0xDFFF) || c == 0xFFFE || c == 0xFFFF ) {
                        sb.append(String.format("\\u%04x", c));
                        quote = true;
                    } else {
                        sb.appendCodePoint(c);
                    }
                    break;
                }
            }

            if ( quote ) {
                sb.insert(start++, '"');
                for ( Integer i : delayedEscapes ) {
                    sb.insert(start++ + i.intValue(), '\\');
                }
                sb.append('"');
            }
        }
    }

    /*
     * Visits all slots in a mapping to render their values as a TSV cell.
     */
    private class MappingSlotVisitor extends SlotVisitorBase<Mapping, String> {
        private List<ExtensionDefinition> definitions;
        private DecimalFormat floatFormatter;

        MappingSlotVisitor(List<ExtensionDefinition> extraSlots) {
            definitions = extraSlots;
            floatFormatter = new DecimalFormat("#.###");
            floatFormatter.setRoundingMode(RoundingMode.HALF_UP);
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, String value) {
            if ( value == null ) {
                return "";
            } else if ( slot.isEntityReference() ) {
                return escapeTSV(prefixManager.shortenIdentifier(value));
            } else {
                return escapeTSV(value);
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
                sb.append(slot.isEntityReference() ? prefixManager.shortenIdentifier(value) : value);
                if ( i < n - 1 ) {
                    sb.append('|');
                }
            }
            return escapeTSV(sb.toString());
        }

        @Override
        public String visit(Slot<Mapping> slot, Mapping object, Double value) {
            if ( value == null ) {
                return "";
            } else {
                return floatFormatter.format(value);
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
        public String visitExtensions(Mapping object, Map<String, ExtensionValue> extensions) {
            if ( extraPolicy == ExtraMetadataPolicy.NONE || extensions == null ) {
                return null;
            }

            ArrayList<String> items = new ArrayList<String>();
            for ( ExtensionDefinition definition : definitions ) {
                ExtensionValue ev = extensions.get(definition.getProperty());
                if ( ev != null ) {
                    items.add(ev.isIdentifier() ? prefixManager.shortenIdentifier(ev.asString()) : ev.toString());
                } else {
                    items.add("");
                }
            }

            return String.join("\t", items);
        }

        /*
         * Apply CSV-style escaping rules
         * https://datatracker.ietf.org/doc/html/rfc4180#section-2
         */
        private String escapeTSV(String value) {
            if ( tsvSpecialChars.matcher(value).find() ) {
                return "\"" + value.replace("\"", "\"\"") + "\"";
            } else {
                return value;
            }
        }
    }
}
