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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.incenp.obofoundry.sssom.BaseWriter;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A writer to serialise a SSSOM mapping set into the RDF Turtle format.
 */
public class RDFWriter extends BaseWriter {
    
    private static final String DCTERMS_NS = "http://purl.org/dc/terms/";

    private org.eclipse.rdf4j.rio.RDFWriter writer;

    /**
     * Creates a new instance that will write data to the specified file.
     * 
     * @param file The file to write the mapping set to.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public RDFWriter(File file) throws FileNotFoundException {
        writer = Rio.createWriter(RDFFormat.TURTLE, new FileOutputStream(file));
        writer.getWriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true);
    }

    /**
     * Creates a new instance that will write data to a file with the specified
     * filename.
     * 
     * @param filename The name of the file to write the mapping set to.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public RDFWriter(String filename) throws FileNotFoundException {
        this(new File(filename));
    }

    @Override
    protected void doWrite(MappingSet mappingSet) throws IOException {
        RDFConverter converter = new RDFConverter();
        Model rdfSet = converter.toRDF(mappingSet);

        // Add all effectively used prefixes
        for ( String prefixName : getUsedPrefixes(mappingSet, true) ) {
            rdfSet.setNamespace(prefixName, prefixManager.getPrefix(prefixName));
        }

        // Those two prefixes are always used in any RDF conversion
        rdfSet.setNamespace(BuiltinPrefix.SSSOM.getPrefixName(), BuiltinPrefix.SSSOM.getPrefix());
        rdfSet.setNamespace(BuiltinPrefix.OWL.getPrefixName(), BuiltinPrefix.OWL.getPrefix());

        // FIXME Those two prefixes MAY be used, but for now we systematically include
        // them instead of taking the time to check whether they are needed or not.
        rdfSet.setNamespace("dcterms", DCTERMS_NS);
        rdfSet.setNamespace(BuiltinPrefix.XSD.getPrefixName(), BuiltinPrefix.XSD.getPrefix());

        Rio.write(rdfSet, writer);
    }
}
