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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.incenp.obofoundry.sssom.DefaultMappingComparator;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.slots.DateSlot;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityReferenceSlot;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionDefinitionSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionSlot;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.PredicateModifierSlot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;
import org.incenp.obofoundry.sssom.slots.URISlot;

/**
 * A helper class to convert SSSOM objects to a RDF model in the Rdf4J API.
 */
public class RDFSerialiser {

    private ExtraMetadataPolicy extraPolicy;
    int bnodeCounter = 0;

    /**
     * Creates a new instance with the default policy for serialising non-standard
     * metadata, which is to serialise them as “undefined” (without the extension
     * definitions).
     */
    public RDFSerialiser() {
        extraPolicy = ExtraMetadataPolicy.UNDEFINED;
    }

    /**
     * Creates a new instance with an explicit policy for serialising non-standard
     * metadata.
     * 
     * @param policy The non-standard metadata policy.
     */
    public RDFSerialiser(ExtraMetadataPolicy policy) {
        extraPolicy = policy;
    }

    /**
     * Converts a MappingSet to a Rdf4J model.
     * 
     * @param ms The mapping set to convert.
     * @return The corresponding RDF model.
     */
    public Model toRDF(MappingSet ms) {
        return toRDF(ms, (PrefixManager) null);
    }

    /**
     * Converts a MappingSet to a Rdf4J model, optionally including namespace
     * declarations for the prefixes found in the set’s prefix map.
     * <p>
     * The default behaviour is <em>not</em> to include namespace declarations,
     * which means that, if the model is later serialised to file, all identifiers
     * would be rendered using their long form. Including namespace declarations for
     * all the prefixes set forth in the set’s prefix map allows identifiers to be
     * serialised in short form.
     * 
     * @param ms                The mapping set to convert.
     * @param includeNamespaces If {@code true}, add a namespace declaration for
     *                          every prefix in the set’s prefix map, if that prefix
     *                          is effectively used in the set.
     * @return The corresponding RDF model.
     */
    public Model toRDF(MappingSet ms, boolean includeNamespaces) {
        PrefixManager pm = null;
        if ( includeNamespaces ) {
            pm = new PrefixManager();
            pm.add(ms.getCurieMap());
        }
        return toRDF(ms, pm);
    }

    /**
     * Converts a MappingSet to a Rdf4J model, including namespace declarations for
     * all prefixes in the specified prefix map.
     * <p>
     * Note that “builtin prefixes” are automatically added to the given prefix map.
     * 
     * @param ms        The mapping set to convert.
     * @param prefixMap A map of prefix name to URI prefixes to add to the model as
     *                  namespace declarations, for the prefixes that are
     *                  effectively used in the set. The set’s own prefix map will
     *                  be ignored.
     * @return The corresponding RDF model.
     */
    public Model toRDF(MappingSet ms, Map<String, String> prefixMap) {
        PrefixManager pm = new PrefixManager();
        pm.add(prefixMap);
        return toRDF(ms, pm);
    }

