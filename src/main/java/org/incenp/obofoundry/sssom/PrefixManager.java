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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;

/**
 * Helper class to shorten/expand identifiers based on a prefix map.
 */
public class PrefixManager {

    private Map<String, String> prefixMap = new HashMap<String, String>();
    private Map<String, String> iri2CurieCache = new HashMap<String, String>();

    /**
     * Creates a new instance with the builtin prefixes.
     */
    public PrefixManager() {
        for ( BuiltinPrefix builtinPrefix : BuiltinPrefix.values() ) {
            prefixMap.put(builtinPrefix.getPrefixName(), builtinPrefix.getPrefix());
        }
    }

    /**
     * Adds a new prefix.
     * 
     * @param prefixName The short name of the prefix, as it would appear in a
     *                   CURIE.
     * @param prefix     The expanded URL prefix;
     */
    public void add(String prefixName, String prefix) {
        prefixMap.put(prefixName, prefix);
    }

    /**
     * Adds new prefixes.
     * 
     * @param map A map associating prefix names to prefixes.
     */
    public void add(Map<? extends String, ? extends String> map) {
        prefixMap.putAll(map);
    }

    /**
     * Shortens an identifier according to the prefix map.
     * 
     * @param iri The identifier to shorten.
     * @return The shortened identifier, or the original identifier if it could not
     *         be shortened.
     */
    public String shortenIdentifier(String iri) {
        String shortId = iri2CurieCache.getOrDefault(iri, null);

        if ( shortId == null ) {
            String bestPrefix = null;
            int bestLength = 0;

            for ( String prefixName : prefixMap.keySet() ) {
                String prefix = prefixMap.get(prefixName);
                if ( iri.startsWith(prefix) && prefix.length() > bestLength ) {
                    bestPrefix = prefixName;
                    bestLength = prefix.length();
                }
            }

            if ( bestPrefix != null ) {
                shortId = bestPrefix + ":" + iri.substring(bestLength);
                iri2CurieCache.put(iri, shortId);
            }
        }

        return shortId != null ? shortId : iri;
    }

    /**
     * Shortens all identifiers in the given list. The original list is left
     * untouched.
     * 
     * @param iris The list of identifiers to shorten.
     * @return A new list with the shortened identifiers.
     */
    public List<String> shortenIdentifiers(List<String> iris) {
        return shortenIdentifiers(iris, false);
    }

    /**
     * Shortens all identifiers in the given list.
     * 
     * @param iris    The list of identifiers to shorten.
     * @param inPlace If {@code true}, the original list is modified; otherwise it
     *                is left untouched and a new list is returned.
     * @return A list with the shortened identifiers. If {@code inPlace} is
     *         {@code true}, this is the original list.
     */
    public List<String> shortenIdentifiers(List<String> iris, boolean inPlace) {
        List<String> shortIds = new ArrayList<String>();
        for ( String iri : iris ) {
            shortIds.add(shortenIdentifier(iri));
        }

        if ( inPlace ) {
            iris.clear();
            iris.addAll(shortIds);
            shortIds = iris;
        }

        return shortIds;
    }

    /**
     * Expands a shortened identifier into its long, canonical form.
     * 
     * @param curie The short identifier to expand.
     * @return The full-length identifier, or the original identifier if it was not
     *         in CURIE form.
     * @throws SSSOMFormatException If the provided identifier is a CURIE that is
     *                              using an undeclared prefix.
     */
    public String expandIdentifier(String curie) throws SSSOMFormatException {
        if ( curie.startsWith("http") ) {
            return curie;
        }

        String[] parts = curie.split(":", 2);
        if ( parts.length == 1 ) {
            return curie;
        }

        String prefix = prefixMap.get(parts[0]);
        if ( prefix == null ) {
            throw new SSSOMFormatException("Undeclared prefix");
        }

        return prefix + parts[1];
    }

    /**
     * Expands all identifiers in the given list. The original list is left
     * untouched.
     * 
     * @param curies The list of short identifiers to expand.
     * @return A new list with the expanded identifiers.
     * @throws SSSOMFormatException If an identifier in the list is a CURIE that is
     *                              using an undeclared prefix.
     */
    public List<String> expandIdentifiers(List<String> curies) throws SSSOMFormatException {
        return expandIdentifiers(curies, false);
    }

    /**
     * Expands all identifiers in the given list.
     * 
     * @param curies  The list of short identifiers to expand.
     * @param inPlace If {@code true}, the original list is modified; otherwise it
     *                is left untouched and a new list is returned.
     * @return A list with the expanded identifiers. If {@code inPlace} is
     *         {@code true}, this is the original list.
     * @throws SSSOMFormatException If an identifier in the list is a CURIE that is
     *                              using an undeclared prefix.
     */
    public List<String> expandIdentifiers(List<String> curies, boolean inPlace) throws SSSOMFormatException {
        List<String> iris = new ArrayList<String>();
        for ( String curie : curies ) {
            iris.add(expandIdentifier(curie));
        }

        if ( inPlace ) {
            curies.clear();
            curies.addAll(iris);
            iris = curies;
        }

        return iris;
    }
}
