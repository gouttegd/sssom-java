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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.ValueType;

/**
 * A helper class to deal with non-standard metadata slots and their
 * definitions.
 */
public class ExtensionSlotManager {

    /**
     * The default namespace for auto-generated properties for undefined extensions.
     */
    public static final String UNDEFINED_EXTENSION_NAMESPACE = "http://sssom.invalid/";

    /**
     * The default type for extensions that are either undefined or that do not have
     * an explicit type.
     */
    public static final String DEFAULT_TYPE_HINT = "xsd:string";

    // Slot names must match this pattern to be valid.
    private static final Pattern slotNamePattern = Pattern.compile("^\\p{Alnum}[\\p{Alnum}._-]*$");

    private ExtraMetadataPolicy policy;
    private PrefixManager prefixManager;

    private HashMap<String, ExtensionDefinition> definedExtensionsBySlotName = new HashMap<String, ExtensionDefinition>();
    private HashMap<String, ExtensionDefinition> definedExtensionsByProperty = new HashMap<String, ExtensionDefinition>();
    private HashSet<String> usedPrefixes = new HashSet<String>();
    private HashSet<String> mappingLevelProperties = new HashSet<String>();

    /**
     * Creates a new instance.
     * 
     * @param policy        The policy that determines how to deal with non-standard
     *                      metadata slots. When the policy is set to
     *                      {@link ExtraMetadataPolicy#NONE}, most operations of
     *                      this object are no-op.
     * @param prefixManager The prefix manager used to resolve any compact IRI
     *                      encountered in extension definitions.
     */
    public ExtensionSlotManager(ExtraMetadataPolicy policy, PrefixManager prefixManager) {
        this.policy = policy;
        this.prefixManager = prefixManager;
    }

    /**
     * Defines a new extension slot.
     * 
     * @param slotName The name of the slot, as it appears in the YAML metadata
     *                 block (for set-level extensions) or as a column header (for
     *                 mapping-level extensions).
     * @param property The property associated with the slot.
     * @param typeHint The expected type of values for this slot. May be
     *                 {@code null}, in which case the default is
     *                 {@code xsd:string}.
     * @return {@code true} if the new slot has been defined correctly. or
     *         {@code false} if either the {@code slot_name} or the {@code property}
     *         is {@code null}, or if the slot name is invalid.
     */
    public boolean addDefinition(String slotName, String property, String typeHint) {
        if ( policy == ExtraMetadataPolicy.NONE ) {
            return true;
        }

        if ( slotName == null || !isExtensionSlotNameValid(slotName) || property == null ) {
            return false;
        }

        if ( typeHint == null ) {
            typeHint = DEFAULT_TYPE_HINT;
        }

        ExtensionDefinition existingDef = definedExtensionsBySlotName.get(slotName);
        if ( existingDef != null ) {
            // Can't have two definitions with the same slot name, override the previous one
            definedExtensionsByProperty.remove(existingDef.getSlotName());
        }

        ExtensionDefinition def = new ExtensionDefinition(slotName, prefixManager.expandIdentifier(property),
                prefixManager.expandIdentifier(typeHint));
        definedExtensionsBySlotName.put(slotName, def);
        definedExtensionsByProperty.put(property, def);

        return true;
    }

    /**
     * Looks up for an existing definition for the specified slot name. If no
     * definition exists and the policy is set to
     * {@link ExtraMetadataPolicy#UNDEFINED}, then a new definition is automatically
     * generated on the fly.
     * 
     * @param slotName The name of the slot for which a definition is requested.
     * @return The slot definition. May be {@code null} if the policy is set to
     *         {@link ExtraMetadataPolicy#NONE}, or if it is set to
     *         {@link ExtraMetadataPolicy#DEFINED} and no prior definition exists.
     */
    public ExtensionDefinition getDefinitionForSlot(String slotName) {
        if ( policy == ExtraMetadataPolicy.NONE ) {
            return null;
        }

        ExtensionDefinition definition = definedExtensionsBySlotName.get(slotName);
        if ( definition == null && policy == ExtraMetadataPolicy.UNDEFINED && isExtensionSlotNameValid(slotName) ) {
            definition = new ExtensionDefinition(slotName, UNDEFINED_EXTENSION_NAMESPACE + slotName);
            definedExtensionsBySlotName.put(slotName, definition);
            definedExtensionsByProperty.put(definition.getProperty(), definition);
        }

        return definition;
    }

