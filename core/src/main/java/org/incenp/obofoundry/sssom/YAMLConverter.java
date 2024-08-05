/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.compatibility.JsonLDConverter;
import org.incenp.obofoundry.sssom.compatibility.MatchTermTypeConverter;
import org.incenp.obofoundry.sssom.compatibility.MatchTypeConverter;
import org.incenp.obofoundry.sssom.compatibility.SemanticSimilarityConverter;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;

/**
 * A helper class to convert generic YAML dictionaries into SSSOM objects.
 */
public class YAMLConverter {

    private PrefixManager prefixManager;
    private List<IYAMLPreprocessor> preprocessors;
    private Map<String, Slot<MappingSet>> setSlotMaps;
    private Map<String, Slot<Mapping>> mappingSlotMaps;
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

        setSlotMaps = new HashMap<String, Slot<MappingSet>>();
        for ( Slot<MappingSet> slot : SlotHelper.getMappingSetHelper().getSlots() ) {
            String slotName = slot.getName();
            if ( !slotName.equals("curie_map") && !slotName.equals("mappings") ) {
                setSlotMaps.put(slotName, slot);
            }
        }

        mappingSlotMaps = new HashMap<String, Slot<Mapping>>();
        for ( Slot<Mapping> slot : SlotHelper.getMappingHelper().getSlots() ) {
            mappingSlotMaps.put(slot.getName(), slot);
        }
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

        // Process the CURIE map first, so that we can expand CURIEs as soon as possible
        Object rawCurieMap = rawMap.getOrDefault("curie_map", new HashMap<String, String>());
        if ( isMapOf(rawCurieMap, String.class) ) {
            @SuppressWarnings("unchecked")
            Map<String, String> curieMap = Map.class.cast(rawCurieMap);
            ms.setCurieMap(curieMap);
            prefixManager.add(curieMap);
            rawMap.remove("curie_map");
        } else {
            onTypingError("curie_map");
        }

        // Process extension definitions
        extensionManager = new ExtensionSlotManager(extraPolicy, prefixManager);
        processDefinitions(rawMap);

        // Process the bulk of the metadata slots
        Map<String, ExtensionValue> extensionSlots = new HashMap<String, ExtensionValue>();
        for ( String key : rawMap.keySet() ) {
            if ( key.equals("mappings") ) { // To be processed separately
                continue;
            }
            if ( setSlotMaps.containsKey(key) ) {
                setSlotValue(setSlotMaps.get(key), ms, rawMap.get(key));
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
                    mappings.add(convertMapping(rawMapping));
                }
                ms.setMappings(mappings);
            } else {
                onTypingError("mappings");
            }

