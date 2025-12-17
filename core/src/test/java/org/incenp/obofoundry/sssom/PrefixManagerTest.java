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
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PrefixManagerTest {
	
	@Test
	void testBuiltinPrefixExpansion() {
		PrefixManager pm = new PrefixManager();
		
        Assertions.assertEquals("https://w3id.org/sssom/ManualMappingCuration",
                pm.expandIdentifier("sssom:ManualMappingCuration"));
        Assertions.assertEquals("http://www.w3.org/2002/07/owl#equivalentClass",
                pm.expandIdentifier("owl:equivalentClass"));
        Assertions.assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#resource",
                pm.expandIdentifier("rdf:resource"));
        Assertions.assertEquals("http://www.w3.org/2000/01/rdf-schema#label", pm.expandIdentifier("rdfs:label"));
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#exactMatch",
                pm.expandIdentifier("skos:exactMatch"));
        Assertions.assertEquals("https://w3id.org/semapv/vocab/crossSpeciesExactMatch",
                pm.expandIdentifier("semapv:crossSpeciesExactMatch"));
	}

    @Test
    void testBuiltinPrefixShortening() {
        PrefixManager pm = new PrefixManager();

        Assertions.assertEquals("sssom:ManualMappingCuration",
                pm.shortenIdentifier("https://w3id.org/sssom/ManualMappingCuration"));
        Assertions.assertEquals("owl:equivalentClass",
                pm.shortenIdentifier("http://www.w3.org/2002/07/owl#equivalentClass"));
        Assertions.assertEquals("rdf:resource",
                pm.shortenIdentifier("http://www.w3.org/1999/02/22-rdf-syntax-ns#resource"));
        Assertions.assertEquals("rdfs:label", pm.shortenIdentifier("http://www.w3.org/2000/01/rdf-schema#label"));
        Assertions.assertEquals("skos:exactMatch",
                pm.shortenIdentifier("http://www.w3.org/2004/02/skos/core#exactMatch"));
        Assertions.assertEquals("semapv:crossSpeciesExactMatch",
                pm.shortenIdentifier("https://w3id.org/semapv/vocab/crossSpeciesExactMatch"));
    }

    @Test
    void testUnknownPrefix() {
        PrefixManager pm = new PrefixManager();

        Assertions.assertEquals("unknown:prefix", pm.expandIdentifier("unknown:prefix"));
        Assertions.assertTrue(pm.getUnresolvedPrefixNames().contains("unknown"));

        Assertions.assertEquals("http://example.org/test", pm.shortenIdentifier("http://example.org/test"));
    }

    @Test
    void testListExpansion() {
        PrefixManager pm = new PrefixManager();

        List<String> source = new ArrayList<String>();
        source.add("skos:exactMatch");
        source.add("skos:broadMatch");
        source.add("skos:narrowMatch");

        List<String> expanded = pm.expandIdentifiers(source);
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#exactMatch", expanded.get(0));
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#broadMatch", expanded.get(1));
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#narrowMatch", expanded.get(2));
        Assertions.assertEquals("skos:exactMatch", source.get(0));
        Assertions.assertEquals("skos:broadMatch", source.get(1));
        Assertions.assertEquals("skos:narrowMatch", source.get(2));

        pm.expandIdentifiers(source, true);
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#exactMatch", source.get(0));
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#broadMatch", source.get(1));
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#narrowMatch", source.get(2));
    }

    @Test
    void testListShortening() {
        PrefixManager pm = new PrefixManager();

        List<String> source = new ArrayList<String>();
        source.add("http://www.w3.org/2004/02/skos/core#exactMatch");
        source.add("http://www.w3.org/2004/02/skos/core#broadMatch");
        source.add("http://www.w3.org/2004/02/skos/core#narrowMatch");

        List<String> shortened = pm.shortenIdentifiers(source);
        Assertions.assertEquals("skos:exactMatch", shortened.get(0));
        Assertions.assertEquals("skos:broadMatch", shortened.get(1));
        Assertions.assertEquals("skos:narrowMatch", shortened.get(2));

        pm.shortenIdentifiers(source, true);
        Assertions.assertEquals("skos:exactMatch", source.get(0));
        Assertions.assertEquals("skos:broadMatch", source.get(1));
        Assertions.assertEquals("skos:narrowMatch", source.get(2));
    }

    @Test
    void testCustomPrefixes() {
        PrefixManager pm = new PrefixManager();
        pm.add("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        pm.add("UBERON", "http://purl.obolibrary.org/obo/UBERON_");

        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_00000001", pm.expandIdentifier("FBbt:00000001"));
        Assertions.assertEquals("http://purl.obolibrary.org/obo/UBERON_0000001", pm.expandIdentifier("UBERON:0000001"));

        Assertions.assertEquals("FBbt:99999999", pm.shortenIdentifier("http://purl.obolibrary.org/obo/FBbt_99999999"));
        Assertions.assertEquals("UBERON:9999999",
                pm.shortenIdentifier("http://purl.obolibrary.org/obo/UBERON_9999999"));
    }

    @Test
    void testShortenToLongestPrefix() {
        PrefixManager pm = new PrefixManager();
        pm.add("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        pm.add("obo", "http://purl.obolibrary.org/obo/");

        Assertions.assertEquals("http://purl.obolibrary.org/obo/FBbt_12345678",
                pm.expandIdentifier("obo:FBbt_12345678"));
        Assertions.assertEquals("FBbt:12345678", pm.shortenIdentifier("http://purl.obolibrary.org/obo/FBbt_12345678"));
    }

    @Test
    void testGetPrefixOrLocalName() {
        PrefixManager pm = new PrefixManager();

        Assertions.assertEquals("exactMatch", pm.getLocalName("http://www.w3.org/2004/02/skos/core#exactMatch"));
        Assertions.assertNull(pm.getLocalName("https://example.org/undeclared/prefix"));

        pm.add("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        pm.add("obo", "http://purl.obolibrary.org/obo/");
        Assertions.assertEquals("12345678", pm.getLocalName("http://purl.obolibrary.org/obo/FBbt_12345678"));
    }

    @Test
    void testGetPrefixName() {
        PrefixManager pm = new PrefixManager();

        Assertions.assertEquals("skos", pm.getPrefixName("http://www.w3.org/2004/02/skos/core#exactMatch"));
        Assertions.assertNull(pm.getPrefixName("https://example.org/undeclared/prefix"));

        pm.add("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        pm.add("obo", "http://purl.obolibrary.org/obo/");
        Assertions.assertEquals("FBbt", pm.getPrefixName("http://purl.obolibrary.org/obo/FBbt_12345678"));
    }
}
