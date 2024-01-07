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

import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;

/**
 * A helper class to deal with extension slots and their definitions.
 */
public class ExtensionHelper {

    /** Default namespace for any undefined extension. */
    public static final String UNDEFINED_EXTENSION_NAMESPACE = "https://w3id.org/sssom/undefined_extensions/";

    private ExtraMetadataPolicy policy;
    private PrefixManager prefixManager;
    private HashMap<String, ExtensionDefinition> definedExtensionsBySlotName = new HashMap<String, ExtensionDefinition>();

    /**
     * Creates a new instance.
     * 
     * @param policy        The policy for processing non-standard metadata slot.
     * @param prefixManager The prefix manager to resolve shortened identifiers.
     */
    public ExtensionHelper(ExtraMetadataPolicy policy, PrefixManager prefixManager) {
        this.policy = policy;
        this.prefixManager = prefixManager;
    }

    /**
     * Extracts extension definitions from a dictionary representation of a mapping
     * set.
     * <p>
     * This method extracts the {@code extension_definitions} key from the
     * dictionary and parses its contents into actual extension definitions. The
     * {@code extension_definitions} is removed from the dictionary in the process.
     * <p>
     * No definition is extracted if the non-standard metadata policy is to ignore
     * all non-standard metadata slots.
     * 
     * @param rawMap The mapping set, as a raw dictionary of objects.
     * @throws SSSOMFormatException If the contents of the
     *                              {@code extension_definitions} key does not match
     *                              the expected format as per the SSSOM
     *                              specification.
     */
    public void processDefinitions(Map<String, Object> rawMap) throws SSSOMFormatException {
        Object rawDefinitions = rawMap.get("extension_definitions");
        rawMap.remove("extension_definitions");

        if ( policy == ExtraMetadataPolicy.NONE ) {
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
                    String typeHint = definition.getOrDefault("type_hint", "xsd:string");

                    if ( slotName != null && property != null ) {
                        ExtensionDefinition def = new ExtensionDefinition(slotName,
                                prefixManager.expandIdentifier(property), prefixManager.expandIdentifier(typeHint));
                        definedExtensionsBySlotName.put(slotName, def);
                    }
                } else {
                    onTypingError("extension_definitions");
                }
            }
        } else {
            onTypingError("extension_definitions");
        }
    }

    /**
     * Get all the known extension definitions. This includes the auto-generated
     * definitions for undefined extensions, if the policy is to accept undefined
     * extensions.
     * 
     * @return All the defined extensions known to this object.
     */
    public List<ExtensionDefinition> getDefinitions() {
        ArrayList<ExtensionDefinition> definitions = new ArrayList<ExtensionDefinition>(
                definedExtensionsBySlotName.values());
        definitions.sort((e1, e2) -> e1.getProperty().compareTo(e2.getProperty()));
        return definitions;
    }

    /**
     * Indicates whether this helper contains any definition.
     * 
     * @return {@code true} if this helper object has at least one definition,
     *         {@code false} otherwise.
     */
    public boolean hasDefinitions() {
        return !definedExtensionsBySlotName.isEmpty();
    }

    /**
     * Processes a non-standard metadata slot. This method takes into account the
     * current policy for dealing with non-standard slot and the current list of
     * defined extension slots to decide whether to accept the slot or not and to
     * parse its value.
     * 
     * @param extensions A map of extension slot values. If the slot to be processed
     *                   is accepted, its value will be added to that map under the
     *                   property associated with the slot.
     * @param slotName   The name of the unknown (non-standard) metadata slot.
     * @param rawValue   The value of the unknown slot.
     * @throws SSSOMFormatException If the value is not of the expected type for the
     *                              slot.
     */
    public void processUnknownSlot(Map<String, ExtensionValue> extensions, String slotName, Object rawValue)
            throws SSSOMFormatException {
        if ( policy == ExtraMetadataPolicy.NONE ) {
            return;
        }

        ExtensionDefinition definition = definedExtensionsBySlotName.get(slotName);
        if ( definition == null && policy == ExtraMetadataPolicy.UNDEFINED ) {
            // Forge a default definition for the undefined slot
            definition = new ExtensionDefinition(slotName, UNDEFINED_EXTENSION_NAMESPACE + slotName);
            definedExtensionsBySlotName.put(slotName, definition);
        }

        if ( definition != null ) {
            if ( !String.class.isInstance(rawValue) ) {
                onTypingError(slotName);
            }
            String value = String.class.cast(rawValue);

            ExtensionValue parsedValue = null;
            switch ( definition.getEffectiveType() ) {
            case STRING:
                parsedValue = new ExtensionValue(value);
                break;

            case INTEGER:
                try {
                    parsedValue = new ExtensionValue(Integer.parseInt(value));
                } catch ( NumberFormatException nfe ) {
                    onTypingError(slotName, nfe);
                }
                break;

            case DOUBLE:
                try {
                    parsedValue = new ExtensionValue(Double.parseDouble(value));
                } catch ( NumberFormatException nfe) {
                    onTypingError(slotName, nfe);
                }
                break;

            case BOOLEAN:
                if ( value.equals("true") ) {
                    parsedValue = new ExtensionValue(true);
                } else if ( value.equals("false") ) {
                    parsedValue = new ExtensionValue(false);
                } else {
                    onTypingError(slotName);
                }
                break;

            case DATE:
                try {
                    parsedValue = new ExtensionValue(LocalDate.parse(value));
                } catch ( DateTimeParseException dtpe ) {
                    onTypingError(slotName, dtpe);
                }
                break;

            case DATETIME:
                try {
                    parsedValue = new ExtensionValue(ZonedDateTime.parse(value));
                } catch ( DateTimeParseException dtpe ) {
                    onTypingError(slotName, dtpe);
                }
                break;

            case IDENTIFIER:
                parsedValue = new ExtensionValue(prefixManager.expandIdentifier(value), true);
                break;

            case OTHER:
                parsedValue = new ExtensionValue((Object) value);
                break;
            }

            extensions.put(definition.getProperty(), parsedValue);
        }
    }

    /*
     * Helper method to cast a SSSOMFormatException.
     */
    private void onTypingError(String slotName, Throwable innerException) throws SSSOMFormatException {
        throw new SSSOMFormatException(String.format("Typing error when parsing '%s'", slotName), innerException);
    }

    /*
     * Same, but without an underlying exception.
     */
    private void onTypingError(String slotName) throws SSSOMFormatException {
        onTypingError(slotName, null);
    }
}