            // Finalise the set now that mappings are processed
            postMappings(ms);
        }

        return ms;
    }

    /**
     * Converts a generic dictionary (as may have been obtained from a YAML or JSON
     * parser) into a {@link Mapping} object.
     * 
     * @param rawMap The dictionary to convert.
     * @return The corresponding mapping object.
     * @throws SSSOMFormatException If the contents of the dictionary does not match
     *                              the SSSOM format and data model, even after
     *                              compatibility processing.
     */
    public Mapping convertMapping(Map<String, Object> rawMap) throws SSSOMFormatException {
        Mapping m = new Mapping();
        Map<String, ExtensionValue> extensionSlots = new HashMap<String, ExtensionValue>();

        for ( IYAMLPreprocessor preprocessor : preprocessors ) {
            preprocessor.process(rawMap);
        }

        for ( String key : rawMap.keySet() ) {
            if ( mappingSlotMaps.containsKey(key) ) {
                setSlotValue(mappingSlotMaps.get(key), m, rawMap.get(key));
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
        if ( !extensionManager.isEmpty() ) {
            // Sets the effective list of defined extensions
            ms.setExtensionDefinitions(extensionManager.getDefinitions(false, false));
        }
    }

    /*
     * Called upon a mismatch between the contents of a dictionary and what is
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
            onTypingError(name);
            return null;
        }
    }

    /*
     * Assigns a value to a SSSOM metadata slot.
     */
    private <T> void setSlotValue(Slot<T> slot, T object, Object rawValue) throws SSSOMFormatException {
        if ( rawValue == null ) {
            slot.setValue(object, rawValue);
            return;
        }

        Class<?> type = slot.getType();
        if ( type == String.class ) {
            String value = stringify(rawValue, slot.getName());
            if ( slot.isEntityReference() ) {
                value = prefixManager.expandIdentifier(value);
            }
            slot.setValue(object, value);
        } else if ( type == List.class ) {
            List<String> value = new ArrayList<String>();
            if ( List.class.isInstance(rawValue) ) {
                @SuppressWarnings("unchecked")
                List<Object> rawList = List.class.cast(rawValue);
                for ( Object rawItem : rawList ) {
                    value.add(stringify(rawItem, slot.getName()));
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
                onTypingError(slot.getName());
            }

            if ( slot.isEntityReference() ) {
                prefixManager.expandIdentifiers(value, true);
            }
            slot.setValue(object, value);
        } else if ( type == Map.class && isMapOf(rawValue, String.class) ) {
            @SuppressWarnings("unchecked")
            Map<String, String> value = Map.class.cast(rawValue);
            slot.setValue(object, value);
        } else if ( type == LocalDate.class && String.class.isInstance(rawValue) ) {
            try {
                String rawDate = String.class.cast(rawValue);
                if ( rawDate.contains("T") ) {
                    slot.setValue(object, LocalDateTime.parse(rawDate).toLocalDate());
                } else {
                    slot.setValue(object, LocalDate.parse(rawDate));
                }
            } catch ( DateTimeParseException e ) {
                onTypingError(slot.getName(), e);
            }
        } else if ( type == Double.class && String.class.isInstance(rawValue) ) {
            try {
                slot.setValue(object, Double.valueOf(String.class.cast(rawValue)));
            } catch ( NumberFormatException e ) {
                onTypingError(slot.getName(), e);
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMFormatException(String.format("Out-of-range value for '%s'", slot.getName()));
            }
        } else if ( type == Double.class && Double.class.isInstance(rawValue) ) {
            try {
                slot.setValue(object, rawValue);
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMFormatException(String.format("Out-of-range value for '%s'", slot.getName()));
            }
        } else if ( type == EntityType.class && String.class.isInstance(rawValue) ) {
            EntityType value = EntityType.fromString(String.class.cast(rawValue));
            if ( value != null ) {
                slot.setValue(object, value);
            } else {
                onTypingError(slot.getName());
            }
        } else if ( type == MappingCardinality.class && String.class.isInstance(rawValue) ) {
            MappingCardinality value = MappingCardinality.fromString(String.class.cast(rawValue));
            if ( value != null ) {
                slot.setValue(object, value);
            } else {
                onTypingError(slot.getName());
            }
        } else if ( type == PredicateModifier.class && String.class.isInstance(rawValue) ) {
            PredicateModifier value = PredicateModifier.fromString(String.class.cast(rawValue));
            if ( value != null ) {
                slot.setValue(object, value);
            } else {
                onTypingError(slot.getName());
            }
        } else {
            onTypingError(slot.getName());
        }
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
                    onTypingError("extension_definitions");
                }
            }
        } else {
            onTypingError("extension_definitions");
        }
    }

    /*
     * Parses any non-standard metadata slot.
     */
    private void processUnknownSlot(Map<String, ExtensionValue> extensions, String slotName, Object rawValue)
            throws SSSOMFormatException {
        // Look up the definition for the unknown slot. If we accept undefined slots,
        // we'll get an auto-generated definition.
        ExtensionDefinition definition = extensionManager.getDefinitionForSlot(slotName);
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
                        onTypingError(slotName, nfe);
                    }
                } else if ( rawValue != null ) {
                    onTypingError(slotName);
                }
                break;

            case DOUBLE:
                if ( Double.class.isInstance(rawValue) ) {
                    parsedValue = new ExtensionValue(Double.class.cast(rawValue).doubleValue());
                } else if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(Double.parseDouble(String.class.cast(rawValue)));
                    } catch ( NumberFormatException nfe ) {
                        onTypingError(slotName, nfe);
                    }
                } else if ( rawValue != null ) {
                    onTypingError(slotName);
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
                        onTypingError(slotName);
                    }
                } else if ( rawValue != null ) {
                    onTypingError(slotName);
                }
                break;

            case DATE:
                if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(LocalDate.parse(String.class.cast(rawValue)));
                    } catch ( DateTimeParseException dtpe ) {
                        onTypingError(slotName, dtpe);
                    }
                } else if ( rawValue != null ) {
                    onTypingError(slotName);
                }
                break;

            case DATETIME:
                if ( String.class.isInstance(rawValue) ) {
                    try {
                        parsedValue = new ExtensionValue(ZonedDateTime.parse(String.class.cast(rawValue)));
                    } catch ( DateTimeParseException dtpe ) {
                        onTypingError(slotName, dtpe);
                    }
                } else if ( rawValue != null ) {
                    onTypingError(slotName);
                }
                break;

            case IDENTIFIER:
                if ( String.class.isInstance(rawValue) ) {
                    parsedValue = new ExtensionValue(prefixManager.expandIdentifier(String.class.cast(rawValue)), true);
                } else if ( rawValue != null ) {
                    onTypingError(slotName);
                }
                break;

            case OTHER:
                parsedValue = new ExtensionValue(rawValue);
                break;
            }

            extensions.put(definition.getProperty(), parsedValue);
        }
    }
}
