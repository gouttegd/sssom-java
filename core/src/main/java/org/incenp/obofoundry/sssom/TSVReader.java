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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        return read(tsvFile == null);
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
            } else {
                File metaFile = findMetadata(tsvFile);
                metaReader = new FileReader(metaFile);
            }
        }

        return read(metaReader, metadataOnly || tsvFile == null);
    }

    /*
     * Reads a mapping set from the source file while reading the metadata from the
     * specified source.
     */
    private MappingSet read(Reader metaReader, boolean metadataOnly) throws SSSOMFormatException, IOException {
        MappingSet ms = readMetadata(metaReader);
        prefixManager.add(ms.getCurieMap());
        SlotHelper.getMappingSetHelper().expandIdentifiers(ms, prefixManager);

        if ( !metadataOnly ) {
            ObjectMapper mapper = new CsvMapper().registerModule(new JavaTimeModule());
            CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t').withNullValue("");
            MappingIterator<Mapping> it = mapper.readerFor(Mapping.class).with(schema).readValues(tsvReader);
            ArrayList<Mapping> mappings = new ArrayList<Mapping>();
            while ( it.hasNext() ) {
                Mapping m;
                try {
                    m = it.next();
                } catch ( RuntimeJsonMappingException e ) {
                    throw new SSSOMFormatException("Error when parsing TSV table", e);
                }
                SlotHelper.getMappingHelper().expandIdentifiers(m, prefixManager);
                mappings.add(m);
            }
            ms.setMappings(mappings);
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
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).registerModule(new JavaTimeModule());
        MappingSet ms;
        try {
            ms = mapper.readValue(reader, MappingSet.class);
        } catch ( JsonParseException e ) {
            throw new SSSOMFormatException("Error when reading YAML metadata", e);
        } catch ( JsonMappingException e ) {
            throw new SSSOMFormatException("Error when mapping YAML metadata", e);
        }

        // Check the provided curie map does not override the builtin prefixes
        Map<String, String> curieMap = ms.getCurieMap();
        if ( curieMap != null ) {
            for ( String prefix : curieMap.keySet() ) {
                BuiltinPrefix bp = BuiltinPrefix.fromString(prefix);
                if ( bp != null && !bp.getPrefix().equals(curieMap.get(prefix)) ) {
                    throw new SSSOMFormatException("Re-defined builtin prefix in the provided curie map");
                }
            }
        } else {
            ms.setCurieMap(new HashMap<String, String>());
        }

        return ms;
    }
}
