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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * A parser to read a SSSOM mapping set from the TSV serialisation format. It
 * can read both files that have an embedded metadata block or files with
 * external metadata. Use one of the two-arguments constructors to explicitly
 * specify the name of the external metadata file.
 * <p>
 * Usage:
 * 
 * <pre>
 * try {
 *     TSVReader reader = new TSVReader("my-mappings.sssom.tsv");
 *     MappingSet mappingSet = reader.read();
 * } catch ( IOException ioe ) {
 *     // Generic, non-SSSOM-related I/O error (e.g. file not found)
 * } catch ( SSSOMFormatException sfe ) {
 *     // Invalid SSSOM data
 * }
 * </pre>
 */
public class TSVReader {

    private File tsvFile;
    private BufferedReader tsvReader;
    private Reader metaReader;
    private PrefixManager prefixManager = new PrefixManager();

    /**
     * Creates a new instance that will read data from the specified files.
     * 
     * @param tsvFile  The main TSV file. May be {@code null} if one only wants to
     *                 read a metadata file (in which case the second argument
     *                 cannot also be {@code null}).
     * @param metaFile The accompanying metadata file. If {@code null}, the parser
     *                 will attempt to automatically locate the metadata from the
     *                 main file.
     * @throws FileNotFoundException If any of the files cannot be found.
     */
    public TSVReader(File tsvFile, File metaFile) throws FileNotFoundException {
        if ( tsvFile == null && metaFile == null ) {
            throw new IllegalArgumentException("tsvFile and metaFile cannot both be null");
        }
        if ( tsvFile != null ) {
            this.tsvFile = tsvFile;
            tsvReader = new BufferedReader(new FileReader(tsvFile));
        }
        if ( metaFile != null ) {
            metaReader = new FileReader(metaFile);
        }
    }

    /**
     * Creates a new instance that will read data from a single file. That file
     * should either contain an embedded metadata block, or a file containing the
     * metadata should exist alongside it.
     * 
     * @param file The single file to read.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public TSVReader(File file) throws FileNotFoundException {
        this(file, null);
    }

    /**
     * Creates a new instance that will read data from the specified streams. Note
     * that when reading from a stream, the metadata either needs to be embedded
     * with the TSV stream or an explicit metadata stream must be specified; the
     * reader cannot automatically locate an external metadata file.
     * 
     * @param tsvStream  The main stream, containing the TSV data. May be
     *                   {@code null} if one only wants to read a metadata stream
     *                   (in which case the second argument cannot also be
     *                   {@code null}).
     * @param metaStream The accompanying metadata stream. If {@code null}, the
     *                   metadata must be embedded in the TSV stream.
     */
    public TSVReader(InputStream tsvStream, InputStream metaStream) {
        if ( tsvStream == null && metaStream == null ) {
            throw new IllegalArgumentException("tsvStream and metaStream cannot both be null");
        }
        if ( tsvStream != null ) {
            tsvReader = new BufferedReader(new InputStreamReader(tsvStream));
        }
        if ( metaStream != null ) {
            metaReader = new InputStreamReader(metaStream);
        }
    }

    /**
     * Creates a new instance that will read data from a single stream. That file
     * must contain an embedded metadata block.
     * 
     * @param stream The single stream to read from.
     */
    public TSVReader(InputStream stream) {
        this(stream, null);
    }

    /**
     * Creates a new instance that will read data from the specified files.
     * 
     * @param tsvFile  The name of the main TSV file. May be {@code null} if one
     *                 only wants to read a metadata file (in which case the second
     *                 argument cannot also be {@code null}).
     * @param metaFile The name of the accompanying metadata file. If {@code null},
     *                 the parser will attempt to automatically locate the metadata
     *                 from the main file.
     * @throws FileNotFoundException If any of the files cannot be found.
     */
    public TSVReader(String tsvFile, String metaFile) throws FileNotFoundException {
        this(tsvFile != null ? new File(tsvFile) : null, metaFile != null ? new File(metaFile) : null);
    }