    /**
     * Gets definitions for all extensions used in the specified mapping set.
     * Existing definitions found in the set itself will be used when available,
     * otherwise definitions will be auto-generated when needed. Note that existing
     * definitions in the set that are never used throughout the set are ignored.
     * 
     * @param ms The mapping set to get extension definitions from.
     */
    public void fillFromExistingExtensions(MappingSet ms) {
        if ( policy == ExtraMetadataPolicy.NONE ) {
            return;
        }

        // Iterate through all extension values in the entire set
        Map<String, Set<ValueType>> typesByProperty = new HashMap<String, Set<ValueType>>();
        fillFromExistingExtensions(ms.getExtensions(), typesByProperty, false);
        for ( Mapping m : ms.getMappings() ) {
            fillFromExistingExtensions(m.getExtensions(), typesByProperty, true);
        }

        // Get the theoretical definitions from the set metadata
        Map<String, ExtensionDefinition> setDefinitionsBySlotName = new HashMap<String,ExtensionDefinition>();
        if (ms.getExtensionDefinitions() != null) {
            for ( ExtensionDefinition def : ms.getExtensionDefinitions() ) {
                // Ensure slot name unicity (any prior definition with same name is replaced)
                setDefinitionsBySlotName.put(def.getSlotName(), def);
            }
        }
        // Re-arrange to look up by property
        Map<String, ExtensionDefinition> setDefinitionsByProperty = new HashMap<String, ExtensionDefinition>();
        setDefinitionsBySlotName.forEach((k, v) -> setDefinitionsByProperty.put(v.getProperty(), v));

        // Iterate through the effectively used properties and reconcile when needed
        int noSlotNameCounter = 0;
        for ( String property : typesByProperty.keySet() ) {
            ValueType effectiveType = ValueType.STRING;
            Set<ValueType> valueTypes = typesByProperty.get(property);
            if ( valueTypes.size() == 1 ) {
                // All values are of the same type, we can use it
                effectiveType = valueTypes.iterator().next();
            }

            // Do we have a definition in the set?
            ExtensionDefinition definition = setDefinitionsByProperty.get(property);
            if ( definition == null ) {
                // No definition, so we need to figure out a slot name
                String slotName = null;
                if ( property.startsWith(UNDEFINED_EXTENSION_NAMESPACE) ) {
                    slotName = property.substring(UNDEFINED_EXTENSION_NAMESPACE.length());
                } else {
                    slotName = String.format("extra%d", ++noSlotNameCounter);
                }

                if (effectiveType == ValueType.OTHER) {
                    // We don't know the real type hint, so fall back to string
                    effectiveType = ValueType.STRING;
                }

                definition = new ExtensionDefinition(slotName, property, effectiveType.toString());
            } else if (definition.getEffectiveType() != effectiveType) {
                // We have a definition but with a different type, fall back to string
                effectiveType = ValueType.STRING;
                definition = new ExtensionDefinition(definition.getSlotName(), property, effectiveType.toString());
            }

            if ( !isExtensionSlotNameValid(definition.getSlotName()) ) {
                continue;
            }

            if (policy == ExtraMetadataPolicy.DEFINED) {
                // Extensions will be defined, so make sure we have the prefix for every property
                usedPrefixes.add(prefixManager.getPrefixName(property));
            }

            definedExtensionsByProperty.put(property, definition);
            definedExtensionsBySlotName.put(definition.getSlotName(), definition);
        }
    }

    /**
     * Gets all the prefix names used in extensions and extension definitions.
     * <p>
     * When this object is initialised from a mapping set (through
     * {@link #fillFromExistingExtensions(MappingSet)}), all prefix names used
     * throughout all extension values and all extension definition will be
     * collected. This allows to know which prefixes are required to compact all
     * IRIs present in extensions. The prefix names are those from the prefix
     * manager used by this object.
     * 
     * @return The set of all prefix names used in extensions.
     */
    public Set<String> getUsedPrefixes() {
        return usedPrefixes;
    }

    /**
     * Indicates whether this object contains any extension definition.
     * 
     * @return {@code true} if this object knows of no extension, otherwise
     *         {@code true}.
     */
    public boolean isEmpty() {
        return definedExtensionsBySlotName.isEmpty();
    }

    /**
     * Checks whether the specified name is usable as an extension slot name.
     * 
     * @param name The name to check.
     * @return {@code true} if the name is a valid extension slot name,
     *         {@code false} otherwise.
     */
    public static boolean isExtensionSlotNameValid(String name) {
        return slotNamePattern.matcher(name).matches();
    }

    /**
     * Gets the definitions known to this manager.
     * 
     * @param sorted           {@code true} to get a list where the definitions are
     *                         sorted on their properties; if {@code false}, the
     *                         order of definitions in the returned list is
     *                         unspecified.
     * @param mappingLevelOnly If {@code true}, returns only the definitions for
     *                         extensions that are used at the mapping level,
     *                         excluding any definition for extensions that are only
     *                         found at the set level; otherwise, returns all
     *                         definitions.
     * @return The list of definitions. This is a copy of the internal list, any
     *         change on the returned list will not affect the state of this object.
     */
    public List<ExtensionDefinition> getDefinitions(boolean sorted, boolean mappingLevelOnly) {
        ArrayList<ExtensionDefinition> defs = new ArrayList<ExtensionDefinition>();

        if ( policy != ExtraMetadataPolicy.NONE ) {
            for ( ExtensionDefinition definition : definedExtensionsBySlotName.values() ) {
                if ( !mappingLevelOnly || mappingLevelProperties.contains(definition.getProperty()) ) {
                    defs.add(definition);
                }
            }

            if ( sorted ) {
                defs.sort((d1, d2) -> d1.getProperty().compareTo(d2.getProperty()));
            }
        }

        return defs;
    }

    private void fillFromExistingExtensions(Map<String, ExtensionValue> extensions,
            Map<String, Set<ValueType>> typesByProperty, boolean mappingLevel) {
        if ( extensions != null ) {
            for ( String property : extensions.keySet() ) {
                ExtensionValue value = extensions.get(property);
                if ( value.isIdentifier() ) {
                    usedPrefixes.add(prefixManager.getPrefixName(value.asString()));
                }
                typesByProperty.computeIfAbsent(property, k -> new HashSet<ValueType>()).add(value.getType());
                if ( mappingLevel ) {
                    mappingLevelProperties.add(property);
                }
            }
        }
    }
}
