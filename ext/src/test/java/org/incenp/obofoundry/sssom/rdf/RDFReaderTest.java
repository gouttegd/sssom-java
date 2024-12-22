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

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.model.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RDFReaderTest {

    @Test
    void testReadSimpleSet() throws SSSOMFormatException, IOException {
        RDFReader reader = new RDFReader("src/test/resources/sets/exo2c.ttl");
        MappingSet ms = reader.read();

        Assertions.assertEquals("O2C set", ms.getMappingSetTitle());
        Assertions.assertEquals(LocalDate.of(2023, 9, 13), ms.getPublicationDate());
        Assertions.assertEquals("https://creativecommons.org/licenses/by/4.0/", ms.getLicense());
        Assertions.assertTrue(ms.getCreatorId().contains("https://example.com/people/0000-0000-0002-5678"));
        Assertions.assertTrue(ms.getCreatorId().contains("https://example.org/people/0000-0000-0001-1234"));
        Assertions.assertEquals(8, ms.getMappings().size());
    }

    @Test
    void testReadSetWithAllSlotTypes() throws SSSOMFormatException, IOException {
        RDFReader reader = new RDFReader("src/test/resources/sets/test-slot-types.ttl");
        MappingSet ms = reader.read();
        Mapping m = ms.getMappings().get(0);

        Assertions.assertEquals("https://example.org/entities/0001", m.getSubjectId());
        Assertions.assertEquals("https://example.com/entities/0011", m.getObjectId());
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#closeMatch", m.getPredicateId());
        Assertions.assertEquals("https://w3id.org/semapv/vocab/ManualMappingCuration", m.getMappingJustification());
        Assertions.assertEquals("alice", m.getSubjectLabel());
        Assertions.assertEquals("alpha", m.getObjectLabel());
        Assertions.assertEquals(0.7, m.getConfidence());
        Assertions.assertEquals(EntityType.OWL_CLASS, m.getSubjectType());
        Assertions.assertEquals(MappingCardinality.ONE_TO_ONE, m.getMappingCardinality());
        Assertions.assertEquals(PredicateModifier.NOT, m.getPredicateModifier());
    }

    @Test
    void testReadDefinedExtensions() throws SSSOMFormatException, IOException {
        RDFReader reader = new RDFReader("src/test/resources/sets/exo2c-with-extensions.ttl");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        MappingSet ms = reader.read();

        Map<String, ExtensionDefinition> defs = new HashMap<String, ExtensionDefinition>();
        for ( ExtensionDefinition def : ms.getExtensionDefinitions() ) {
            defs.put(def.getSlotName(), def);
        }

        ExtensionDefinition def = defs.get("ext_foo");
        Assertions.assertNotNull(def);
        Assertions.assertEquals("https://example.org/properties/foo", def.getProperty());
        Assertions.assertEquals("http://www.w3.org/2001/XMLSchema#integer", def.getTypeHint());
        Assertions.assertEquals(ValueType.INTEGER, def.getEffectiveType());

        def = defs.get("ext_bar");
        Assertions.assertNotNull(def);
        Assertions.assertEquals("https://example.org/properties/bar", def.getProperty());
        Assertions.assertEquals("https://w3id.org/linkml/Uriorcurie", def.getTypeHint());
        Assertions.assertEquals(ValueType.IDENTIFIER, def.getEffectiveType());

        ExtensionValue ev = ms.getMappings().get(0).getExtensions().get("https://example.org/properties/foo");
        Assertions.assertNotNull(ev);
        Assertions.assertTrue(ev.isInteger());
        Assertions.assertEquals(11, ev.asInteger());

        ev = ms.getMappings().get(0).getExtensions().get("https://example.org/properties/bar");
        Assertions.assertNotNull(ev);
        Assertions.assertTrue(ev.isIdentifier());
        Assertions.assertEquals("https://example.com/entities/BAR_0001", ev.asString());
    }

    @Test
    void testReadUndefinedExtensions() throws SSSOMFormatException, IOException {
        RDFReader reader = new RDFReader("src/test/resources/sets/exo2c-with-extensions.ttl");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        Map<String, ExtensionDefinition> defs = new HashMap<String, ExtensionDefinition>();
        for ( ExtensionDefinition def : ms.getExtensionDefinitions() ) {
            defs.put(def.getSlotName(), def);
        }

        ExtensionDefinition def = defs.get("ext_foo");
        Assertions.assertNotNull(def);
        Assertions.assertEquals("https://example.org/properties/foo", def.getProperty());
        Assertions.assertEquals("http://www.w3.org/2001/XMLSchema#integer", def.getTypeHint());
        Assertions.assertEquals(ValueType.INTEGER, def.getEffectiveType());

        def = defs.get("ext_bar");
        Assertions.assertNotNull(def);
        Assertions.assertEquals("https://example.org/properties/bar", def.getProperty());
        Assertions.assertEquals("https://w3id.org/linkml/Uriorcurie", def.getTypeHint());
        Assertions.assertEquals(ValueType.IDENTIFIER, def.getEffectiveType());

        ExtensionValue ev = ms.getMappings().get(0).getExtensions().get("https://example.org/properties/foo");
        Assertions.assertNotNull(ev);
        Assertions.assertTrue(ev.isInteger());
        Assertions.assertEquals(11, ev.asInteger());

        ev = ms.getMappings().get(0).getExtensions().get("https://example.org/properties/bar");
        Assertions.assertNotNull(ev);
        Assertions.assertTrue(ev.isIdentifier());
        Assertions.assertEquals("https://example.com/entities/BAR_0001", ev.asString());

        ev = ms.getMappings().get(0).getExtensions().get("http://sssom.invalid/ext_baz");
        Assertions.assertNotNull(ev);
        Assertions.assertTrue(ev.isString());
        Assertions.assertEquals("Baz 0001", ev.asString());
    }
}