    /**
     * Creates a new instance that will read data from a single file. That file
     * should either contain an embedded metadata block, or a file containing the
     * metadata should exist alongside it.
     * 
     * @param file The name of the single file to read.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public TSVReader(String file) throws FileNotFoundException {
        this(new File(file), null);
    }

    /**
     * Reads a mapping set from the source file(s).
     * 
     * @return A complete SSSOM mapping set, unless no TSV file was provided to the
     *         constructor when this object was created, in which case the returned
     *         object will contain no mappings.
     * @throws SSSOMFormatException If encountering invalid SSSOM data. This
     *                              includes the case where the metadata cannot be
     *                              found.
     * @throws IOException          If any kind of non-SSSOM-related I/O error
     *                              occurs.
     */
    public MappingSet read() throws SSSOMFormatException, IOException {
        return read(tsvReader == null);
    }

    /**
     * Reads a mapping set from the source file(s), with the option of reading the
     * metadata only.
     * 
     * @param metadataOnly If {@code true}, the mappings themselves will not be
     *                     read, only the metadata. The returned {@link MappingSet}
     *                     object will contain no mappings. If no TSV file was
     *                     provided to the constructor when this object was created,
     *                     then no mappings will be read regardless of the value of
     *                     this parameter.
     * @return A SSSOM mapping set, with or without any mappings depending on the
     *         parameter.
     * @throws SSSOMFormatException If encountering invalid SSSOM data. This
     *                              includes the case where the metadata cannot be
     *                              found.
     * @throws IOException          If any kind of non-SSSOM-related I/O error
     *                              occurs.
     */
    public MappingSet read(boolean metadataOnly) throws SSSOMFormatException, IOException {
        if ( metaReader == null ) {
            if ( hasEmbeddedMetadata(tsvReader) ) {
                metaReader = new StringReader(extractMetadata(tsvReader));
            } else if ( tsvFile != null ) {
                File metaFile = findMetadata(tsvFile);
                metaReader = new FileReader(metaFile);
            } else {
                throw new SSSOMFormatException("No embedded metadata and external metadata not specified");
            }
        }

        return read(metaReader, metadataOnly || tsvReader == null);
    }

    /*
     * Reads a mapping set from the source file while reading the metadata from the
     * specified source.
     */
    private MappingSet read(Reader metaReader, boolean metadataOnly) throws SSSOMFormatException, IOException {
        MappingSet ms = readMetadata(metaReader);

        if ( !metadataOnly ) {
            ArrayList<Mapping> mappings = new ArrayList<Mapping>();
            ms.setMappings(mappings);

            // Prepare the list of accepted slots
            Map<String, Slot<Mapping>> slotMaps = new HashMap<String, Slot<Mapping>>();
            for ( Slot<Mapping> slot : SlotHelper.getMappingHelper().getSlots() ) {
                slotMaps.put(slot.getName(), slot);
            }

            // Read the mappings as generic Map objects
            ObjectMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t').withNullValue("");
            MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class).with(schema).readValues(tsvReader);
            while ( it.hasNext() ) {
                try {
                    Mapping mapping = new Mapping();

                    Map<String, Object> rawMapping = it.next();
                    processForBackwardsCompatibility(rawMapping);
                    for ( String key : rawMapping.keySet() ) {
                        if ( slotMaps.containsKey(key) ) {
                            setSlotValue(slotMaps.get(key), mapping, rawMapping.get(key));
                        }
                    }

                    mappings.add(mapping);
                } catch ( RuntimeJsonMappingException e ) {
                    throw new SSSOMFormatException("Error when parsing TSV table", e);
                }
            }

            // Propagate values from set-level to mapping-level
            new SlotPropagator(PropagationPolicy.NeverReplace).propagate(ms);
        } else {
            ms.setMappings(new ArrayList<Mapping>());
        }

        if ( prefixManager.getUnresolvedPrefixNames().size() > 0 ) {
            throw new SSSOMFormatException(String.format("Some prefixes are undeclared: %s",
                    String.join(", ", prefixManager.getUnresolvedPrefixNames())));
        }

        metaReader.close();
        if ( tsvReader != null ) {
            tsvReader.close();
        }

