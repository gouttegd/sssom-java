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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityReferenceSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;
import org.incenp.obofoundry.sssom.slots.URISlot;

/**
 * A helper class to convert SSSOM objects from a RDF model in the Rdf4J API.
 */
public class RDFConverter {

    /**
     * Converts a RDF model to a MappingSet object.
     * 
     * @param model The Rdf4J model to convert.
     * @return The corresponding mapping set.
     * @throws SSSOMFormatException If the contents of the model does not match what
     *                              is expected for a SSSOM MappingSet object.
     */
    public MappingSet convertMappingSet(Model model) throws SSSOMFormatException {
        Model root = model.filter(null, RDF.TYPE, Constants.SSSOM_MAPPING_SET);
        Optional<BNode> set = Models.subjectBNode(root);
        if ( set.isEmpty() ) {
            throw new SSSOMFormatException("RDF model does not contain a SSSOM mapping set");
        }

        MappingSet ms = new MappingSet();
        ms.setMappings(new ArrayList<Mapping>());
        SlotSetterVisitor<MappingSet> visitor = new SlotSetterVisitor<MappingSet>();

        // Process all statements about the mapping set node
        for ( Statement st : model.filter(set.get(), null, null) ) {
            Slot<MappingSet> slot = SlotHelper.getMappingSetHelper().getSlotByURI(st.getPredicate().stringValue());
            if ( slot != null ) {
                // Statement is a mapping set metadata slot
                visitor.rdfValue = st.getObject();
                slot.accept(visitor, ms, null);
                if ( visitor.error != null ) {
                    throw visitor.error;
                }
            } else if ( st.getPredicate().equals(Constants.SSSOM_MAPPINGS) ) {
                // Statement is an individual mapping
                Value o = st.getObject();
                if ( o instanceof BNode ) {
                    ms.getMappings().add(convertMapping(model.filter((BNode) o, null, null)));
                } else {
                    throw getTypingError("mappings");
                }
            }
        }

        // Fill in the Curie map from the model's namespaces
        ms.setCurieMap(new HashMap<String, String>());
        for ( Namespace ns : model.getNamespaces() ) {
            ms.getCurieMap().put(ns.getPrefix(), ns.getName());
        }

        return ms;
    }

    /**
     * Converts a RDF model to a Mapping object.
     * 
     * @param model The Rdf4J model to convert.
     * @return The corresponding mapping.
     * @throws SSSOMFormatException If the contents of the model does not match what
     *                              is expected for a SSSOM Mapping object.
     */
    public Mapping convertMapping(Model model) throws SSSOMFormatException {
        Model root = model.filter(null, RDF.TYPE, Constants.OWL_AXIOM);
        Optional<BNode> mappingNode = Models.subjectBNode(root);
        if ( mappingNode.isEmpty() ) {
            throw new SSSOMFormatException("RDF model does not contain a Mapping object");
        }

        Mapping mapping = new Mapping();
        SlotSetterVisitor<Mapping> visitor = new SlotSetterVisitor<Mapping>();
        for ( Statement st : model.filter(mappingNode.get(), null, null) ) {
            Slot<Mapping> slot = SlotHelper.getMappingHelper().getSlotByURI(st.getPredicate().stringValue());
            if ( slot != null ) {
                // Statement is a mapping metadata slot
                visitor.rdfValue = st.getObject();
                slot.accept(visitor, mapping, null);
                if ( visitor.error != null ) {
                    throw visitor.error;
                }
            }
        }

        return mapping;
    }

    /*
     * Called upon a mismatch between the contents of the RDF model and what is
     * expected by the SSSOM data model.
     */
    private SSSOMFormatException getTypingError(String slotName, Throwable innerException) {
        return new SSSOMFormatException(String.format("Typing error when parsing '%s'", slotName), innerException);
    }

    /*
     * Same, but without an exception as the cause for the mismatch.
     */
    private SSSOMFormatException getTypingError(String slotName) {
        return getTypingError(slotName, null);
    }

    /*
     * Helper visitor to set slot values from the contents of the RDF model.
     */
    private class SlotSetterVisitor<T> extends SlotVisitorBase<T> {

        Value rdfValue;
        SSSOMFormatException error;

        @Override
        public void visit(StringSlot<T> slot, T target, String unused) {
            if ( !(rdfValue instanceof Literal) ) {
                error = getTypingError(slot.getName());
            } else {
                slot.setValue(target, rdfValue.stringValue());
            }
        }

        @Override
        public void visit(StringSlot<T> slot, T target, List<String> unused) {
            if ( !(rdfValue instanceof Literal) ) {
                error = getTypingError(slot.getName());
            } else {
                slot.setValue(target, rdfValue.stringValue());
            }
        }

        @Override
        public void visit(URISlot<T> slot, T target, String unused) {
            if ( !(rdfValue instanceof Literal) || !((Literal) rdfValue).getDatatype().equals(XSD.ANYURI) ) {
                error = getTypingError(slot.getName());
            } else {
                slot.setValue(target, rdfValue.stringValue());
            }
        }

        @Override
        public void visit(URISlot<T> slot, T target, List<String> unused) {
            if ( !(rdfValue instanceof Literal) || !((Literal) rdfValue).getDatatype().equals(XSD.ANYURI) ) {
                error = getTypingError(slot.getName());
            } else {
                slot.setValue(target, rdfValue.stringValue());
            }
        }

        @Override
        public void visit(EntityReferenceSlot<T> slot, T target, String unused) {
            if ( !(rdfValue instanceof IRI) ) {
                error = getTypingError(slot.getName());
            } else {
                slot.setValue(target, rdfValue.stringValue());
            }
        }

        @Override
        public void visit(EntityReferenceSlot<T> slot, T target, List<String> unused) {
            if ( !(rdfValue instanceof IRI) ) {
                error = getTypingError(slot.getName());
            } else {
                slot.setValue(target, rdfValue.stringValue());
            }
        }

        public void visit(DoubleSlot<T> slot, T target, Double unused) {
            if ( !(rdfValue instanceof Literal) ) {
                error = getTypingError(slot.getName());
                return;
            }
            Literal litValue = (Literal) rdfValue;
            try {
                slot.setValue(target, litValue.doubleValue());
            } catch ( NumberFormatException e ) {
                error = getTypingError(slot.getName());
            } catch ( IllegalArgumentException e ) {
                error = new SSSOMFormatException(String.format("Out-of-range value for '%s'", slot.getName()));
            }
        }

        // Covers all enum-based slots
        @Override
        public void visit(Slot<T> slot, T target, Object unused) {
            try {
                slot.setValue(target, rdfValue.stringValue());
            } catch ( IllegalArgumentException e ) {
                error = new SSSOMFormatException(String.format("Typing error when parsing '%s'", slot.getName()));
            }
        }
    }
}
