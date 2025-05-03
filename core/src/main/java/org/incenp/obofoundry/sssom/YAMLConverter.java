/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024,2025 Damien Goutte-Gattat
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.compatibility.JsonLDConverter;
import org.incenp.obofoundry.sssom.compatibility.LiteralProfileConverter;
import org.incenp.obofoundry.sssom.compatibility.MatchTermTypeConverter;
import org.incenp.obofoundry.sssom.compatibility.MatchTypeConverter;
import org.incenp.obofoundry.sssom.compatibility.SemanticSimilarityConverter;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityReferenceSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.StringSlot;

/**
 * A helper class to convert generic YAML dictionaries into SSSOM objects.
 */
public class YAMLConverter {

    private PrefixManager prefixManager;
    private List<IYAMLPreprocessor> preprocessors;
    private ExtensionSlotManager extensionManager;
    private ExtraMetadataPolicy extraPolicy = ExtraMetadataPolicy.NONE;

    /**
     * Creates a new YAML converter.
     */
    public YAMLConverter() {
        prefixManager = new PrefixManager();

        preprocessors = new ArrayList<IYAMLPreprocessor>();
        preprocessors.add(new MatchTypeConverter());
        preprocessors.add(new MatchTermTypeConverter());
        preprocessors.add(new JsonLDConverter());
        preprocessors.add(new SemanticSimilarityConverter());
        preprocessors.add(new LiteralProfileConverter());
    }

    /**
     * Sets the policy to deal with non-standard metadata in the input file.
     * 
     * @param policy The policy instructing the parser about what to do when
     *               encountering non-standard metadata. The default policy is
     *               {@link ExtraMetadataPolicy#NONE}.
     */
    public void setExtraMetadataPolicy(ExtraMetadataPolicy policy) {
        extraPolicy = policy;
    }

