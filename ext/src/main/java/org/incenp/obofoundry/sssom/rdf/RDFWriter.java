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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.common.text.StringUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.incenp.obofoundry.sssom.BaseWriter;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.SlotVisitor;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A writer to serialise a SSSOM mapping set into the RDF Turtle format.
 */
public class RDFWriter extends BaseWriter {
    
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
        RDFConverter converter = new RDFConverter(extraPolicy);
        Model rdfSet = converter.toRDF(mappingSet);

        // We might need those prefixes as some SSSOM slots are represented in RDF using
        // properties in the corresponding namespaces.
        prefixManager.add("dcterms", DCTERMS_NS);
        prefixManager.add("pav", PAV_NS);

        // Add all effectively used prefixes
        for ( String prefixName : getUsedPrefixes(mappingSet) ) {
            rdfSet.setNamespace(prefixName, prefixManager.getPrefix(prefixName));
        }

        Rio.write(rdfSet, writer);
    }

    /*
     * The getUsedPrefixes method in the BaseWriter class is not enough as it only
     * gives the prefixes used in mappings' EntityReference slots. For RDF
     * serialisation we also need the prefixes used by the properties that represent
     * the slots themselves, and maybe also their types.
     */
    @Override
    protected Set<String> getUsedPrefixes(MappingSet mappingSet) {
        HashSet<String> usedPrefixes = new HashSet<String>();
        SlotHelper.getMappingSetHelper().visitSlots(mappingSet, new PrefixUsageVisitorEx<MappingSet>(usedPrefixes));
        PrefixUsageVisitorEx<Mapping> puv = new PrefixUsageVisitorEx<Mapping>(usedPrefixes);
        for ( Mapping mapping : mappingSet.getMappings() ) {
            SlotHelper.getMappingHelper().visitSlots(mapping, puv);
        }

        return usedPrefixes;
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
            String shortIRI = prefixManager.shortenIdentifier(original);
            if ( original == shortIRI ) {
                writer.write("<");
                StringUtil.simpleEscapeIRI(original, writer, false);
                writer.write(">");
            } else {
                writer.write(shortIRI);
            }
        }
    }

    /*
     * Used by getUsedPrefixes. Visits all slots in a mapping or mapping set to
     * record all the IRI prefixes needed to serialise the object in RDF.
     */
    private class PrefixUsageVisitorEx<T> implements SlotVisitor<T, Void> {

        Set<String> usedPrefixes;

        PrefixUsageVisitorEx(Set<String> usedPrefixes) {
            this.usedPrefixes = usedPrefixes;
        }

        private void maybeAddPrefix(String iri) {
            String prefix = prefixManager.getPrefixName(iri);
            if ( prefix != null ) {
                usedPrefixes.add(prefix);
            }
        }

        @Override
        public Void visit(Slot<T> slot, T object, String value) {
            maybeAddPrefix(slot.getURI());
            if ( slot.isEntityReference() ) {
                maybeAddPrefix(value);
            }
            if ( slot.isURI() ) {
                usedPrefixes.add(BuiltinPrefix.XSD.getPrefixName());
            }
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, List<String> values) {
            maybeAddPrefix(slot.getURI());
            if ( slot.isEntityReference() ) {
                for ( String value : values ) {
                    maybeAddPrefix(value);
                }
            }
            if ( slot.isURI() ) {
                usedPrefixes.add(BuiltinPrefix.XSD.getPrefixName());
            }
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, Double value) {
            maybeAddPrefix(slot.getURI());
            usedPrefixes.add(BuiltinPrefix.XSD.getPrefixName());
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, Map<String, String> values) {
            maybeAddPrefix(slot.getURI());
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, LocalDate value) {
            maybeAddPrefix(slot.getURI());
            usedPrefixes.add(BuiltinPrefix.XSD.getPrefixName());
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, Object value) {
            maybeAddPrefix(slot.getURI());
            if ( EntityType.class.isInstance(value) ) {
                EntityType et = EntityType.class.cast(value);
                String iri = et.getIRI();
                if ( iri != null ) {
                    maybeAddPrefix(iri);
                }
            }
            return null;
        }

        @Override
        public Void visitExtensionDefinitions(T object, List<ExtensionDefinition> values) {
            if ( extraPolicy == ExtraMetadataPolicy.DEFINED ) {
                for ( ExtensionDefinition ed : values ) {
                    maybeAddPrefix(ed.getProperty());
                    if ( ed.getTypeHint() != null ) {
                        maybeAddPrefix(ed.getTypeHint());
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitExtensions(T object, Map<String, ExtensionValue> values) {
            if ( extraPolicy != ExtraMetadataPolicy.NONE ) {
                for ( Entry<String, ExtensionValue> entry : values.entrySet() ) {
                    maybeAddPrefix(entry.getKey());
                    ExtensionValue value = entry.getValue();
                    if ( value != null ) {
                        if ( value.isIdentifier() ) {
                            maybeAddPrefix(value.asString());
                        } else {
                            usedPrefixes.add(BuiltinPrefix.XSD.getPrefixName());
                        }
                    }
                }
            }
            return null;
        }
    }
}
