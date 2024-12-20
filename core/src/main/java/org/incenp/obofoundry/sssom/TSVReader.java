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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.SlotPropagator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * A parser to read a SSSOM mapping set from the TSV serialisation format. It
 * can read both files that have an embedded metadata block or files with
 * external metadata. Use one of the two-arguments constructors to explicitly
 * specify the name of the external metadata file.
 * <p>
 * That parser can also be indirectly used to parse a JSON file: if it detects
 * that the first byte of the file is a <code>{</code> character, it will assume
 * the file is a JSON file (no valid SSSOM/TSV file can start with a
 * <code>{</code>) and will automatically invoke a {@link JSONReader}.
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
public class TSVReader extends SSSOMReader {

    private File tsvFile;
    private BufferedReader tsvReader;
    private Reader metaReader;
    private YAMLConverter converter = new YAMLConverter();

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
     * Creates a new instance that will read data from the specified reader. Note
     * that when reading from a reader object, the metadata either needs to be
     * embedded with the TSV or an explicit reader for the metadata must be
     * specified; the reader cannot automatically locate an external metadata file.
     * 
     * @param tsvReader  The main reader, containing the TSV data. May be
     *                   {@code null} if one only wants to read the metadata (in
     *                   which case the second argument cannot also be
     *                   {@code null}).
     * @param metaReader The accompagnying metadata reader. If {@code null}, the
     *                   metadata must be embedded with the TSV section.
     */
    public TSVReader(Reader tsvReader, Reader metaReader) {
        if ( tsvReader == null && metaReader == null ) {
            throw new IllegalArgumentException("tsvReader and metaReader cannot both be null");
        }
        if ( tsvReader != null ) {
            this.tsvReader = new BufferedReader(tsvReader);
        }
        this.metaReader = metaReader;
    }

    /**
     * Creates a new instance that will read data from the specified reader.
     * <p>
     * This constructor behaves similarly to {@link #TSVReader(Reader, Reader)}, but
     * allows providing the filename of the TSV file in the third argument, which
     * will be used to try locating the external metadata file if (1) the TSV file
     * does not contain an embedded metadata block and (2) the {@code metaReader}
     * argument is {@code null}.
     * 
     * @param tsvReader  The main reader, containing the TSV data. May be
     *                   {@code null} if one only wants to read the metadata (in
     *                   which case the second argument cannot also be {@code null}.
     * @param metaReader The accompanying metadata reader. If {@code null}, the
     *                   metadata must either be embedded with the TSV section or be
     *                   in a file in the same directory and with the same basename
     *                   than the name provided in the third argument.
     * @param filename   The name of the file containing the TSV section; that name
     *                   is not used to actually read the TSV section, but to try
     *                   locating the external metadata file if needed (if the TSV
     *                   file contains no embedded metadata and the second argument
     *                   is {@code null}).
     */
    public TSVReader(Reader tsvReader, Reader metaReader, String filename) {
        this(tsvReader, metaReader);
        if ( filename != null ) {
            tsvFile = new File(filename);
        }
    }

    /**
     * Creates a new instance that will read data from the specified reader.
     * <p>
     * The metadata must be embedded with the TSV section; the reader cannot
     * automatically locate an external metadata file. To read from a Reader object
     * while still being able to automatically locate and use an external metadata
     * file, use {@link #TSVReader(Reader, Reader, String)} with the second argument
     * set to {@code null} and the third argument set to the filename of the TSV
     * section.
     * 
     * @param tsvReader The single reader to read from.
     */
    public TSVReader(Reader tsvReader) {
        this(tsvReader, null);
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
    @Override
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
            GuessedFileType t = peek(tsvReader);
            if ( t == GuessedFileType.JSON ) {
                // Divert to the JSON reader; this will ignore the metadataOnly argument
                JSONReader jr = new JSONReader(tsvReader);
                jr.setExtraMetadataPolicy(extraPolicy);
                jr.setPropagationEnabled(propagationPolicy != PropagationPolicy.Disabled);
                return jr.read();
            } else if ( t == GuessedFileType.TSV_WITH_EMBEDDED_METADATA ) {
                metaReader = new StringReader(extractMetadata(tsvReader));
            } else if ( tsvFile != null ) {
                File metaFile = findMetadata(tsvFile);
                metaReader = new FileReader(metaFile);
            } else {
                throw new SSSOMFormatException("No embedded metadata and external metadata not specified");
            }
        }
        converter.setExtraMetadataPolicy(extraPolicy);
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

            // Read the mappings as generic Map objects
            ObjectMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t').withNullValue("");
            MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class)
                    .with(CsvParser.Feature.SKIP_EMPTY_LINES).with(schema).readValues(tsvReader);
            while ( it.hasNext() ) {
                try {
                    Map<String, Object> rawMapping = it.next();
                    mappings.add(converter.convertMapping(rawMapping));
                } catch ( RuntimeJsonMappingException e ) {
                    throw new SSSOMFormatException("Error when parsing TSV table", e);
                }
            }

            converter.postMappings(ms);

            // Propagate values from set-level to mapping-level
            new SlotPropagator(propagationPolicy).propagate(ms);
        } else {
            ms.setMappings(new ArrayList<Mapping>());
            converter.postMappings(ms);
        }

        if ( converter.getPrefixManager().getUnresolvedPrefixNames().size() > 0 ) {
            throw new SSSOMFormatException(String.format("Some prefixes are undeclared: %s",
                    String.join(", ", converter.getPrefixManager().getUnresolvedPrefixNames())));
        }

        validate(ms.getMappings());

        metaReader.close();
        if ( tsvReader != null ) {
            tsvReader.close();
        }

        return ms;
    }

    /*
     * Peek into a file to guess what it contains (pure TSV, TSV with embedded
     * metadata block, or JSON).
     */
    private GuessedFileType peek(BufferedReader reader) throws IOException {
        GuessedFileType t = GuessedFileType.TSV_ONLY;

        reader.mark(1);
        int c = reader.read();
        if ( c != -1 ) {
            if ( ((char) c) == '#' ) {
                t = GuessedFileType.TSV_WITH_EMBEDDED_METADATA;
            } else if ( ((char) c) == '{' ) {
                t = GuessedFileType.JSON;
            }
        }
        reader.reset();

        return t;
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
        int trailingTabs = 0;
        YamlExtractorState state = YamlExtractorState.NEWLINE;

        while ( !done ) {
            int c = reader.read();
            if ( c == -1 ) {
                done = true;
            } else {
                switch ( state ) {
                case TEXT:
                    if ( c == '\n' ) {
                        sb.append((char) c);
                        state = YamlExtractorState.NEWLINE;
                        reader.mark(1);
                    } else if ( c == '\t' ) {
                        // Possible trailing tabs that we might need to ignore.
                        state = YamlExtractorState.TRAILING;
                        trailingTabs = 1;
                    } else {
                        sb.append((char) c);
                    }
                    break;

                case TRAILING:
                    /*
                     * Common spreadsheet software (e.g. LibreOffice Calc) will write the lines of
                     * the metadata block as if they were normal data lines, with the YAML contents
                     * in the first column followed by empty "columns" (trailing tab characters). We
                     * need to remove those trailing tab characters for the YAML parser to be able
                     * to correctly parse the extracted block.
                     */
                    if ( c == '\n' ) {
                        // We were indeed in trailing tabs; discard them and go the next line.
                        trailingTabs = 0;
                        sb.append((char) c);
                        state = YamlExtractorState.NEWLINE;
                        reader.mark(1);
                    } else if ( c == '\t' ) {
                        // Still in trailing tabs, keep counting.
                        trailingTabs += 1;
                    } else if ( c == '\r' ) {
                        // Some macOS or Windows/DOS cruft, just ignore.
                    } else {
                        // Those were not the trailing tabs we were looking for, there is still some
                        // text coming in on the line; inject the tabs we have counted and go back to
                        // reading normal text.
                        for ( int i = 0; i < trailingTabs; i++ ) {
                            sb.append('\t');
                        }
                        trailingTabs = 0;
                        state = YamlExtractorState.TEXT;
                    }
                    break;

                case NEWLINE:
                    if ( c != '#' ) {
                        done = true;
                        reader.reset();
                    } else {
                        state = YamlExtractorState.TEXT;
                    }
                    break;
                }
            }
        }

        return sb.toString();
    }

    /*
     * Used by extractMetadata to keep track of where we are when reading the YAML
     * block.
     */
    private enum YamlExtractorState {
        TEXT,
        TRAILING,
        NEWLINE
    }

    /*
     * Parse a metadata YAML block into a MappingSet object.
     */
    private MappingSet readMetadata(Reader reader) throws SSSOMFormatException, IOException {
        MappingSet ms;

        // Parse the metadata block into a generic map
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawSet = mapper.readValue(reader, Map.class);
            ms = converter.convertMappingSet(rawSet);
        } catch ( JsonParseException | JsonMappingException e ) {
            throw new SSSOMFormatException("Invalid YAML metadata", e);
        }

        // Check the CURIE map for re-defined prefixes
        Map<String, String> curieMap = ms.getCurieMap();
        for ( String prefix : curieMap.keySet() ) {
            BuiltinPrefix bp = BuiltinPrefix.fromString(prefix);
            if ( bp != null && !bp.getPrefix().equals(curieMap.get(prefix)) ) {
                throw new SSSOMFormatException("Re-defined builtin prefix in the provided curie map");
            }
        }

        return ms;
    }

    private enum GuessedFileType {
        TSV_WITH_EMBEDDED_METADATA,
        TSV_ONLY,
        JSON
    }
}