    /**
     * Checks that an object is a list containing items of a given type.
     * 
     * @param value The object whose runtime type is to be checked.
     * @param type  The expected runtime type of the list's values.
     * @return {@code true} if the object is a {@link List} whose items are of the
     *         expected type, otherwise {@code false}.
     */
    public static boolean isListOf(Object value, Class<?> type) {
        if ( List.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            List<Object> list = List.class.cast(value);
            for ( Object item : list ) {
                if ( !type.isInstance(item) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks that an object is a dictionary with string keys and values of a given
     * type.
     * 
     * @param value The object whose runtime type is to be checked.
     * @param type  The expected runtime of the dictionary's values.
     * @return {@code true} if the object is a {@link Map} whose keys are
     *         {@link String} and whose values are of the expected type, otherwise
     *         {@code false}.
     */
    public static boolean isMapOf(Object value, Class<?> type) {
        if ( Map.class.isInstance(value) ) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = Map.class.cast(value);
            for ( Object key : map.keySet() ) {
                if ( !String.class.isInstance(key) || !type.isInstance(map.get(key)) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the prefix manager used by this object to expand shortened entity
     * references in values.
     * 
     * @return The prefix manager.
     */
    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    /**
     * Converts a generic dictionary (as may have been obtained from a YAML or JSON
     * parser) into a {@link MappingSet} object.
     * 
     * @param rawMap The dictionary to convert.
     * @return The corresponding mapping set object.
     * @throws SSSOMFormatException If the contents of the dictionary does not match
     *                              the SSSOM format and data model, even after
     *                              compatibility processing.
     */
    public MappingSet convertMappingSet(Map<String, Object> rawMap) throws SSSOMFormatException {
        MappingSet ms = new MappingSet();

        // Deal with variations from older versions of the specification
        for ( IYAMLPreprocessor preprocessor : preprocessors ) {
            preprocessor.process(rawMap);
        }

        // Find out the version of the spec the set declares itself to be compliant with
        Version version = Version.SSSOM_1_0;
        Object rawVersion = rawMap.get("sssom_version");
        if ( rawVersion != null ) {
            if ( String.class.isInstance(rawVersion) ) {
                version = Version.fromString(String.class.cast(rawVersion));
            } else {
                throw getTypingError("sssom_version");
            }
        }
        ms.setSssomVersion(version);
        if ( version == Version.UNKNOWN ) {
            // Try the latest supported version
            version = Version.SSSOM_1_1;
        }

        // Process the CURIE map, so that we can expand CURIEs as soon as possible
        Object rawCurieMap = rawMap.getOrDefault("curie_map", new HashMap<String, String>());
        if ( isMapOf(rawCurieMap, String.class) ) {
            @SuppressWarnings("unchecked")
            Map<String, String> curieMap = Map.class.cast(rawCurieMap);
            ms.setCurieMap(curieMap);
            prefixManager.add(curieMap);
            rawMap.remove("curie_map");
        } else {
            throw getTypingError("curie_map");
        }

        // Process extension definitions
        extensionManager = new ExtensionSlotManager(extraPolicy, prefixManager);
        processDefinitions(rawMap);

        // Process the bulk of the metadata slots
        SlotSetterVisitor<MappingSet> visitor = new SlotSetterVisitor<MappingSet>();
        Map<String, ExtensionValue> extensionSlots = new HashMap<String, ExtensionValue>();
        for ( String key : rawMap.keySet() ) {
            if ( key.equals("mappings") ) { // To be processed separately
                continue;
            }

            Slot<MappingSet> slot = SlotHelper.getMappingSetHelper().getSlotByName(key);
            if ( slot != null && slot.getCompliantVersion().isCompatibleWith(version) ) {
                Object rawValue = rawMap.get(key);
                if ( rawValue == null ) {
                    slot.setValue(ms, rawValue);
                } else {
                    visitor.rawValue = rawValue;
                    slot.accept(visitor, ms, null);
                    if ( visitor.error != null ) {
                        throw visitor.error;
                    }
                }
            } else {
                processUnknownSlot(extensionSlots, key, rawMap.get(key));
            }
        }
        if ( !extensionSlots.isEmpty() ) {
            ms.setExtensions(extensionSlots);
        }

        // Process the mappings themselves, if we have them
        if ( rawMap.containsKey("mappings") ) {
            Object value = rawMap.get("mappings");
            if ( isListOf(value, Map.class) ) {
                ArrayList<Mapping> mappings = new ArrayList<Mapping>();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawMappings = List.class.cast(value);
                for ( Map<String, Object> rawMapping : rawMappings ) {
                    mappings.add(convertMapping(rawMapping, version));
                }
                ms.setMappings(mappings);
            } else {
                throw getTypingError("mappings");
            }

            // Finalise the set now that mappings are processed
            postMappings(ms);
        }

        return ms;
    }

    /**
     * Converts a generic dictionary (as may have been obtained from a YAML or JSON
     * parser) into a {@link Mapping} object.
     * <p>
     * This method assumes the mapping is compliant with the highest supported
     * version of the specification.
     * 
     * @param rawMap The dictionary to convert.
     * @return The corresponding mapping object.
     * @throws SSSOMFormatException If the contents of the dictionary does not match
     *                              the SSSOM format and data model, even after
     *                              compatibility processing.
     */
    public Mapping convertMapping(Map<String, Object> rawMap) throws SSSOMFormatException {
        return convertMapping(rawMap, Version.SSSOM_1_1);
    }

    /**
     * Converts a generic dictionary (as may have been obtained from a YAML or JSON
     * parser) into a {@link Mapping} object.
     * 
     * @param rawMap        The dictionary to convert.
     * @param targetVersion The version of the SSSOM specification the mapping is
     *                      compliant with.
     * @return The corresponding mapping object.
     * @throws SSSOMFormatException If the contents of the dictionary does not match
     *                              the SSSOM format and data model, even after
     *                              compatibility processing.
     */
    public Mapping convertMapping(Map<String, Object> rawMap, Version targetVersion) throws SSSOMFormatException {
        Mapping m = new Mapping();
        Map<String, ExtensionValue> extensionSlots = new HashMap<String, ExtensionValue>();

        for ( IYAMLPreprocessor preprocessor : preprocessors ) {
            preprocessor.process(rawMap);
        }

        if ( targetVersion == Version.UNKNOWN ) {
            targetVersion = Version.SSSOM_1_1;
        }

        SlotSetterVisitor<Mapping> visitor = new SlotSetterVisitor<Mapping>();

        for ( String key : rawMap.keySet() ) {
            Slot<Mapping> slot = SlotHelper.getMappingHelper().getSlotByName(key);
            if ( slot != null && slot.getCompliantVersion().isCompatibleWith(targetVersion) ) {
                Object rawValue = rawMap.get(key);
                if ( rawValue == null ) {
                    slot.setValue(m, rawValue);
                } else {
                    visitor.rawValue = rawMap.get(key);
                    slot.accept(visitor, m, null);
                    if ( visitor.error != null ) {
                        throw visitor.error;
                    }
                }
            } else {
                processUnknownSlot(extensionSlots, key, rawMap.get(key));
            }
        }
        if ( !extensionSlots.isEmpty() ) {
            m.setExtensions(extensionSlots);
        }

        return m;
    }

    /**
     * Finalise the conversion of a set. This method should be called once all
     * individual mappings have been converted through calls to
     * {@link #convertMapping(Map)}. There is no need to call this method if the
     * mappings were already present in the raw map passed to
     * {@link #convertMappingSet(Map)}.
     * 
     * @param ms The mapping set to finalise.
     */
    public void postMappings(MappingSet ms) {
        if ( !getExtensionManager().isEmpty() ) {
            // Sets the effective list of defined extensions
            ms.setExtensionDefinitions(getExtensionManager().getDefinitions(false, false));
        }
    }

    /*
     * We access the extension manager through this method because it may not have
     * been initialised if convertMappingSet has never been called (which can happen
     * if we're reading a TSV set that doesn't have any metadata block).
     */
    private ExtensionSlotManager getExtensionManager() {
        if ( extensionManager == null ) {
            extensionManager = new ExtensionSlotManager(extraPolicy, new PrefixManager());
        }
        return extensionManager;
    }

    /*
     * Gets the standard exception and error message for when there is a mismatch
     * between the contents of a dictionary and what is expected by the SSSOM data
     * model.
     */
    private SSSOMFormatException getTypingError(String slotName, Throwable innerException) {
        return new SSSOMFormatException(String.format("Typing error when parsing '%s'", slotName), innerException);
    }

    /*
     * Likewise, but without an inner exception as the root cause for the mismatch.
     */
    private SSSOMFormatException getTypingError(String slotName) {
        return getTypingError(slotName, null);
    }

    /*
     * Make sure the given object is a String. If it is a YAML collection instead
     * (list or dictionary), we throw a SSSOMFormatException. `name` is the name of
     * the slot we are currently converting (will be used in the error message).
     */
    private String stringify(Object o, String name) throws SSSOMFormatException {
        if ( o == null ) {
            return null;
        } else if ( String.class.isInstance(o) ) {
            return String.class.cast(o);
        } else if ( !List.class.isInstance(o) && !Map.class.isInstance(o) ) {
            return o.toString();
        } else {
            throw getTypingError(name);
        }
    }

    /*
     * Converts a YAML object into a list of strings.
     */
    private List<String> getListOfStrings(String slotName, Object rawValue) throws SSSOMFormatException {
        List<String> value = new ArrayList<String>();
        if ( List.class.isInstance(rawValue) ) {
            @SuppressWarnings("unchecked")
            List<Object> rawList = (List<Object>) rawValue;
            for ( Object rawItem : rawList ) {
                value.add(stringify(rawItem, slotName));
            }
        } else if ( !Map.class.isInstance(rawValue) ) {
            /*
             * The TSV serialisation format stores list values as a single string, from
             * which list values must be extracted by splitting the string around '|'
             * characters.
             * 
             * This has the side effect of allowing list-valued slots to be (mis)used as if
             * they were single-valued, which is strictly speaking but happens in the wild
             * (including in the examples shown in the SSSOM documentation!).
             */
            for ( String item : rawValue.toString().split("\\|") ) {
                value.add(item);
            }
        } else {
            throw getTypingError(slotName);
        }
        return value;
    }

    /*
     * Parses the "extension_definitions" key.
     */
    private void processDefinitions(Map<String, Object> rawMap) throws SSSOMFormatException {
        Object rawDefinitions = rawMap.get("extension_definitions");
        rawMap.remove("extension_definitions");

        if ( extraPolicy == ExtraMetadataPolicy.NONE || rawDefinitions == null ) {
            return;
        }

        if ( List.class.isInstance(rawDefinitions) ) {
            @SuppressWarnings("unchecked")
            List<Object> rawDefinitionsList = List.class.cast(rawDefinitions);

            for ( Object rawDefinition : rawDefinitionsList ) {
                if ( YAMLConverter.isMapOf(rawDefinition, String.class) ) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> definition = Map.class.cast(rawDefinition);

                    String slotName = definition.get("slot_name");
                    String property = definition.get("property");
                    String typeHint = definition.get("type_hint");

                    if ( slotName != null && property != null ) {
                        // Pass the definition to the extension slot manager. We'll assign all the
                        // collected definitions to the mapping set later.
                        extensionManager.addDefinition(slotName, property, typeHint);
                    }
                } else {
                    throw getTypingError("extension_definitions");
                }
            }
        } else {
            throw getTypingError("extension_definitions");
        }
    }

    /*
     * Parses any non-standard metadata slot.
     */
    private void processUnknownSlot(Map<String, ExtensionValue> extensions, String slotName, Object rawValue)
            throws SSSOMFormatException {
        // Look up the definition for the unknown slot. If we accept undefined slots,
        // we'll get an auto-generated definition.
        ExtensionDefinition definition = getExtensionManager().getDefinitionForSlot(slotName);
        if ( definition != null ) {
            ExtensionValue parsedValue = null;
            switch ( definition.getEffectiveType() ) {
            case STRING:
                if ( rawValue != null ) {
                    parsedValue = new ExtensionValue(stringify(rawValue, slotName));
                }
                break;

            case INTEGER:
                if ( Integer.class.isInstance(rawValue) ) {
                    parsedValue = new ExtensionValue(Integer.class.cast(rawValue).intValue());
                } else if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(Integer.parseInt(String.class.cast(rawValue)));
                    } catch ( NumberFormatException nfe ) {
                        throw getTypingError(slotName, nfe);
                    }
                } else if ( rawValue != null ) {
                    throw getTypingError(slotName);
                }
                break;

            case DOUBLE:
                if ( Double.class.isInstance(rawValue) ) {
                    parsedValue = new ExtensionValue(Double.class.cast(rawValue).doubleValue());
                } else if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(Double.parseDouble(String.class.cast(rawValue)));
                    } catch ( NumberFormatException nfe ) {
                        throw getTypingError(slotName, nfe);
                    }
                } else if ( rawValue != null ) {
                    throw getTypingError(slotName);
                }
                break;

            case BOOLEAN:
                if ( Boolean.class.isInstance(rawValue) ) {
                    parsedValue = new ExtensionValue(Boolean.class.cast(rawValue).booleanValue());
                } else if ( String.class.isInstance(rawValue) ) {
                    String value = String.class.cast(rawValue);
                    if ( value.equals("true") ) {
                        parsedValue = new ExtensionValue(true);
                    } else if ( value.equals("false") ) {
                        parsedValue = new ExtensionValue(false);
                    } else {
                        throw getTypingError(slotName);
                    }
                } else if ( rawValue != null ) {
                    throw getTypingError(slotName);
                }
                break;

            case DATE:
                if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(LocalDate.parse(String.class.cast(rawValue)));
                    } catch ( DateTimeParseException dtpe ) {
                        throw getTypingError(slotName, dtpe);
                    }
                } else if ( rawValue != null ) {
                    throw getTypingError(slotName);
                }
                break;

            case DATETIME:
                if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(ZonedDateTime.parse(String.class.cast(rawValue)));
                    } catch ( DateTimeParseException dtpe ) {
                        throw getTypingError(slotName, dtpe);
                    }
                } else if ( rawValue != null ) {
                    throw getTypingError(slotName);
                }
                break;

            case IDENTIFIER:
                if ( String.class.isInstance(rawValue) ) {
                    parsedValue = new ExtensionValue(prefixManager.expandIdentifier(String.class.cast(rawValue)), true);
                } else if ( rawValue != null ) {
                    throw getTypingError(slotName);
                }
                break;

            case OTHER:
                parsedValue = new ExtensionValue(rawValue);
                break;
            }

