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
import java.util.Map;

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

/**
 * A parser to read a SSSOM mapping set from the TSV serialisation format.
 */
public class TSVReader {

    private File tsvFile;
    private BufferedReader tsvReader;
    private Reader metaReader;

    /**
     * Create a new instance that will read data from the specified files.
     * 
     * @param tsvFile  The main TSV file.
     * @param metaFile The accompanying metadata file. If {@code null}, the parser
     *                 will attempt to automatically locate the metadata from the
     *                 main file.
     * @throws FileNotFoundException If any of the files cannot be found.
     */
    public TSVReader(File tsvFile, File metaFile) throws FileNotFoundException {
        this.tsvFile = tsvFile;
        tsvReader = new BufferedReader(new FileReader(tsvFile));
        if ( metaFile != null ) {
            metaReader = new FileReader(metaFile);
        }
    }

    /**
     * Create a new instance that will read data from a single file. That file
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
     * Create a new instance that will read data from the specified files.
     * 
     * @param tsvFile  The name of the main TSV file.
     * @param metaFile The name of the accompanying metadata file.
     * @throws FileNotFoundException If any of the files cannot be found.
     */
    public TSVReader(String tsvFile, String metaFile) throws FileNotFoundException {
        this(new File(tsvFile), new File(metaFile));
    }

    /**
     * Create a new instance that will read data from a single file. That file
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
     * Read a mapping set from the source file(s).
     * 
     * @return A complete SSSOM mapping set.
     * @throws SSSOMFormatException If encountering invalid SSSOM data. This
     *                              includes the case where the metadata cannot be
     *                              found.
     * @throws IOException          If any kind of non-SSSOM-related I/O error
     *                              occurs.
     */
    public MappingSet read() throws SSSOMFormatException, IOException {
        if ( metaReader == null ) {
            if ( hasEmbeddedMetadata(tsvReader) ) {
                metaReader = new StringReader(extractMetadata(tsvReader));
            } else {
                File metaFile = findMetadata(tsvFile);
                metaReader = new FileReader(metaFile);
            }
        }

        return read(metaReader);
    }

    /**
     * Read a mapping set from the source file while reading the metadata from the
     * specified source.
     * 
     * @param metaReader A Reader from which to get the metadata.
     * @return A complete SSSOM mapping set.
     * @throws SSSOMFormatException If encountering invalid SSSOM data.
     * @throws IOException          If any kind of non-SSSOM-related I/O error
     *                              occurs.
     */
    public MappingSet read(Reader metaReader) throws SSSOMFormatException, IOException {
        MappingSet ms = readMetadata(metaReader);

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t');
        MappingIterator<Mapping> it = mapper.readerFor(Mapping.class).with(schema).readValues(tsvReader);
        ArrayList<Mapping> mappings = new ArrayList<Mapping>();
        while ( it.hasNext() ) {
            Mapping m;
            try {
                m = it.next();
            } catch ( RuntimeJsonMappingException e ) {
                throw new SSSOMFormatException("Error when parsing TSV table", e);
            }
            m.setSubjectId(expandCurie(ms.getCurieMap(), m.getSubjectId()));
            m.setPredicateId(expandCurie(ms.getCurieMap(), m.getPredicateId()));
            m.setObjectId(expandCurie(ms.getCurieMap(), m.getObjectId()));
            mappings.add(m);
        }
        ms.setMappings(mappings);

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
                throw new SSSOMFormatException("Exernal metadata file not found");
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
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MappingSet ms;
        try {
            ms = mapper.readValue(reader, MappingSet.class);
        } catch ( JsonParseException e ) {
            throw new SSSOMFormatException("Error when reading YAML metadata", e);
        } catch ( JsonMappingException e ) {
            throw new SSSOMFormatException("Error when mapping YAML metadata", e);
        }

        return ms;
    }

    /*
     * Expand a CURIE from the provided prefix map.
     */
    private String expandCurie(Map<String, String> curieMap, String curie) {
        if ( curie.startsWith("http") ) {
            // The SSSOM TSV format allows using full (non-CURIEfied) IRIs, even though it's
            // discouraged.
            return curie;
        }

        String[] parts = curie.split(":", 2);
        if ( curieMap.containsKey(parts[0]) ) {
            return curieMap.get(parts[0]) + parts[1];
        }

        // This should not happen as the spec says that it is "required to provide a
        // prefix map that allows the unambiguous interpretation of CURIEs".
        // Consequently, one could argue that a TSV file containing a CURIE whose prefix
        // is not in the prefix map is invalid, and we could legitimately throw a
        // SSSOMFormatException here. For now we simply let the original identifier as
        // it is.
        return curie;
    }
}
