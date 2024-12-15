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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

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

        // Process all statements about the mapping set node
        for ( Statement st : model.filter(set.get(), null, null) ) {
            if ( st.getPredicate().equals(Constants.SSSOM_EXT_DEFINITIONS) ) {
                // Statement is an extension definition; we do not support those for now
                continue;
            }
            Slot<MappingSet> slot = SlotHelper.getMappingSetHelper().getSlotByURI(st.getPredicate().stringValue());
            if ( slot != null ) {
                // Statement is a mapping set metadata slot
                setSlotFromRDF(ms, slot, st.getObject());
            } else if ( st.getPredicate().equals(Constants.SSSOM_MAPPINGS) ) {
                // Statement is an individual mapping
                Value o = st.getObject();
                if ( o instanceof BNode ) {
                    ms.getMappings().add(convertMapping(model.filter((BNode) o, null, null)));
                } else {
                    onTypingError("mappings");
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
        for ( Statement st : model.filter(mappingNode.get(), null, null) ) {
            Slot<Mapping> slot = SlotHelper.getMappingHelper().getSlotByURI(st.getPredicate().stringValue());
            if ( slot != null ) {
                // Statement is a mapping metadata slot
                setSlotFromRDF(mapping, slot, st.getObject());
            }
        }

        return mapping;
    }

    /*
     * Helper methods to convert RDF objects to SSSOM objects.
     */

    /*
     * Assigns a value to a SSSOM metadata slot from a RDF value.
     */
    private <T> void setSlotFromRDF(T target, Slot<T> slot, Value rdfValue) throws SSSOMFormatException {
        if ( slot.getType().equals(String.class) ) {
            slot.setValue(target, getStringValue(slot, rdfValue));
        } else if ( slot.getType().equals(List.class) ) {
            String value = getStringValue(slot, rdfValue);
            @SuppressWarnings("unchecked")
            List<String> list = List.class.cast(slot.getValue(target));
            if ( list == null ) {
                list = new ArrayList<String>();
                slot.setValue(target, list);
            }
            list.add(value);
        } else if ( slot.getType().equals(LocalDate.class) ) {
            try {
                String rawDate = getLiteralValue(slot.getName(), rdfValue).stringValue();
                if ( rawDate.contains("T") ) {
                    slot.setValue(target, LocalDateTime.parse(rawDate).toLocalDate());
                } else {
                    slot.setValue(target, LocalDate.parse(rawDate));
                }
            } catch ( DateTimeParseException e ) {
                onTypingError(slot.getName(), e);
            }
        } else if ( slot.getType().equals(Double.class) ) {
            try {
                slot.setValue(target, getLiteralValue(slot.getName(), rdfValue).doubleValue());
            } catch ( NumberFormatException e ) {
                onTypingError(slot.getName(), e);
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMFormatException(String.format("Out-of-range value for '%s'", slot.getName()));
            }
        } else if ( slot.getType().equals(EntityType.class) ) {
            EntityType value = null;
            if ( rdfValue instanceof IRI ) {
                value = EntityType.fromIRI(rdfValue.stringValue());
            } else if ( rdfValue instanceof Literal ) {
                value = EntityType.fromString(rdfValue.stringValue());
            }
            if ( value == null ) {
                onTypingError(slot.getName());
            }
            slot.setValue(target, value);
        } else if ( slot.getType().equals(MappingCardinality.class) ) {
            MappingCardinality value = MappingCardinality
                    .fromString(getLiteralValue(slot.getName(), rdfValue).stringValue());
            if ( value == null ) {
                onTypingError(slot.getName());
            }
            slot.setValue(target, value);
        } else if ( slot.getType().equals(PredicateModifier.class) ) {
            PredicateModifier value = PredicateModifier
                    .fromString(getLiteralValue(slot.getName(), rdfValue).stringValue());
            if ( value == null ) {
                onTypingError(slot.getName());
            }
            slot.setValue(target, value);
        }
    }

    /*
     * Converts a RDF value to a value suitable for a string-typed slot, taking into
     * accounts whether the slot represents an entity reference, an URI, or a
     * literal string.
     */
    private <T> String getStringValue(Slot<T> slot, Value rdfValue) throws SSSOMFormatException {
        if ( slot.isEntityReference() ) {
            if ( !(rdfValue instanceof IRI) ) {
                onTypingError(slot.getName());
            }
            return ((IRI) rdfValue).getNamespace() + ((IRI) rdfValue).getLocalName();
        } else if ( slot.isURI() ) {
            Literal litValue = getLiteralValue(slot.getName(), rdfValue);
            if ( !litValue.getDatatype().equals(XSD.ANYURI) ) {
                onTypingError(slot.getName());
            }
            return litValue.stringValue();
        } else {
            return getLiteralValue(slot.getName(), rdfValue).stringValue();
        }
    }

    /*
     * Ensures a RDF value is a literal value, or throws a format error.
     */
    private Literal getLiteralValue(String slotName, Value rdfValue) throws SSSOMFormatException {
        if ( !(rdfValue instanceof Literal) ) {
            onTypingError(slotName);
        }
        return (Literal) rdfValue;
    }

    /*
     * Called upon a mismatch between the contents of the RDF model and what is
     * expected by the SSSOM data model.
     */
    private void onTypingError(String slotName, Throwable innerException) throws SSSOMFormatException {
        throw new SSSOMFormatException(String.format("Typing error when parsing '%s'", slotName), innerException);
    }

    /*
     * Same, but without an exception as the cause for the mismatch.
     */
    private void onTypingError(String slotName) throws SSSOMFormatException {
        onTypingError(slotName, null);
    }
}
