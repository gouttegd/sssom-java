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
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.incenp.obofoundry.sssom.ExtensionSlotManager;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.Validator;
import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.model.ValueType;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.slots.DateSlot;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityReferenceSlot;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionDefinitionSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionSlot;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.PredicateModifierSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;
import org.incenp.obofoundry.sssom.slots.URISlot;
import org.incenp.obofoundry.sssom.slots.VersionSlot;

/**
 * A helper class to convert SSSOM objects to and from a RDF model in the Rdf4J
 * API.
 */
public class RDFConverter {

    private ExtraMetadataPolicy extraPolicy;
    private ExtensionSlotManager extMgr;
    private int bnodeCounter;
    private Set<String> excludedSlots;

    /**
     * Creates a new instance with the default policy for converting non-standard
     * metadata.
     */
    public RDFConverter() {
        extraPolicy = ExtraMetadataPolicy.UNDEFINED;
    }

    /**
     * Creates a new instance with an explicit policy for converting non-standard
     * metadata.
     * 
     * @param policy The non-standard metadata policy.
     */
    public RDFConverter(ExtraMetadataPolicy policy) {
        extraPolicy = policy;
    }

    /*
     * SSSOM to RDF conversions
     */

    /**
     * When converting from SSSOM to RDF, do not convert the mapping slots in the
     * provided list.
     * <p>
     * This is mainly intended to avoid converting the slots that have been
     * condensed to the set level.
     * 
     * @param slots A list of slot names to exclude from the RDF conversion.
     */
    public void excludeMappingSlots(Set<String> slots) {
        excludedSlots = slots;
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
        bnodeCounter = 0;
        Set<String> usedPrefixes = prefixManager != null ? new HashSet<String>() : null;

        // Determine mininum compliant version
        ms.setSssomVersion(new Validator().getCompliantVersion(ms));

        // Create the mapping set node
        BNode set = Values.bnode(String.valueOf(bnodeCounter++));
        model.add(set, RDF.TYPE, Constants.SSSOM_MAPPING_SET);

        // Add the set-level metadata
        RDFBuilderVisitor<MappingSet> setVisitor = new RDFBuilderVisitor<MappingSet>(model, set, prefixManager,
                usedPrefixes);
        SlotHelper.getMappingSetHelper().visitSlots(ms, setVisitor);

        RDFBuilderVisitor<Mapping> mappingVisitor = new RDFBuilderVisitor<Mapping>(model, null, prefixManager,
                usedPrefixes);
        SlotHelper<Mapping> mappingHelper = getMappingHelper();
        for ( Mapping mapping : ms.getMappings() ) {
            // Add individual mapping
            BNode mappingNode = Values.bnode(String.valueOf(bnodeCounter++));
            model.add(mappingNode, RDF.TYPE, Constants.OWL_AXIOM);
            model.add(set, Constants.SSSOM_MAPPINGS, mappingNode);

            // Add mapping metadata slots
            mappingVisitor.subject = mappingNode;
            mappingHelper.visitSlots(mapping, mappingVisitor);
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
     * RDF to SSSOM conversions
     */

    /**
     * Converts a RDF model to a MappingSet object.
     * 
     * @param model The Rdf4J model to convert.
     * @return The corresponding mapping set.
     * @throws SSSOMFormatException If the contents of the model does not match what
     *                              is expected for a SSSOM MappingSet object.
     */
    public MappingSet fromRDF(Model model) throws SSSOMFormatException {
        Model root = model.filter(null, RDF.TYPE, Constants.SSSOM_MAPPING_SET);
        Optional<BNode> set = Models.subjectBNode(root);
        if ( set.isEmpty() ) {
            throw new SSSOMFormatException("RDF model does not contain a SSSOM mapping set");
        }

        MappingSet ms = new MappingSet();
        ms.setMappings(new ArrayList<Mapping>());
        SlotSetterVisitor<MappingSet> visitor = new SlotSetterVisitor<MappingSet>();

        // Extract the SSSOM version first
        Version version = versionFromRDF(model, set.get());
        ms.setSssomVersion(version);
        if ( version == Version.UNKNOWN ) {
            version = Version.SSSOM_1_1;
        }

        // Parse extension definitions ahead, so that definitions are available if/when
        // we encounter an extension slot when looping over all the statements
        extensionsFromRDF(ms, model, set.get());

        // Process all statements about the mapping set node
        for ( Statement st : model.filter(set.get(), null, null) ) {
            if ( st.getPredicate().equals(Constants.SSSOM_VERSION) ) {
                // We have dealt with that one already, skip
                continue;
            }
            Slot<MappingSet> slot = SlotHelper.getMappingSetHelper().getSlotByURI(st.getPredicate().stringValue());
            if ( slot != null && slot.getCompliantVersion().isCompatibleWith(version) ) {
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
                    ms.getMappings().add(mappingFromRDF(model.filter((BNode) o, null, null), version));
                } else {
                    throw getTypingError("mappings");
                }
            } else if ( !st.getPredicate().equals(RDF.TYPE) ) {
                extensionValueFromRDF(ms, st);
            }
        }

        // Fill in the Curie map from the model's namespaces
        ms.setCurieMap(new HashMap<String, String>());
        for ( Namespace ns : model.getNamespaces() ) {
            if ( BuiltinPrefix.fromString(ns.getPrefix()) == null ) {
                ms.getCurieMap().put(ns.getPrefix(), ns.getName());
            }
        }

        return ms;
    }

    /**
     * Converts a RDF model to a Mapping object.
     * <p>
     * This method assumes the mapping is compliant with the highest supported
     * version of the specification.
     * 
     * @param model The Rdf4J model to convert.
     * @return The corresponding mapping.
     * @throws SSSOMFormatException If the contents of the model does not match what
     *                              is expected for a SSSOM Mapping object.
     */
    public Mapping mappingFromRDF(Model model) throws SSSOMFormatException {
        return mappingFromRDF(model, Version.SSSOM_1_1);
    }

    /**
     * Converts a RDF model to a Mapping object.
     * 
     * @param model         The Rdf4J model to convert.
     * @param targetVersion The version of the SSSOM specification the mapping is
     *                      compliant with.
     * @return The corresponding mapping.
     * @throws SSSOMFormatException If the contents of the model does not match what
     *                              is expected for a SSSOM Mapping object.
     */
    public Mapping mappingFromRDF(Model model, Version targetVersion) throws SSSOMFormatException {
        Model root = model.filter(null, RDF.TYPE, Constants.OWL_AXIOM);
        Optional<BNode> mappingNode = Models.subjectBNode(root);
        if ( mappingNode.isEmpty() ) {
            throw new SSSOMFormatException("RDF model does not contain a Mapping object");
        }

        Mapping mapping = new Mapping();
        SlotSetterVisitor<Mapping> visitor = new SlotSetterVisitor<Mapping>();
        for ( Statement st : model.filter(mappingNode.get(), null, null) ) {
            Slot<Mapping> slot = SlotHelper.getMappingHelper().getSlotByURI(st.getPredicate().stringValue());
            if ( slot != null && slot.getCompliantVersion().isCompatibleWith(targetVersion) ) {
                // Statement is a mapping metadata slot
                visitor.rdfValue = st.getObject();
                slot.accept(visitor, mapping, null);
                if ( visitor.error != null ) {
                    throw visitor.error;
                }
            } else if ( !st.getPredicate().equals(RDF.TYPE) ) {
                extensionValueFromRDF(mapping, st);
            }
        }

        return mapping;
    }

    /*
     * Private helper methods and classes
     */

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
     * Gets the helper to use for visiting mapping slots.
     */
    private SlotHelper<Mapping> getMappingHelper() {
        SlotHelper<Mapping> helper;
        if ( excludedSlots == null || excludedSlots.isEmpty() ) {
            helper = SlotHelper.getMappingHelper();
        } else {
            helper = SlotHelper.getMappingHelper(true);
            helper.excludeSlots(excludedSlots);
        }
        return helper;
    }

    /*
     * Extract the SSSOM Version value from the RDF model.
     */
    private Version versionFromRDF(Model model, BNode set) throws SSSOMFormatException {
        Version version = Version.SSSOM_1_0;
        for ( Statement st : model.filter(set, Constants.SSSOM_VERSION, null) ) {
            if ( st.getObject().isLiteral() ) {
                version = Version.fromString(st.getObject().stringValue());
            } else {
                throw getTypingError("sssom_version");
            }
        }
        return version;
    }

    /*
     * Extracts extension definitions from the RDF model, if extensions are enabled
     * by policy.
     */
    private void extensionsFromRDF(MappingSet ms, Model model, BNode set) {
        if ( extraPolicy == ExtraMetadataPolicy.NONE ) {
            return;
        }

        extMgr = new ExtensionSlotManager(extraPolicy);
        for ( Statement st : model.filter(set, Constants.SSSOM_EXT_DEFINITIONS, null) ) {
            if ( st.getObject().isResource() ) {
                Resource defNode = (Resource) st.getObject();

                IRI prop = Models.objectIRI(model.filter(defNode, Constants.SSSOM_EXT_PROPERTY, null)).orElse(null);
                IRI hint = Models.objectIRI(model.filter(defNode, Constants.SSSOM_EXT_TYPEHINT, null)).orElse(null);
                Literal name = Models.objectLiteral(model.filter(defNode, Constants.SSSOM_EXT_SLOTNAME, null))
                        .orElse(null);

                if ( prop != null && name != null ) {
                    String typeHint = hint != null ? hint.stringValue() : null;
                    extMgr.addDefinition(name.stringValue(), prop.stringValue(), typeHint);
                }
            }
        }
        if ( !extMgr.isEmpty() ) {
            ms.setExtensionDefinitions(extMgr.getDefinitions(false, false));
        }
    }

    /*
     * Interprets a RDF statement as a SSSOM non-standard metadata on a MappingSet.
     */
    private void extensionValueFromRDF(MappingSet ms, Statement statement) throws SSSOMFormatException {
        String property = statement.getPredicate().stringValue();
        ExtensionValue value = extensionValueFromRDF(property, statement.getObject());
        if ( value != null ) {
            Map<String, ExtensionValue> extensions = ms.getExtensions();
            if ( extensions == null ) {
                extensions = new HashMap<String, ExtensionValue>();
                ms.setExtensions(extensions);
            }
            extensions.put(property, value);
        }
    }

    /*
     * Likewise, but for a Mapping object.
     */
    private void extensionValueFromRDF(Mapping m, Statement statement) throws SSSOMFormatException {
        String property = statement.getPredicate().stringValue();
        ExtensionValue value = extensionValueFromRDF(property, statement.getObject());
        if ( value != null ) {
            Map<String, ExtensionValue> extensions = m.getExtensions();
            if ( extensions == null ) {
                extensions = new HashMap<String, ExtensionValue>();
                m.setExtensions(extensions);
            }
            extensions.put(property, value);
        }
    }

    /*
     * Converts a RDF value into a non-standard metadata. This method does the bulk
     * of the work for the two wrapper methods above.
     */
    private ExtensionValue extensionValueFromRDF(String property, Value rdfValue) throws SSSOMFormatException {
        if ( extraPolicy == ExtraMetadataPolicy.NONE ) {
            // Extension support disabled by policy
            return null;
        } else if ( extraPolicy == ExtraMetadataPolicy.DEFINED ) {
            if ( extMgr == null ) {
                // Could happen if we convert a single Mapping instead of a MappingSet; without
                // a MappingSet, we cannot have extension definitions, so we just ignore
                return null;
            }

            ExtensionDefinition definition = extMgr.getDefinitionForProperty(property);
            if ( definition == null ) {
                // Ignore undefined extension, as per policy
                return null;
            }

            // This is a valid extension, so check that its type matches the type hint; if
            // it does not, this is an error rather than something to ignore
            ValueType valueType = definition.getEffectiveType();
            if ( valueType == ValueType.IDENTIFIER ) {
                // The value must be an IRI
                if ( !rdfValue.isIRI() ) {
                    throw getTypingError(property);
                }
            } else {
                // The value must be a literal
                if ( !rdfValue.isLiteral() ) {
                    throw getTypingError(property);
                }

                // The value must also match the expected type
                String actualType = ((Literal) rdfValue).getDatatype().stringValue();
                if ( valueType == ValueType.OTHER ) {
                    if ( !actualType.equals(definition.getTypeHint()) ) {
                        throw getTypingError(property);
                    }
                } else {
                    if ( valueType != ValueType.fromIRI(actualType) ) {
                        throw getTypingError(property);
                    }
                }
            }
        }

        ExtensionValue ev = null;
        if ( rdfValue.isIRI() ) {
            ev = new ExtensionValue(rdfValue.stringValue(), true);
        } else if ( rdfValue.isLiteral() ) {
            Literal litValue = (Literal) rdfValue;
            ValueType valueType = ValueType.fromIRI(litValue.getDatatype().stringValue());
            try {
                switch ( valueType ) {
                case BOOLEAN:
                    ev = new ExtensionValue(litValue.booleanValue());
                    break;
                case DATE:
                    ev = new ExtensionValue(LocalDate.parse(litValue.stringValue()));
                    break;
                case DATETIME:
                    ev = new ExtensionValue(ZonedDateTime.parse(litValue.stringValue()));
                    break;
                case DOUBLE:
                    ev = new ExtensionValue(litValue.doubleValue());
                    break;
                case IDENTIFIER:
                    // Should not really happen, unless someone explicitly types a literal as a
                    // linkml:Uriorcurie
                    ev = new ExtensionValue(litValue.stringValue(), true);
                    break;
                case INTEGER:
                    ev = new ExtensionValue(litValue.intValue());
                    break;
                case OTHER:
                    ev = new ExtensionValue((Object) litValue.stringValue());
                    break;
                case STRING:
                    ev = new ExtensionValue(litValue.stringValue());
                    break;
                }
            } catch ( IllegalArgumentException | DateTimeParseException e ) {
                // In DEFINED mode, an invalid value is an error; otherwise, we can just ignore
                // the extension altogether
                if ( extraPolicy == ExtraMetadataPolicy.DEFINED ) {
                    throw getTypingError(property, e);
                }
            }
        }

        return ev;
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

        @Override
        public void visit(ExtensionDefinitionSlot<T> slot, T target, List<ExtensionDefinition> values) {
            // Nothing to do, handled elsewhere
        }
    }

    /*
     * Helper visitor to fill in the RDF model (including namespaces) from slot
     * values.
     */
    private class RDFBuilderVisitor<T> extends SlotVisitorBase<T> {

        Model model;
        Resource subject;
        PrefixManager pfxMgr;
        Set<String> prefixes;

        RDFBuilderVisitor(Model model, Resource subject, PrefixManager prefixManager, Set<String> usedPrefixes) {
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

        @Override
        public void visit(VersionSlot<T> slot, T object, Version value) {
            if ( value != Version.SSSOM_1_0 && value != Version.UNKNOWN ) {
                recordUsedIRI(slot.getURI());
                model.add(subject, Values.iri(slot.getURI()), Values.literal(value.toString()));
            }
        }
    }
}
