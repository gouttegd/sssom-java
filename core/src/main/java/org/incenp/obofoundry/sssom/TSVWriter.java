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
import java.util.UUID;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.ValueType;

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

    private static final Pattern tsvSpecialChars = Pattern.compile("[\t\n\r\"]");

    private BufferedWriter writer;
    private PrefixManager prefixManager = new PrefixManager();
    private Set<String> usedPrefixes = new HashSet<String>();
    private boolean customMap = false;
    private ExtraMetadataPolicy extraPolicy = ExtraMetadataPolicy.NONE;
    private ExtensionSlotManager extensionManager;

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

        // Compute effective definitions for non-standard slots
        extensionManager = new ExtensionSlotManager(extraPolicy, prefixManager);
        extensionManager.fillFromExistingExtensions(mappingSet);
        mappingSet.setExtensionDefinitions(extensionManager.getDefinitions(true, false));

        // Find out which prefixes are actually needed
        SlotHelper.getMappingSetHelper().visitSlots(mappingSet, new MappingSetPrefixVisitor());
        MappingPrefixVisitor prefixVisitor = new MappingPrefixVisitor();
        mappingSet.getMappings().forEach(m -> SlotHelper.getMappingHelper().visitSlots(m, prefixVisitor));
        usedPrefixes.addAll(extensionManager.getUsedPrefixes());
        for ( BuiltinPrefix bp : BuiltinPrefix.values() ) {
            usedPrefixes.remove(bp.getPrefixName());
        }

        // Condense the set
        Set<String> condensedSlots = new SlotPropagator(PropagationPolicy.NeverReplace).condense(mappingSet, true);

        // Write the metadata
        // FIXME: Support writing them in a separate file
        MappingSetSlotVisitor v = new MappingSetSlotVisitor();
        SlotHelper.getMappingSetHelper().visitSlots(mappingSet, v);
        writer.append(v.getMetadataBlock());

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
        writer.append(String.join("\t", headers));
        writer.append('\n');

        // Write the individual mappings
        MappingSlotVisitor mappingVisitor = new MappingSlotVisitor(extraSlots);
        mappingSet.getMappings().sort(new DefaultMappingComparator());
        for ( Mapping mapping : mappingSet.getMappings() ) {
            List<String> values = helper.visitSlots(mapping, mappingVisitor, true);
            writer.append(String.join("\t", values));
            writer.append('\n');
        }

        writer.close();
    }

    /*
     * Visits all slots in a MappingSet to get the lines that will make up the
     * metadata block.
     */
    private class MappingSetSlotVisitor extends SlotVisitorBase<MappingSet, Void> {
        StringBuilder sb = new StringBuilder();
        DecimalFormat floatFormatter;

        MappingSetSlotVisitor() {
            floatFormatter = new DecimalFormat("#.###");
            floatFormatter.setRoundingMode(RoundingMode.HALF_UP);
        }

        /*
         * Get the collected metadata as a single string.
         */
        public String getMetadataBlock() {
            return sb.toString();
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, String value) {
            sb.append('#');
            sb.append(slot.getName());
            sb.append(": ");
            escapeYAML(sb, slot.isEntityReference() ? prefixManager.shortenIdentifier(value) : value);
            sb.append('\n');
            return null;
        }

        @Override
        public Void visit(Slot<MappingSet> slot, MappingSet set, List<String> values) {
            if ( values.size() > 0 ) {
                sb.append("#");
                sb.append(slot.getName());
                sb.append(":\n");
                for ( String value : values ) {
                    sb.append("#  - ");
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
                    if ( prefixName != null ) {
                        values.put(prefixName, prefixManager.getPrefix(prefixName));
                    }
                }
            }

            if ( !values.isEmpty() ) {
                sb.append("#");
                sb.append(name);
                sb.append(":\n");
                List<String> keys = new ArrayList<String>(values.keySet());
                keys.sort((s1, s2) -> s1.compareTo(s2));
                for ( String key : keys ) {
                    sb.append("#  ");
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
            sb.append(String.format("#%s: %s\n", slot.getName(), value.format(DateTimeFormatter.ISO_DATE)));
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
                sb.append("#extension_definitions:\n");
                for ( ExtensionDefinition definition : definitions ) {
                    sb.append("#  - slot_name: ");
                    sb.append(definition.getSlotName());
                    sb.append("\n#    property: ");
                    escapeYAML(sb, prefixManager.shortenIdentifier(definition.getProperty()));
                    sb.append("\n");
                    if ( definition.getEffectiveType() != ValueType.STRING ) {
                        sb.append("#    type_hint: ");
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
                        sb.append("#");
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

                // Allowed as the first character if followed by a non-white space character
                case ':':
                case '?':
                case '-':
                    if ( s.length() >= 2 ) {
                        int c2 = s.codePointAt(1);
                        if ( c2 != 0xFEFF && c2 != '\n' && c2 != '\r' && c2 != ' ' && c2 != '\t' ) {
                            quote = false;
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

                case 0x22: // Double quote
                    sb.append("\\\"");
                    quote = true;
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
                sb.insert(start, '"');
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
