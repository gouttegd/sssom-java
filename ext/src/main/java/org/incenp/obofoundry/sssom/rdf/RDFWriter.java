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
import java.io.OutputStream;

import org.eclipse.rdf4j.common.text.StringUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.incenp.obofoundry.sssom.SSSOMWriter;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A writer to serialise a SSSOM mapping set into the RDF Turtle format.
 */
public class RDFWriter extends SSSOMWriter {
    
    private static final String DCTERMS_NS = "http://purl.org/dc/terms/";
    private static final String PAV_NS = "http://purl.org/pav/";

    private org.eclipse.rdf4j.rio.RDFWriter writer;

    /**
     * Creates a new instance that will write data to the specified file.
     * 
     * @param file The file to write the mapping set to.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public RDFWriter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
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

    /**
     * Creates a new instance that will write data to the specified stream.
     * 
     * @param stream The stream to write the mapping set to.
     */
    public RDFWriter(OutputStream stream) {
        writer = new CustomTurtleWriter(stream);
        writer.getWriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true);
    }

    @Override
    protected void doWrite(MappingSet mappingSet) throws IOException {
        // We might need those prefixes as some SSSOM slots are represented in RDF using
        // properties in the corresponding namespaces.
        prefixManager.add("dcterms", DCTERMS_NS);
        prefixManager.add("pav", PAV_NS);

        condenseSet(mappingSet);
        RDFConverter converter = new RDFConverter(extraPolicy);
        Model rdfSet = converter.toRDF(mappingSet, prefixManager);

        Rio.write(rdfSet, writer);
    }

    /*
     * We need a custom Turtle writer as Rdf4J's default writer assumes that IRI
     * prefixes can only end on the boundaries of IRI components, which is not
     * suitable for many of the IRI prefixes we may have to deal with.
     */
    private class CustomTurtleWriter extends TurtleWriter {

        public CustomTurtleWriter(OutputStream out) {
            super(out);
        }

        @Override
        protected void writeURI(IRI uri) throws IOException {
            // We delegate all the IRI shortening logic to our own PrefixManager class.
            String original = uri.stringValue();
            String prefixName = prefixManager.getPrefixName(original);
            if ( prefixName == null ) {
                writer.write("<");
                StringUtil.simpleEscapeIRI(original, writer, false);
                writer.write(">");
            } else {
                int prefixLen = prefixManager.getPrefix(prefixName).length();
                writer.write(prefixName);
                writer.write(":");
                writer.write(escapeShortIRI(original.substring(prefixLen)));
            }
        }

        private String escapeShortIRI(String localName) {
            /* ~.-!$&'()*+,;=/?#@%_ */
            StringBuffer sb = new StringBuffer();
            int len = localName.length();
            for ( int i = 0; i < len; i++ ) {
                char c = localName.charAt(i);
                switch ( c ) {
                case '~':
                case '!':
                case '$':
                case '&':
                case '\'':
                case '(':
                case ')':
                case '*':
                case '+':
                case ',':
                case ';':
                case '=':
                case '/':
                case '?':
                case '#':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(c);
                    break;

                default:
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
