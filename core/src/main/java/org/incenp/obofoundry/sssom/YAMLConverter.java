/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.EntityType;
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
    private ExtensionHelper extensionHelper;
    private ExtraMetadataPolicy extraPolicy = ExtraMetadataPolicy.NONE;

    /**
     * Creates a new YAML converter.
     */
    public YAMLConverter() {
        prefixManager = new PrefixManager();

        preprocessors = new ArrayList<IYAMLPreprocessor>();
        preprocessors.add(new MatchTypeConverter());
        preprocessors.add(new MatchTermTypeConverter());

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

        // Deal with variations from older versions of the specification
        for ( IYAMLPreprocessor preprocessor : preprocessors ) {
            preprocessor.process(rawMap);
        }

        // Process extension definitions
        extensionHelper = new ExtensionHelper(extraPolicy, prefixManager);
        extensionHelper.processDefinitions(rawMap);

        // Process the bulk of the metadata slots
        Map<String, ExtensionValue> extensionSlots = new HashMap<String, ExtensionValue>();
        for ( String key : rawMap.keySet() ) {
            if ( setSlotMaps.containsKey(key) ) {
                setSlotValue(setSlotMaps.get(key), ms, rawMap.get(key));
            } else {
                extensionHelper.processUnknownSlot(extensionSlots, key, rawMap.get(key));
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
                extensionHelper.processUnknownSlot(extensionSlots, key, rawMap.get(key));
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
        if ( extensionHelper.hasDefinitions() ) {
            ms.setExtensionDefinitions(extensionHelper.getDefinitions());
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
     * Assigns a value to a SSSOM metadata slot.
     */
    private <T> void setSlotValue(Slot<T> slot, T object, Object rawValue) throws SSSOMFormatException {
        Class<?> type = slot.getType();
        if ( type == String.class && String.class.isInstance(rawValue) ) {
            String value = String.class.cast(rawValue);
            if ( slot.isEntityReference() ) {
                value = prefixManager.expandIdentifier(value);
            }
            slot.setValue(object, value);
        } else if ( type == List.class && isListOf(rawValue, String.class) ) {
            @SuppressWarnings("unchecked")
            List<String> value = List.class.cast(rawValue);
            if ( slot.isEntityReference() ) {
                prefixManager.expandIdentifiers(value, true);
            }
            slot.setValue(object, value);
        } else if ( type == List.class && String.class.isInstance(rawValue) ) {
            /*
             * The TSV serialisation format stores list values as a single string, from
             * which list values must be extracted by splitting the string around '|'
             * characters.
             * 
             * This has the side effect of allowing list-valued slots to be (mis)used as if
             * they were single-valued, which is strictly speaking invalid but happens in
             * the wild (including in the examples shown in the SSSOM documentation!).
             */
            List<String> value = new ArrayList<String>();
            for ( String item : String.class.cast(rawValue).split("\\|") ) {
                value.add(slot.isEntityReference() ? prefixManager.expandIdentifier(item) : item);
            }
            slot.setValue(object, value);
        } else if ( type == Map.class && isMapOf(rawValue, String.class) ) {
            @SuppressWarnings("unchecked")
            Map<String, String> value = Map.class.cast(rawValue);
            slot.setValue(object, value);
        } else if ( type == LocalDate.class && String.class.isInstance(rawValue) ) {
            try {
                slot.setValue(object, LocalDate.parse(String.class.cast(rawValue)));
            } catch ( DateTimeParseException e ) {
                onTypingError(slot.getName(), e);
            }
        } else if ( type == Double.class && String.class.isInstance(rawValue) ) {
            try {
                slot.setValue(object, Double.valueOf(String.class.cast(rawValue)));
            } catch ( NumberFormatException e ) {
                onTypingError(slot.getName(), e);
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
        } else if ( rawValue == null ) {
            slot.setValue(object, null);
        } else {
            onTypingError(slot.getName());
        }
    }
}
