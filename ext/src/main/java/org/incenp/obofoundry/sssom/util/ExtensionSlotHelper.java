/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.ExtensionSlotManager;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Helper class to work with extension slots.
 * <p>
 * This class provides static method to convert extension slots to and from
 * <code>other</code> slots, which were the original mechanism in SSSOM to store
 * non-standard metadata. That mechanism is now deprecated in favour of
 * extension slots, but since extension slots are not supported by all
 * implementations, it can be useful to have a way to fallback to the old
 * mechanism.
 * <p>
 * This should be considered experimental.
 */
public class ExtensionSlotHelper {

    private ExtensionSlotManager slotManager;
    private PrefixManager prefixManager;

    private ExtensionSlotHelper(MappingSet ms) {
        slotManager = new ExtensionSlotManager(ExtraMetadataPolicy.UNDEFINED);
        if ( ms.getExtensionDefinitions() != null ) {
            for ( ExtensionDefinition def : ms.getExtensionDefinitions() ) {
                slotManager.addDefinition(def.getSlotName(), def.getProperty(), def.getTypeHint());
            }
        }

        prefixManager = new PrefixManager();
        if ( ms.getCurieMap() != null ) {
            prefixManager.add(ms.getCurieMap());
        }
    }

    /**
     * Encodes extension slots into <code>other</code> values.
     * 
     * @param ms     The mapping set whose extension slots are to to converted to
     *               <code>other</code> values.
     * @param remove If <code>true</code>, the extension slots will be removed from
     *               the set after they have been converted.
     */
    public static void toOther(MappingSet ms, boolean remove) {
        ExtensionSlotHelper helper = new ExtensionSlotHelper(ms);

        if ( ms.getExtensions() != null && !ms.getExtensions().isEmpty() ) {
            ms.setOther(helper.toOther(ms.getExtensions()));
            if ( remove ) {
                ms.setExtensions(null);
            }
        }

        for ( Mapping m : ms.getMappings(true) ) {
            if ( m.getExtensions() != null && !m.getExtensions().isEmpty() ) {
                m.setOther(helper.toOther(m.getExtensions()));
                if ( remove ) {
                    m.setExtensions(null);
                }
            }
        }
    }

    private String toOther(Map<String, ExtensionValue> extensions) {
        ArrayList<String> tmp = new ArrayList<>();
        for ( String property : extensions.keySet() ) {
            ExtensionDefinition def = slotManager.getDefinitionForProperty(property);
            if ( def != null ) {
                ExtensionValue value = extensions.get(property);
                String strValue = value.isIdentifier() ? prefixManager.shortenIdentifier(value.asString())
                        : value.toString();
                tmp.add(String.format("%s=%s", def.getSlotName(), strValue));
            }
        }
        if ( !tmp.isEmpty() ) {
            tmp.sort(Collator.getInstance());
            return String.join("|", tmp);
        }
        return null;
    }

    /**
     * Decodes the values of <code>other</code> slots into proper extension slots.
     * 
     * @param ms     The mapping set whose <code>other</code> slots are to be
     *               converted into extension slots.
     * @param remove If <code>true</code>, the <code>other</code> slots will be
     *               removed after the conversion.
     */
    public static void fromOther(MappingSet ms, boolean remove) {
        ExtensionSlotHelper helper = new ExtensionSlotHelper(ms);

        if ( ms.getOther() != null ) {
            ms.getExtensions(true).putAll(helper.fromOther(ms.getOther()));
            if ( remove ) {
                ms.setOther(null);
            }
        }

        for ( Mapping m : ms.getMappings() ) {
            if ( m.getOther() != null ) {
                m.getExtensions(true).putAll(helper.fromOther(m.getOther()));
                if ( remove ) {
                    m.setOther(null);
                }
            }
        }

        if ( !helper.slotManager.isEmpty() ) {
            ms.setExtensionDefinitions(helper.slotManager.getDefinitions(false, false));
        }
    }

    private Map<String, ExtensionValue> fromOther(String other) {
        HashMap<String, ExtensionValue> extensions = new HashMap<>();
        String[] items = other.split("\\|");
        for ( String item : items ) {
            String[] parts = item.split("=", 2);
            if ( parts.length != 2 ) {
                // Ignore invalid item
                continue;
            }

            String name = parts[0];
            String value = parts[1];

            ExtensionDefinition def = slotManager.getDefinitionForSlot(name);
            ExtensionValue ev = null;
            switch ( def.getEffectiveType() ) {
            case BOOLEAN:
                ev = new ExtensionValue(value.equals("true"));
                break;
            case DATE:
                try {
                    ev = new ExtensionValue(LocalDate.parse(value));
                } catch ( DateTimeParseException dtpe ) {
                }
                break;
            case DATETIME:
                try {
                    ev = new ExtensionValue(ZonedDateTime.parse(value));
                } catch ( DateTimeParseException dtpe ) {
                }
                break;
            case DOUBLE:
                try {
                    ev = new ExtensionValue(Double.parseDouble(value));
                } catch ( NumberFormatException nfe ) {
                }
                break;
            case IDENTIFIER:
                ev = new ExtensionValue(prefixManager.expandIdentifier(value), true);
                break;
            case INTEGER:
                try {
                    ev = new ExtensionValue(Integer.parseInt(value));
                } catch ( NumberFormatException nfe ) {
                }
                break;
            case OTHER:
                ev = new ExtensionValue(value);
                break;
            case STRING:
                ev = new ExtensionValue(value);
                break;
            case URI:
                try {
                    ev = new ExtensionValue(new URI(value));
                } catch ( URISyntaxException e ) {
                }
                break;
            }

            if ( ev != null ) {
                extensions.put(def.getProperty(), ev);
            }
        }
        return extensions;
    }
}