            extensions.put(definition.getProperty(), parsedValue);
        }
    }

    /*
     * Helper visitor to set slot values from the YAML contents.
     */
    class SlotSetterVisitor<T> extends SlotVisitorBase<T> {

        SSSOMFormatException error;
        Object rawValue;

        // Covers all enum-based slots
        @Override
        public void visit(Slot<T> slot, T object, Object unused) {
            if ( String.class.isInstance(rawValue) ) {
                try {
                    slot.setValue(object, String.class.cast(rawValue));
                } catch ( IllegalArgumentException e ) {
                    error = getTypingError(slot.getName());
                }
            } else {
                error = getTypingError(slot.getName());
            }
        }

        // Covers both string- and URI-typed mono-valued slots
        @Override
        public void visit(StringSlot<T> slot, T object, String unused) {
            try {
                String value = stringify(rawValue, slot.getName());
                slot.setValue(object, value);
            } catch ( SSSOMFormatException e ) {
                error = e;
            }
        }

        // Covers both string- and URI-typed multi-valued slots
        @Override
        public void visit(StringSlot<T> slot, T object, List<String> unused) {
            try {
                slot.setValue(object, getListOfStrings(slot.getName(), rawValue));
            } catch ( SSSOMFormatException e ) {
                error = e;
            }
        }

        @Override
        public void visit(EntityReferenceSlot<T> slot, T object, String unused) {
            try {
                String value = prefixManager.expandIdentifier(stringify(rawValue, slot.getName()));
                slot.setValue(object, value);
            } catch ( SSSOMFormatException e ) {
                error = e;
            }
        }

        @Override
        public void visit(EntityReferenceSlot<T> slot, T object, List<String> unused) {
            try {
                List<String> values = getListOfStrings(slot.getName(), rawValue);
                prefixManager.expandIdentifiers(values, true);
                slot.setValue(object, values);
            } catch ( SSSOMFormatException e ) {
                error = e;
            }
        }

        @Override
        public void visit(DoubleSlot<T> slot, T object, Double unused) {
            if ( String.class.isInstance(rawValue) ) {
                try {
                    slot.setValue(object, String.class.cast(rawValue));
                } catch ( NumberFormatException e ) {
                    error = getTypingError(slot.getName());
                } catch ( IllegalArgumentException e ) {
                    error = new SSSOMFormatException(String.format("Out-of-range value for '%s'", slot.getName()));
                }
            } else if ( Double.class.isInstance(rawValue) ) {
                try {
                    slot.setValue(object, rawValue);
                } catch ( IllegalArgumentException e ) {
                    error = new SSSOMFormatException(String.format("Out-of-range value for '%s'", slot.getName()));
                }
            } else {
                error = getTypingError(slot.getName());
            }
        }
    }
}
