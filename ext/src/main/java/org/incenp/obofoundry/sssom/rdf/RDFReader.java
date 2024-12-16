/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.incenp.obofoundry.sssom.SSSOMReader;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A parser to read a SSSOM mapping set from the RDF Turtle serialisation
 * format.
 */
public class RDFReader extends SSSOMReader {

    private Reader reader;

    /**
     * Creates a new instance that will read data from the specified file.
     * 
     * @param file The file to read the mapping set from.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public RDFReader(File file) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Creates a new instance that will read data from the specified stream.
     * 
     * @param stream The stream to read the mapping set from.
     */
    public RDFReader(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Creates a new instance that will read data from the specified reader.
     * 
     * @param reader The reader to read the mapping set from.
     */
    public RDFReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * Creates a new instance that will read data from a file with the specified
     * name.
     * 
     * @param filename The name of the file to read the mapping set from.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public RDFReader(String filename) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(new File(filename)));
    }

    @Override
    public MappingSet read() throws SSSOMFormatException, IOException {
        RDFConverter converter = new RDFConverter();
        Model model = Rio.parse(reader, RDFFormat.TURTLE);
        reader.close();

        MappingSet ms = converter.convertMappingSet(model);

        Map<String, String> curieMap = ms.getCurieMap();
        if ( curieMap != null ) {
            for ( String prefix : curieMap.keySet() ) {
                BuiltinPrefix bp = BuiltinPrefix.fromString(prefix);
                if ( bp != null && !bp.getPrefix().equals(curieMap.get(prefix)) ) {
                    throw new SSSOMFormatException("Re-defined builtin prefix in the provided curie map");
                }
            }
        }

        validate(ms.getMappings());

        return ms;
    }
}