    /**
     * Converts a MappingSet to a Rdf4J model, optional including namespace
     * declarations for all prefixes held in the specified PrefixManager.
     * 
     * @param ms            The mapping set to convert.
     * @param prefixManager A prefix manager holding the prefixes to add to the
     *                      model if they are effectively used in the set. The set’s
     *                      own prefix map will be ignored. May be {@code null}, in
     *                      which case no namespaces will be added to the model.
     * @return The corresponding RDF model.
     */
    public Model toRDF(MappingSet ms, PrefixManager prefixManager) {
        Model model = new TreeModel();
        Set<String> usedPrefixes = prefixManager != null ? new HashSet<String>() : null;

        // Create the mapping set node
        BNode set = Values.bnode(String.valueOf(bnodeCounter++));
        model.add(set, RDF.TYPE, Constants.SSSOM_MAPPING_SET);

        // Add the set-level metadata
        RDFSlotVisitor<MappingSet> setVisitor = new RDFSlotVisitor<MappingSet>(model, set, prefixManager, usedPrefixes);
        SlotHelper.getMappingSetHelper().visitSlots(ms, setVisitor);

        RDFSlotVisitor<Mapping> mappingVisitor = new RDFSlotVisitor<Mapping>(model, null, prefixManager, usedPrefixes);
        ms.getMappings().sort(new DefaultMappingComparator());
        for ( Mapping mapping : ms.getMappings() ) {
            // Add individual mapping
            BNode mappingNode = Values.bnode(String.valueOf(bnodeCounter++));
            model.add(mappingNode, RDF.TYPE, Constants.OWL_AXIOM);
            model.add(set, Constants.SSSOM_MAPPINGS, mappingNode);

            // Add mapping metadata slots
            mappingVisitor.subject = mappingNode;
            SlotHelper.getMappingHelper().visitSlots(mapping, mappingVisitor);
        }

        // Add namespace declarations
        if ( usedPrefixes != null ) {
            // Those prefixes are always used no matter what
            usedPrefixes.add(BuiltinPrefix.SSSOM.getPrefixName());
            usedPrefixes.add(BuiltinPrefix.OWL.getPrefixName());

            for ( String prefixName : usedPrefixes ) {
                model.setNamespace(prefixName, prefixManager.getPrefix(prefixName));
            }
        }

        return model;
    }

    /*
     * Helper visitor to fill in the RDF model (including namespaces) from slot
     * values.
     */
    private class RDFSlotVisitor<T> extends SlotVisitorBase<T> {

        Model model;
        Resource subject;
        PrefixManager pfxMgr;
        Set<String> prefixes;

        RDFSlotVisitor(Model model, Resource subject, PrefixManager prefixManager, Set<String> usedPrefixes) {
            this.model = model;
            this.subject = subject;
            pfxMgr = prefixManager;
            prefixes = usedPrefixes;
        }

        private void recordUsedIRI(String iri) {
            if ( pfxMgr != null && prefixes != null ) {
                String prefix = pfxMgr.getPrefixName(iri);
                if ( prefix != null ) {
                    prefixes.add(prefix);
                }
            }
        }

        private void recordUsedPrefix(BuiltinPrefix prefix) {
            if ( prefixes != null ) {
                prefixes.add(prefix.getPrefixName());
            }
        }

