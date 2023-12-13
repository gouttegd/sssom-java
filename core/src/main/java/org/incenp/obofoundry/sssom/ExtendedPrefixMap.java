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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A basic implementation of the “Extended Prefix Map” concept promoted by the
 * Bioregistry.
 * <p>
 * The implementation is basic because it is never intended to actually be used
 * to manage prefixes. The only purpose here is to be able to perform a single
 * “reconciliation” operation, to force a mapping set to use the “canonical”
 * prefixes set forth in a given extended prefix map.
 * <p>
 * This is another case of “the usual bioinformatics paradigm of ignoring the
 * specs and inferring a format from a few examples”, but then again, the entire
 * “spec” is nothing more than a single example, so we don’t have much choice.
 * 
 * @see <a href=
 *      "https://github.com/cthoyt/curies/blob/main/docs/source/struct.rst#extended-prefix-maps">Extended
 *      prefix map specification</a>
 * 
 */
public class ExtendedPrefixMap {

    private BufferedReader mapReader;
    private HashMap<String, String> prefixMap = new HashMap<String, String>();
    private HashMap<String, String> synonymMap = new HashMap<String, String>();
    private HashMap<String, String> iri2CanonCache = new HashMap<String, String>();
    private HashSet<String> canonicalPrefixes = new HashSet<String>();

    /**
     * Creates a new extended prefix map from the specified file.
     * 
     * @param file The file to read the map from.
     * @throws IOException If any error occurs when reading the file (including a
     *                     format error).
     */
    public ExtendedPrefixMap(File file) throws IOException {
        mapReader = new BufferedReader(new FileReader(file));
        read();
    }

    /**
     * Creates a new extended prefix map from the specified filename.
     * 
     * @param filename The name of the file to read the map from.
     * @throws IOException If any error occurs when reading the file (including a
     *                     format error).
     */
    public ExtendedPrefixMap(String filename) throws IOException {
        this(new File(filename));
    }

    /**
     * Creates a new extended prefix map from the specified stream.
     * 
     * @param stream The stream to read the map from.
     * @throws IOException If any error occurs when reading the file (including a
     *                     format error).
     */
    public ExtendedPrefixMap(InputStream stream) throws IOException {
        mapReader = new BufferedReader(new InputStreamReader(stream));
        read();
    }

    /**
     * Get a simple prefix map associating the canonical prefix name to the
     * canonical URL prefix.
     * 
     * @return The simple, canonical prefix map.
     */
    public Map<String, String> getSimplePrefixMap() {
        return prefixMap;
    }

    /**
     * Canonicalises an IRI. This method checks whether the specified IRI uses a
     * prefix that is a synonym of a canonical prefix, and returns a new IRI using
     * the corresponding canonical prefix.
     * 
     * @param iri The IRI to canonicalise.
     * @return The canonical form of the IRI; may be identical to the original IRI
     *         if (1) that URI was already using the canonical prefix, or (2) no
     *         matching prefix was found in the extended map.
     */
    public String canonicalise(String iri) {
        String canonIri = iri2CanonCache.getOrDefault(iri, null);

        if ( canonIri == null ) {
            String bestPrefix = null;
            int bestLength = 0;

            // First check if this is already a canonical prefix
            for ( String canon : canonicalPrefixes ) {
                if ( iri.startsWith(canon) && canon.length() > bestLength ) {
                    bestPrefix = canon;
                    bestLength = canon.length();
                }
            }
            if ( bestPrefix != null ) {
                // It is, so we can return it as is
                iri2CanonCache.put(iri, iri);
                return iri;
            }

            // No luck, search through the prefix synonyms
            for ( String synonym : synonymMap.keySet() ) {
                if ( iri.startsWith(synonym) && synonym.length() > bestLength ) {
                    bestPrefix = synonym;
                    bestLength = synonym.length();
                }
            }
            if ( bestPrefix != null ) {
                canonIri = synonymMap.get(bestPrefix) + iri.substring(bestLength);
                iri2CanonCache.put(iri, canonIri);
            }
        }

        return canonIri != null ? canonIri : iri;
    }

    /**
     * Canonicalises a list of IRIs.
     * 
     * @param iris    The IRIs to canonicalise.
     * @param inPlace If {@code true}, the original list will be modified and
     *                returned; otherwise a new list with canonicalised IRIs will be
     *                returned.
     * @return A list with canonicalised IRIs.
     */
    public List<String> canonicalise(List<String> iris, boolean inPlace) {
        List<String> canonIris = new ArrayList<String>();
        for ( String iri : iris ) {
            canonIris.add(canonicalise(iri));
        }

        if ( inPlace ) {
            iris.clear();
            iris.addAll(canonIris);
            canonIris = iris;
        }

        return iris;
    }

    /**
     * Canonicalises a list of mappings. This method performs a canonicalisation on
     * the values of all slots of type EntityReference.
     * 
     * @param mappings The mappings of canonicalise.
     */
    public void canonicalise(List<Mapping> mappings) {
        Visitor<Mapping> v = new Visitor<Mapping>();
        for ( Mapping mapping : mappings ) {
            SlotHelper.getMappingHelper().visitSlots(mapping, v);
        }
    }

    /**
     * Canonicalises a mapping set. This performs a canonicalisation on the values
     * of all slots of type EntityReference in the set itself, then in the
     * individual mappings. The original prefix map of the set is updated with the
     * canonical prefix map.
     * 
     * @param set The mapping set to canonicalise.
     */
    public void canonicalise(MappingSet set) {
        SlotHelper.getMappingSetHelper().visitSlots(set, new Visitor<MappingSet>());
        set.getCurieMap().putAll(prefixMap);
        canonicalise(set.getMappings());
    }

    // Actual reading of the EPM
    private void read() throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ExtendedPrefixMapEntry[] rawMap = mapper.readerFor(ExtendedPrefixMapEntry[].class).readValue(mapReader);

        for (ExtendedPrefixMapEntry entry : rawMap) {
            prefixMap.put(entry.prefixName, entry.prefix);
            canonicalPrefixes.add(entry.prefix);
            if ( entry.prefixSynonyms != null) {
                for ( String prefixSynonym : entry.prefixSynonyms ) {
                    synonymMap.put(prefixSynonym, entry.prefix);
                }
            }
        }
    }

    // Helper object to represent an entry in the EPM
    static class ExtendedPrefixMapEntry {
        @JsonProperty("prefix")
        public String prefixName;
        @JsonProperty("uri_prefix")
        public String prefix;
        @JsonProperty("prefix_synonyms")
        public List<String> synonyms;
        @JsonProperty("uri_prefix_synonyms")
        public List<String> prefixSynonyms;
    }

    // Visitor object to update EntityReference-typed slots in mappings and mapping
    // sets
    class Visitor<T> extends SlotVisitorBase<T, Void> {

        @Override
        public Void visit(Slot<T> slot, T object, String value) {
            if ( slot.isEntityReference() ) {
                slot.setValue(object, canonicalise(value));
            }
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, List<String> values) {
            if ( slot.isEntityReference() ) {
                canonicalise(values, true);
            }
            return null;
        }
    }
}
