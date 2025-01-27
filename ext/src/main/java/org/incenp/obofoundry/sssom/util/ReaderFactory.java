/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024,2025 Damien Goutte-Gattat
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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.incenp.obofoundry.sssom.JSONReader;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.SSSOMReader;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.rdf.RDFReader;

/**
 * A class providing helper methods to obtain SSSOM reader objects.
 */
public class ReaderFactory {

    private boolean useExtension;

    /**
     * Creates a new instance.
     */
    public ReaderFactory() {
        useExtension = false;
    }

    /**
     * Creates a new instance that may optionally use a filename’s extension to
     * infer the format of the file.
     * 
     * @param useExtension If {@code true}, the factory will first try to infer the
     *                     format of a file based on its extension, before peeking
     *                     at the file’s contents.
     */
    public ReaderFactory(boolean useExtension) {
        this.useExtension = useExtension;
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the provided file.
     * 
     * @param file The file for which a SSSOM reader is desired.
     * @return A SSSOM reader suitable for the format of the data in the provided
     *         file.
     * @throws IOException          If any I/O error occurs when trying to read from
     *                              the provided file.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(File file) throws IOException, SSSOMFormatException {
        return getReader(new BufferedReader(new FileReader(file)), file.getPath());
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the provided stream.
     * 
     * @param stream The data stream for which a SSSOM reader is desired.
     * @return A SSSOM reader suitable for the format of the data in the provided
     *         stream.
     * @throws IOException          If any I/O error occurs when trying to read from
     *                              the indicated stream.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(InputStream stream) throws IOException, SSSOMFormatException {
        return getReader(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the file with the
     * provided name.
     * 
     * @param filename The name of the file for which a SSSOM reader is desired.
     * @return A SSSOM reader suitable for the format of the data in the indicated
     *         file.
     * @throws IOException          If any I/O error occurs when trying to read from
     *                              the indicated file.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(String filename) throws IOException, SSSOMFormatException {
        return getReader(new BufferedReader(new FileReader(new File(filename))), filename);
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the file with the
     * provided name.
     * 
     * @param filename   The name of the file for which a SSSOM reader is desired.
     * @param allowStdin If {@code true}, a filename consisting of a single dash
     *                   ({@code -}) is interpreted as representing the program’s
     *                   standard input.
     * @return A SSSOM reader suitable for the format of the data in the indicated
     *         file.
     * @throws IOException          If any I/O error occurs when trying to read from
     *                              the indicated file.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(String filename, boolean allowStdin) throws IOException, SSSOMFormatException {
        if ( allowStdin && filename.equals("-") ) {
            return getReader(System.in);
        }
        return getReader(new BufferedReader(new FileReader(new File(filename))), filename);
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the file with the
     * provided name.
     * 
     * @param filename     The name of the file for which a SSSOM reader is desired.
     * @param metaFilename If non-{@code null}, the name of the file containing the
     *                     dataset metadata. This automatically assumes that the
     *                     data is in the SSSOM/TSV format, which is the only format
     *                     allowing to store the metadata in a separate file.
     * @return A SSSOM reader suitable for the format of the data in the indicated
     *         file.
     * @throws IOException          If any I/O error occurs when trying to read from
     *                              the indicated files.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(String filename, String metaFilename) throws IOException, SSSOMFormatException {
        if ( metaFilename != null ) {
            return new TSVReader(filename, metaFilename);
        } else {
            return getReader(filename);
        }
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the file with the
     * provided name.
     * 
     * @param filename     The name of the file for which a SSSOM reader is desired.
     * @param metaFilename If non-{@code null}, the name of the file containing the
     *                     dataset metadata. This automatically assumes that the
     *                     data is in the SSSOM/TSV format, which is the only format
     *                     allowing to store the metadata in a separate file.
     * @param allowStdin   If {@code true}, a filename consisting of a single dash
     *                     ({@code -}) is interpreted as representing the program’s
     *                     standard input.
     * @return A SSSOM reader suitable for the format of the data in the indicated
     *         file.
     * @throws IOException          If any I/O error occurs when trying to read from
     *                              the indicate files.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(String filename, String metaFilename, boolean allowStdin)
            throws IOException, SSSOMFormatException {
        if ( metaFilename != null ) {
            if ( allowStdin ) {
                boolean tsvFromStdin = filename.equals("-");
                boolean metaFromStdin = filename.equals("-");
                if ( tsvFromStdin && metaFromStdin ) {
                    throw new IOException("Cannot read both TSV section and metadata from standard input");
                }
                InputStream tsv = tsvFromStdin ? System.in : new FileInputStream(filename);
                InputStream meta = metaFromStdin ? System.in : new FileInputStream(metaFilename);
                return new TSVReader(tsv, meta);
            } else {
                return new TSVReader(filename, metaFilename);
            }
        } else {
            return getReader(filename, allowStdin);
        }
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the provider Reader
     * object.
     * 
     * @param reader The reader for which to obtain a suitable SSSOM reader.
     * @return A SSSOM reader suitable for the format of the data in the provided
     *         reader.
     * @throws IOException          If any I/O error occurs when trying to infer the
     *                              serialisation format.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(Reader reader) throws IOException, SSSOMFormatException {
        return getReader(reader, null);
    }

    /**
     * Gets a SSSOM reader suitable for the format used in the provided Reader
     * object.
     * 
     * @param reader   The reader for which to obtain a suitable SSSOM reader.
     * @param filename The filename to use to try automatically locating the
     *                 external metadata file, if the provided reader contains
     *                 SSSOM/TSV data without an embedded metadata block. May be
     *                 {@code null}.
     * @return A SSSOM reader suitable for the format of the data in the provided
     *         reader.
     * @throws IOException          If any I/O error occurs when trying to infer the
     *                              serialisation format.
     * @throws SSSOMFormatException If no known serialisation format could be
     *                              recognised.
     */
    public SSSOMReader getReader(Reader reader, String filename) throws IOException, SSSOMFormatException {
        SSSOMReader br = null;
        SerialisationFormat format = null;
        if ( useExtension && filename != null ) {
            format = inferFormat(filename);
        }
        if ( format == null ) {
            format = inferFormat(reader);
        }
        if ( format == null ) {
            throw new SSSOMFormatException("Unrecognised SSSOM serialisation format");
        }
        switch ( format ) {
        case RDF_TURTLE:
            br = new RDFReader(reader);
            break;
        case JSON:
            br = new JSONReader(reader);
            break;
        case TSV:
            br = new TSVReader(reader, null, filename);
            break;
        }
        return br;
    }

    /**
     * Peeks inside a Reader object to try and guess the SSSOM serialisation format
     * that is being used.
     * <p>
     * This method looks at the first byte of the provided Reader object to try to
     * automatically determine the SSSOM serialisation format.
     * 
     * @param reader The reader object to peek into.
     * @return The serialisation format used, or {@code null} if it has not been
     *         possible to peek inside the reader, or if no known format has been
     *         recognised.
     * @throws IOException If any I/O error occurred when trying to peek inside the
     *                     reader.
     */
    public SerialisationFormat inferFormat(Reader reader) throws IOException {
        if ( !reader.markSupported() ) {
            return null;
        }

        reader.mark(1);
        SerialisationFormat format = null;
        int i = reader.read();
        if ( i != -1 ) {
            char c = (char) i;
            if ( c == '#' ) {
                format = SerialisationFormat.TSV;
            } else if ( c == '{' ) {
                format = SerialisationFormat.JSON;
            } else if ( c == '@' || c == '[' ) {
                format = SerialisationFormat.RDF_TURTLE;
            } else if ( Character.isLowerCase(c) ) {
                // Assume a TSV file without an embedded metadata block
                format = SerialisationFormat.TSV;
            }
        }
        reader.reset();

        return format;
    }

    private SerialisationFormat inferFormat(String filename) {
        for ( SerialisationFormat format : SerialisationFormat.values() ) {
            if ( filename.endsWith(format.getExtension()) ) {
                return format;
            }
        }
        return null;
    }
}