        @Override
        public void visit(StringSlot<T> slot, T object, String value) {
            recordUsedIRI(slot.getURI());
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value));
        }

        @Override
        public void visit(URISlot<T> slot, T object, String value) {
            recordUsedIRI(slot.getURI());
            recordUsedPrefix(BuiltinPrefix.XSD);
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value, XSD.ANYURI));
        }

        @Override
        public void visit(EntityReferenceSlot<T> slot, T object, String value) {
            recordUsedIRI(slot.getURI());
            recordUsedIRI(value);
            model.add(subject, Values.iri(slot.getURI()), Values.iri(value));
        }

        @Override
        public void visit(StringSlot<T> slot, T object, List<String> values) {
            recordUsedIRI(slot.getURI());
            for ( String value : values ) {
                model.add(subject, Values.iri(slot.getURI()), Values.literal(value));
            }
        }

        @Override
        public void visit(URISlot<T> slot, T object, List<String> values) {
            recordUsedIRI(slot.getURI());
            recordUsedPrefix(BuiltinPrefix.XSD);
            for ( String value : values ) {
                model.add(subject, Values.iri(slot.getURI()), Values.literal(value, XSD.ANYURI));
            }
        }

        @Override
        public void visit(EntityReferenceSlot<T> slot, T object, List<String> values) {
            recordUsedIRI(slot.getURI());
            for ( String value : values ) {
                recordUsedIRI(value);
                model.add(subject, Values.iri(slot.getURI()), Values.iri(value));
            }
        }

        @Override
        public void visit(DoubleSlot<T> slot, T object, Double value) {
            recordUsedIRI(slot.getURI());
            recordUsedPrefix(BuiltinPrefix.XSD);
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value));
        }

        @Override
        public void visit(DateSlot<T> slot, T object, LocalDate value) {
            recordUsedIRI(slot.getURI());
            recordUsedPrefix(BuiltinPrefix.XSD);
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value));
        }

        @Override
        public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
            recordUsedIRI(slot.getURI());
            String valIRI = value.getIRI();
            Value rdfValue = null;
            if ( valIRI != null ) {
                rdfValue = Values.iri(valIRI);
                recordUsedIRI(valIRI);
            } else {
                rdfValue = Values.literal(value.toString());
            }
            model.add(subject, Values.iri(slot.getURI()), rdfValue);
        }

        @Override
        public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value) {
            recordUsedIRI(slot.getURI());
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value.toString()));
        }

        @Override
        public void visit(PredicateModifierSlot<T> slot, T object, PredicateModifier value) {
            recordUsedIRI(slot.getURI());
            model.add(subject, Values.iri(slot.getURI()), Values.literal(value.toString()));
        }

        @Override
        public void visit(ExtensionDefinitionSlot<T> slot, T object, List<ExtensionDefinition> values) {
            if ( extraPolicy != ExtraMetadataPolicy.DEFINED ) {
                return;
            }

            values.sort((a, b) -> a.getProperty().compareTo(b.getProperty()));
            for ( ExtensionDefinition ed : values ) {
                BNode edNode = Values.bnode(String.valueOf(bnodeCounter++));
                // FIXME: The SSSOM spec does not say how extension definitions should be
                // serialised in RDF.
                model.add(edNode, Constants.SSSOM_EXT_PROPERTY, Values.iri(ed.getProperty()));
                recordUsedIRI(ed.getProperty());
                if ( ed.getSlotName() != null ) {
                    model.add(edNode, Constants.SSSOM_EXT_SLOTNAME, Values.literal(ed.getSlotName()));
                }
                if ( ed.getTypeHint() != null ) {
                    model.add(edNode, Constants.SSSOM_EXT_TYPEHINT, Values.iri(ed.getTypeHint()));
                    recordUsedIRI(ed.getTypeHint());
                }

                model.add(subject, Constants.SSSOM_EXT_DEFINITIONS, edNode);
            }
        }

        @Override
        public void visit(ExtensionSlot<T> slot, T object, Map<String, ExtensionValue> values) {
            if ( extraPolicy == ExtraMetadataPolicy.NONE ) {
                return;
            }

            for ( String property : values.keySet() ) {
                ExtensionValue ev = values.get(property);
                if ( ev == null ) {
                    continue;
                }
                recordUsedIRI(property);
                IRI predicate = Values.iri(property);
                Value rdfValue = null;
                switch ( ev.getType() ) {
                case BOOLEAN:
                    rdfValue = Values.literal(ev.asBoolean());
                    recordUsedPrefix(BuiltinPrefix.XSD);
                    break;
                case DATE:
                    rdfValue = Values.literal(ev.asDate());
                    recordUsedPrefix(BuiltinPrefix.XSD);
                    break;
                case DATETIME:
                    rdfValue = Values.literal(ev.asDatetime());
                    recordUsedPrefix(BuiltinPrefix.XSD);
                    break;
                case DOUBLE:
                    rdfValue = Values.literal(ev.asDouble());
                    recordUsedPrefix(BuiltinPrefix.XSD);
                    break;
                case IDENTIFIER:
                    rdfValue = Values.iri(ev.asString());
                    recordUsedIRI(ev.asString());
                    break;
                case INTEGER:
                    rdfValue = Values.literal(ev.asInteger());
                    recordUsedPrefix(BuiltinPrefix.XSD);
                    break;
                case OTHER:
                case STRING:
                    rdfValue = Values.literal(ev.asString());
                    break;
                default:
                    // Should not happen
                    break;
                }

                model.add(subject, predicate, rdfValue);
            }
        }
    }
}