        return ms;
    }

    /*
     * Peek into a file to check for an embedded metadata block.
     */
    private boolean hasEmbeddedMetadata(BufferedReader reader) throws IOException {
        boolean ret = false;

        reader.mark(1);
        int c = reader.read();
        if ( c != -1 && ((char) c) == '#' ) {
            ret = true;
        }
        reader.reset();

        return ret;
    }

    /*
     * Locate an external metadata file.
     */
    private File findMetadata(File file) throws SSSOMFormatException {
        String originalFilename = file.getName();
        int lastDot = originalFilename.lastIndexOf('.');
        if ( lastDot != -1 ) {
            originalFilename = originalFilename.substring(0, lastDot);
        }

        String metaFilename = originalFilename + ".yml";
        File metaFile = new File(file.getParent(), metaFilename);

        if ( !metaFile.exists() ) {
            // Try looking for a file ending with "-meta.yml". The SSSOM spec mentions it,
            // though I suspect it is a mistake.
            lastDot = originalFilename.lastIndexOf('.');
            if ( lastDot != -1 ) {
                originalFilename = originalFilename.substring(0, lastDot);
            }
            metaFilename = originalFilename + "-meta.yml";
            metaFile = new File(file.getParent(), metaFilename);

            if ( !metaFile.exists() ) {
                throw new SSSOMFormatException("External metadata file not found");
            }
        }

        return metaFile;
    }

    /*
     * Extract the embedded metadata block from the commented header.
     */
    private String extractMetadata(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean done = false;
        boolean newLine = true;

        while ( !done ) {
            int c = reader.read();
            if ( c == -1 ) {
                done = true;
            } else {
                if ( newLine ) {
                    newLine = false;
                    if ( c != '#' ) {
                        done = true;
                        reader.reset();
                    }
                } else {
                    sb.append((char) c);
                    if ( c == '\n' ) {
                        newLine = true;
                        reader.mark(1);
                    }
                }
            }
        }

        return sb.toString();
    }

    /*
     * Parse a metadata YAML block into a MappingSet object.
     */
    private MappingSet readMetadata(Reader reader) throws SSSOMFormatException, IOException {
        MappingSet ms = new MappingSet();

        // Parse the metadata block into a generic map
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        @SuppressWarnings("unchecked")
        Map<String, Object> rawSet = mapper.readValue(reader, Map.class);

        // Prepare the list of accepted slots
        Map<String, Slot<MappingSet>> slotMaps = new HashMap<String, Slot<MappingSet>>();
        for ( Slot<MappingSet> slot : SlotHelper.getMappingSetHelper().getSlots() ) {
            slotMaps.put(slot.getName(), slot);
        }

        // Process the CURIE map first, so that we can expand CURIEs as soon as possible
        Object rawCurieMap = rawSet.getOrDefault("curie_map", new HashMap<String, String>());
        if ( isMapOfStrings(rawCurieMap) ) {
            @SuppressWarnings("unchecked")
            Map<String, String> curieMap = Map.class.cast(rawCurieMap);
            for ( String prefix : curieMap.keySet() ) {
                BuiltinPrefix bp = BuiltinPrefix.fromString(prefix);
                if ( bp != null && !bp.getPrefix().equals(curieMap.get(prefix)) ) {
                    throw new SSSOMFormatException("Re-defined builtin prefix in the provided curie map");
                }
            }
            ms.setCurieMap(curieMap);
            rawSet.remove("curie_map");
            prefixManager.add(curieMap);
        } else {
            onTypingError("curie_map");
        }

        // Deal with variations from older versions of the spec
        processForBackwardsCompatibility(rawSet);

        // Now process the remaining slots
        for ( String key : rawSet.keySet() ) {
            if ( slotMaps.containsKey(key) ) {
                setSlotValue(slotMaps.get(key), ms, rawSet.get(key));
            }
        }

        return ms;
    }

    /*
     * Try to assign a parsed YAML value to a mapping or mapping set slot.
     */
    private <T> void setSlotValue(Slot<T> slot, T object, Object rawValue) throws SSSOMFormatException {
        if ( slot.getType() == String.class && String.class.isInstance(rawValue) ) {
            String value = String.class.cast(rawValue);
            if ( slot.isEntityReference() ) {
                value = prefixManager.expandIdentifier(value);
            }
            slot.setValue(object, value);
        } else if ( slot.getType() == List.class && isListOfStrings(rawValue) ) {
            @SuppressWarnings("unchecked")
            List<String> value = List.class.cast(rawValue);
            if ( slot.isEntityReference() ) {
                prefixManager.expandIdentifiers(value, true);
            }
            slot.setValue(object, value);
        } else if ( slot.getType() == List.class && String.class.isInstance(rawValue) ) {
            /*
             * When reading from the TSV table, list values will appear to the YAML parser
             * as a single string; list values must be extracted by splitting the string
             * around '|' characters.
             * 
             * This has the side-effect of allowing list-valued slots to be used as if they
             * were single-valued, which is strictly speaking invalid but happens in the
             * wild (including in the examples shown in the SSSOM documentation!).
             */
            List<String> value = new ArrayList<String>();
            for ( String item : String.class.cast(rawValue).split("\\|") ) {
                value.add(slot.isEntityReference() ? prefixManager.expandIdentifier(item) : item);
            }
            slot.setValue(object, value);
        } else if ( slot.getType() == LocalDate.class && String.class.isInstance(rawValue) ) {
            try {
                LocalDate value = LocalDate.parse(String.class.cast(rawValue));
                slot.setValue(object, value);
            } catch ( DateTimeParseException e ) {
                onTypingError(slot.getName(), e);
            }
        } else if ( slot.getType() == Double.class && String.class.isInstance(rawValue) ) {
            try {
                Double value = Double.valueOf(String.class.cast(rawValue));
                slot.setValue(object, value);
            } catch ( NumberFormatException e ) {
                onTypingError(slot.getName(), e);
            }
        } else if ( slot.getType() == EntityType.class && String.class.isInstance(rawValue) ) {
            EntityType value = EntityType.fromString(String.class.cast(rawValue));
            if ( value != null ) {
                slot.setValue(object, value);
            } else {
                onTypingError(slot.getName());
            }
        } else if ( slot.getType() == MappingCardinality.class && String.class.isInstance(rawValue) ) {
            MappingCardinality value = MappingCardinality.fromString(String.class.cast(rawValue));
            if ( value != null ) {
                slot.setValue(object, value);
            } else {
                onTypingError(slot.getName());
            }
        } else if ( slot.getType() == PredicateModifier.class && String.class.isInstance(rawValue) ) {
            PredicateModifier value = PredicateModifier.fromString(String.class.cast(rawValue));
            if ( value != null ) {
                slot.setValue(object, value);
            } else {
                onTypingError(slot.getName());
            }
        } else if ( rawValue == null ) {
            slot.setValue(object, null);
        } else {
            onTypingError(slot.getName());
        }
    }

    /*
     * Check that a given object is a List<String>
     */
    private boolean isListOfStrings(Object value) {
        if ( List.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            List<Object> list = List.class.cast(value);
            for ( Object item : list ) {
                if ( !String.class.isInstance(item) ) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /*
     * Check that a given object is a Map<String, String>.
     */
    private boolean isMapOfStrings(Object value) {
        if ( Map.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = Map.class.cast(value);
            for ( Object key : map.keySet() ) {
                if ( !String.class.isInstance(key) || !String.class.isInstance(map.get(key)) ) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /*
     * Called when a parsed value does not match the expected type for a slot.
     */
    private void onTypingError(String slotName, Throwable innerException) throws SSSOMFormatException {
        throw new SSSOMFormatException(String.format("Typing error when parsing '%s'", slotName), innerException);
    }

    /*
     * Same, but without an underlying exception as the cause for the typing error.
     */
    private void onTypingError(String slotName) throws SSSOMFormatException {
        onTypingError(slotName, null);
    }

    /*
     * Tweak the parsed YAML dictionary if needed to match the currently supported
     * version of the spec.
     */
    private void processForBackwardsCompatibility(Map<String, Object> rawMap) throws SSSOMFormatException {

        // match_type has been replaced by mapping_justification in SSSOM 0.9.1
        if ( rawMap.containsKey("match_type") && !rawMap.containsKey("mapping_justification") ) {
            Object rawValue = rawMap.get("match_type");
            String value = null;
            if ( rawValue != null ) {
                if ( String.class.isInstance(rawValue) ) {
                    switch ( String.class.cast(rawValue) ) {
                    case "Lexical":
                        value = "semapv:LexicalMatching";
                        break;
                    case "Logical":
                        value = "semapv:LogicalMatching";
                        break;
                    case "HumanCurated":
                        value = "semapv:ManualMappingCuration";
                        break;
                    case "Complex":
                        value = "semapv:CompositeMatching";
                        break;
                    case "Unspecified":
                        value = "semapv:UnspecifiedMatching";
                        break;
                    case "SemanticSimilarity":
                        value = "semapv:SemanticSimilarityThresholdMatching";
                        break;
                    }
                }

                if ( value == null ) {
                    onTypingError("match_type");
                }
            }

            rawMap.remove("match_type");
            rawMap.put("mapping_justification", value);
        }
    }
}
