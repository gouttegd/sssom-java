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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.SlotVisitor;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * A helper class to convert SSSOM objects to a RDF model in the Rdf4J API.
 */
public class RDFConverter {

    private static final String SSSOM_NS = BuiltinPrefix.SSSOM.getPrefix();
    private static final String OWL_NS = BuiltinPrefix.OWL.getPrefix();

    /**
     * Converts a mapping set to a RDF model.
     * 
     * @param ms The mapping set to convert.
     * @return The corresponding Rdf4J model.
     */
    public Model toRDF(MappingSet ms) {
        Model model = new TreeModel();

        BNode set = Values.bnode();
        IRI mappingSetIRI = Values.iri(SSSOM_NS, "mapping_set");
        model.add(set, RDF.TYPE, mappingSetIRI);

        // Add mapping set metadata slots
        RDFSlotVisitor<MappingSet> visitor = new RDFSlotVisitor<MappingSet>(model, set);
        SlotHelper.getMappingSetHelper().visitSlots(ms, visitor);

        RDFSlotVisitor<Mapping> mappingVisitor = new RDFSlotVisitor<Mapping>(model, null);
        for ( Mapping mapping : ms.getMappings() ) {
            // Add individual mapping
            BNode mappingNode = Values.bnode();
            model.add(mappingNode, RDF.TYPE, Values.iri(OWL_NS, "Axiom"));
            model.add(set, Values.iri(SSSOM_NS, "mappings"), mappingNode);

            // Add mapping metadata slot
            mappingVisitor.subject = mappingNode;
            SlotHelper.getMappingHelper().visitSlots(mapping, mappingVisitor);
        }

        return model;
    }

    private class RDFSlotVisitor<T> implements SlotVisitor<T, Void> {

        Model model;
        Resource subject;

        RDFSlotVisitor(Model model, Resource subject) {
            this.model = model;
            this.subject = subject;
        }

        @Override
        public Void visit(Slot<T> slot, T object, String value) {
            Value rdfValue = null;
            if ( slot.isEntityReference() ) {
                rdfValue = Values.iri(value);
            } else if ( slot.isURI() ) {
                rdfValue = Values.literal(value, XSD.ANYURI);
            } else {
                rdfValue = Values.literal(value);
            }
            model.add(subject, Values.iri(slot.getURI()), rdfValue);
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, List<String> values) {
            boolean isEntityReference = slot.isEntityReference();
            boolean isURI = slot.isURI();
            IRI predicate = Values.iri(slot.getURI());
            for ( String value : values ) {
                Value rdfValue = null;
                if ( isEntityReference ) {
                    rdfValue = Values.iri(value);
                } else if ( isURI ) {
                    rdfValue = Values.literal(value, XSD.ANYURI);
                } else {
                    rdfValue = Values.literal(value);
                }
                model.add(subject, predicate, rdfValue);
            }
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, Double value) {
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value));
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, Map<String, String> value) {
            // Nothing to do here.
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, LocalDate value) {
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value));
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, Object value) {
            // TODO
            return null;
        }

        @Override
        public Void visitExtensionDefinitions(T object, List<ExtensionDefinition> values) {
            // TODO
            return null;
        }

        @Override
        public Void visitExtensions(T object, Map<String, ExtensionValue> values) {
            // TODO
            return null;
        }
    }
}
