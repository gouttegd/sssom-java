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
import java.util.Map;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.SlotPropagator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A parser to read a SSSOM mapping set from a JSON serialisation format.
 * <p>
 * The JSON format expected by this reader is <em>not</em> the JSON format used
 * by SSSOM-Py, which is based on JSON-LD and is currently unspecified. Instead,
 * this reader expects a “pure” JSON format that is merely a direct JSON
 * rendering of the internal SSSOM data model.
 */
public class JSONReader extends SSSOMReader {

    private Reader reader;
    private YAMLConverter converter = new YAMLConverter();

    /**
     * Creates a new instance that will read data from the specified file.
     * 
     * @param file The file to read the mapping set from.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public JSONReader(File file) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Creates a new instance that will read data from the specified stream.
     * 
     * @param stream The stream to read the mapping set from.
     */
    public JSONReader(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Creates a new instance that will read data from the specified reader.
     * 
     * @param reader The reader object to read the mapping set from.
     */
    public JSONReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * Creates a new instance that will read data from a file with the specified
     * name.
     * 
     * @param filename The name of the file to read the mapping set from.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public JSONReader(String filename) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(new File(filename)));
    }

    @Override
    public MappingSet read() throws SSSOMFormatException, IOException {
        MappingSet ms;
        converter.setExtraMetadataPolicy(extraPolicy);

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawSet = mapper.readValue(reader, Map.class);
            ms = converter.convertMappingSet(rawSet);
        } catch ( JsonParseException | JsonMappingException e ) {
            throw new SSSOMFormatException("Invalid JSON data", e);
        }
        new SlotPropagator(propagationPolicy).propagate(ms);

        // Post-reading checks
        // 1. Check that built-in prefixes are not redefined
        Map<String, String> curieMap = ms.getCurieMap();
        if ( curieMap != null ) {
            for ( String prefix : curieMap.keySet() ) {
                BuiltinPrefix bp = BuiltinPrefix.fromString(prefix);
                if ( bp != null && !bp.getPrefix().equals(curieMap.get(prefix)) ) {
                    throw new SSSOMFormatException("Re-defined builtin prefix in the provided curie map");
                }
            }
        }

        // 2. Check there are no unresolvable CURIEs
        if ( converter.getPrefixManager().getUnresolvedPrefixNames().size() > 0 ) {
            throw new SSSOMFormatException(String.format("Some prefixes are undeclared: %s",
                    String.join(", ", converter.getPrefixManager().getUnresolvedPrefixNames())));
        }

        // 3. Check individual mappings for missing slots
        validate(ms.getMappings());

        reader.close();

        return ms;
    }

}
