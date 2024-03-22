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

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TSVReaderTest {

    /*
     * Basic test of the TSV reader. We check that we can read a small SSSOM file
     * and get all the slots correctly.
     */
    @Test
    void testRead() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertNotNull(ms);

        Assertions.assertNotNull(ms.getCreatorId());
        Assertions.assertEquals(2, ms.getCreatorId().size());
        Assertions.assertEquals("https://example.org/people/0000-0000-0001-1234", ms.getCreatorId().get(0));
        Assertions.assertEquals("https://example.com/people/0000-0000-0002-5678", ms.getCreatorId().get(1));
        Assertions.assertEquals("O2C set", ms.getMappingSetTitle());
        Assertions.assertEquals(LocalDate.of(2023, 9, 13), ms.getPublicationDate());

        List<Mapping> mappings = ms.getMappings();
        Assertions.assertNotNull(mappings);
        Assertions.assertEquals(8, mappings.size());

        Mapping mapping = mappings.get(0);
        Assertions.assertEquals("https://example.org/entities/0001", mapping.getSubjectId());
        Assertions.assertEquals("alice", mapping.getSubjectLabel());
        Assertions.assertEquals("http://www.w3.org/2004/02/skos/core#closeMatch", mapping.getPredicateId());
        Assertions.assertEquals("https://example.com/entities/0011", mapping.getObjectId());
        Assertions.assertEquals("https://w3id.org/semapv/vocab/ManualMappingCuration",
                mapping.getMappingJustification());
    }

    @Test
    void testReadingNonSSSOM() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("pom.xml");
        Assertions.assertThrows(SSSOMFormatException.class, () -> reader.read());
    }

    /*
     * Check that we can read a file where not all mappings have values in all
     * columns. Missing values should result in the corresponding slots being set to
     * null.
     */
    @Test
    void testMissingValues() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-values.sssom.tsv");
        List<Mapping> mappings = reader.read().getMappings();

        Assertions.assertNull(mappings.get(0).getSubjectLabel());
        Assertions.assertNull(mappings.get(0).getConfidence());
        Assertions.assertNull(mappings.get(0).getPredicateModifier());

        Assertions.assertNull(mappings.get(1).getSubjectLabel());
        Assertions.assertNotNull(mappings.get(1).getConfidence());
        Assertions.assertNull(mappings.get(1).getPredicateModifier());

        Assertions.assertNotNull(mappings.get(2).getSubjectLabel());
        Assertions.assertNull(mappings.get(2).getConfidence());
        Assertions.assertNotNull(mappings.get(2).getPredicateModifier());
    }

    /*
     * Check that we can read a file that uses multi-valued slots.
     */
    @Test
    void testListValues() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-mapping-list-values.sssom.tsv");
        MappingSet ms = reader.read();
        Mapping m = ms.getMappings().get(0);

        Assertions.assertEquals(2, m.getAuthorId().size());
        Assertions.assertEquals("https://example.org/people/0000-0000-0001-1234", m.getAuthorId().get(0));
        Assertions.assertEquals("https://example.com/people/0000-0000-0002-5678", m.getAuthorId().get(1));

        Assertions.assertEquals(1, m.getSeeAlso().size());
        Assertions.assertEquals("https://example.org/misc/seeAlso1", m.getSeeAlso().get(0));
    }

    /*
     * Check that we can read a file that misuses multi-valued slots for
     * single-valued slots.
     * 
     */
    @Test
    void testPseudoListValues() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-pseudo-list-values.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertEquals(1, ms.getCreatorId().size());
        Assertions.assertEquals("https://example.org/people/0000-0000-0001-1234", ms.getCreatorId().get(0));
    }

    /*
     * Check that we can find an external metadata file that has the same basename
     * as the TSV file.
     */
    @Test
    void testReadExternalMetadataAuto() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-external-metadata.sssom.tsv");
        MappingSet ms = reader.read();
        Assertions.assertEquals("https://example.org/sets/test-external-metadata", ms.getMappingSetId());
    }

    /*
     * Check that we can read external metadata from an explicitly specified
     * filename, ignoring if needed any file that has the same basename as the TSV
     * file.
     */
    @Test
    void testReadExternalMetadataExplicit() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-external-metadata.sssom.tsv",
                "src/test/resources/sets/test-explicit-external-metadata.sssom.yml");
        MappingSet ms = reader.read();
        Assertions.assertEquals("https://example.org/sets/test-explicit-external-metadata", ms.getMappingSetId());
    }

    /*
     * Check that we fail if we can't find the metadata.
     */
    @Test
    void testFailIfNoMetadata() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-missing-metadata.sssom.tsv");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for missing metadata");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("External metadata file not found", sfe.getMessage());
        }
    }

    /*
     * If an external metadata file is specified, the TSV file should not contain an
     * embedded metadata block.
     */
    @Test
    void testFailOnEmbeddedMetadataIfExternalFileSpecified() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c.sssom.tsv",
                "src/test/resources/sets/test-external-metadata.sssom.yml");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for spurious embedded metadata block");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("Error when parsing TSV table", sfe.getMessage());
        }
    }

    /*
     * Check that we can read the metadata only.
     */
    @Test
    void testReadingMetadataOnly() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c.sssom.tsv");
        MappingSet ms = reader.read(true);
        Assertions.assertEquals("https://example.org/sets/exo2c", ms.getMappingSetId());
        Assertions.assertEquals(0, ms.getMappings().size());

        reader = new TSVReader(null, "src/test/resources/sets/test-external-metadata.sssom.yml");
        ms = reader.read(false);
        Assertions.assertEquals("https://example.org/sets/test-external-metadata", ms.getMappingSetId());
        Assertions.assertEquals(0, ms.getMappings().size());
    }

    /*
     * Check that we can read from a stream rather than from a file.
     */
    @Test
    void testReadingFromStream() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader(new FileInputStream("src/test/resources/sets/exo2c.sssom.tsv"));
        MappingSet ms = reader.read();
        Assertions.assertEquals("https://example.org/sets/exo2c", ms.getMappingSetId());
        Assertions.assertEquals(8, ms.getMappings().size());

        reader = new TSVReader(new FileInputStream("src/test/resources/sets/test-external-metadata.sssom.tsv"));
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for missing metadata when reading from stream");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("No embedded metadata and external metadata not specified", sfe.getMessage());
        }
    }

    /*
     * Any undeclared prefix should result in an exception.
     */
    @Test
    void testFailOnUndeclaredPrefixes() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-undeclared-prefixes.sssom.tsv");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for undeclared prefixes");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("Some prefixes are undeclared: ORGPID", sfe.getMessage());
        }
    }

    /*
     * Any redefinition of a built-in prefix should result in an exception.
     */
    @Test
    void testFailOnRedefinedBuiltinPrefix() throws IOException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-redefined-builtin-prefix.sssom.tsv");
        try {
            reader.read();
            Assertions.fail("SSSOMFormatException not thrown for a redefined built-in prefix");
        } catch ( SSSOMFormatException sfe ) {
            Assertions.assertEquals("Re-defined builtin prefix in the provided curie map", sfe.getMessage());
        }
    }

    /*
     * "Propagatable" slots in the set metadata should be propagated to the
     * individual mappings.
     */
    @Test
    void testSlotPropagation() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-propagatable-slots.sssom.tsv");
        MappingSet ms = reader.read();

        for ( Mapping m : ms.getMappings() ) {
            // Set-level mapping provider should be propagated to all mappings
            Assertions.assertEquals("https://example.org/provider", m.getMappingProvider());

            // Set-level mapping tool should NOT be propagated down to mappings, because the
            // set has a mapping_tool column
            Assertions.assertNotEquals("foo mapper", m.getMappingTool());
        }

        // Second mapping has its own mapping tool value
        Assertions.assertEquals("bar mapper", ms.getMappings().get(1).getMappingTool());
    }

    /*
     * Obsolete fields should be translated to their standard equivalents.
     */
    @Test
    void testObsoleteFields() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-obsolete-fields.sssom.tsv");
        MappingSet ms = reader.read();

        String[] expectedJustifications = { "LexicalMatching", "LogicalMatching", "ManualMappingCuration",
                "CompositeMatching", "ManualMappingCuration", "SemanticSimilarityThresholdMatching",
                "UnspecifiedMatching", null };
        EntityType[] expectedEntityTypes = { EntityType.SKOS_CONCEPT, EntityType.OWL_CLASS,
                EntityType.OWL_OBJECT_PROPERTY, EntityType.OWL_NAMED_INDIVIDUAL, EntityType.OWL_DATA_PROPERTY,
                EntityType.RDFS_LITERAL, null, null };

        for ( int i = 0, len = ms.getMappings().size(); i < len; i++ ) {
            Mapping m = ms.getMappings().get(i);

            if ( expectedJustifications[i] != null ) {
                Assertions.assertEquals("https://w3id.org/semapv/vocab/" + expectedJustifications[i],
                        m.getMappingJustification());
            } else {
                Assertions.assertNull(m.getMappingJustification());
            }

            Assertions.assertEquals(expectedEntityTypes[i], m.getSubjectType());
            Assertions.assertEquals(expectedEntityTypes[i], m.getObjectType());
        }
    }

    /*
     * Extra slots should be completely ignored when ExtraMetadataPolicy is set to
     * NONE.
     */
    @Test
    void testIgnoreExtensions() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.NONE);
        MappingSet ms = reader.read();

        Assertions.assertNull(ms.getExtensionDefinitions());

        Assertions.assertNull(ms.getExtensions());

        Assertions.assertNull(ms.getMappings().get(0).getExtensions());
    }

    /*
     * Only the extra slots that have been properly defined should be accepted when
     * ExtraMetadataPolicy is set to DEFINED.
     */
    @Test
    void testAcceptDefinedExtensions() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        MappingSet ms = reader.read();


        // Check the parsed definitions
        Assertions.assertNotNull(ms.getExtensionDefinitions());
        Map<String, ExtensionDefinition> definitions = new HashMap<String, ExtensionDefinition>();
        for ( ExtensionDefinition definition : ms.getExtensionDefinitions() ) {
            definitions.put(definition.getSlotName(), definition);
        }
        Assertions.assertEquals(3, definitions.size());
        compare(new ExtensionDefinition("ext_bar", "https://example.org/properties/barProperty",
                "http://www.w3.org/2001/XMLSchema#integer"), definitions.get("ext_bar"));
        compare(new ExtensionDefinition("ext_baz", "https://example.org/properties/bazProperty",
                "https://w3id.org/linkml/uriOrCurie"), definitions.get("ext_baz"));
        compare(new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"),
                definitions.get("ext_foo"));

        // Check the set-level extensions
        Assertions.assertNotNull(ms.getExtensions());
        Assertions.assertEquals(1, ms.getExtensions().size());
        compare(new ExtensionValue("Foo A", false),
                ms.getExtensions().get("https://example.org/properties/fooProperty"));

        // Check the mapping-level extensions
        Mapping m1 = ms.getMappings().get(0);
        Assertions.assertNotNull(m1.getExtensions());
        Assertions.assertEquals(2, m1.getExtensions().size());
        compare(new ExtensionValue(111), m1.getExtensions().get("https://example.org/properties/barProperty"));
        compare(new ExtensionValue("https://example.org/entities/BAZ_0001", true),
                m1.getExtensions().get("https://example.org/properties/bazProperty"));
    }

    /*
     * All non-standard metadata should end up in the extension slots, regardless of
     * whether they have been defined, when ExtraMetadataPolicy is set to UNDEFINED.
     */
    @Test
    void testAcceptAllExtensions() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        // Checked the parsed *and* auto-generated definitions
        Assertions.assertNotNull(ms.getExtensionDefinitions());
        Map<String, ExtensionDefinition> definitions = new HashMap<String, ExtensionDefinition>();
        for ( ExtensionDefinition definition : ms.getExtensionDefinitions() ) {
            definitions.put(definition.getSlotName(), definition);
        }
        Assertions.assertEquals(5, definitions.size());
        compare(new ExtensionDefinition("ext_bar", "https://example.org/properties/barProperty",
                "http://www.w3.org/2001/XMLSchema#integer"), definitions.get("ext_bar"));
        compare(new ExtensionDefinition("ext_baz", "https://example.org/properties/bazProperty",
                "https://w3id.org/linkml/uriOrCurie"), definitions.get("ext_baz"));
        compare(new ExtensionDefinition("ext_foo", "https://example.org/properties/fooProperty"),
                definitions.get("ext_foo"));
        compare(new ExtensionDefinition("ext_undeclared_baz", "http://sssom.invalid/ext_undeclared_baz"),
                definitions.get("ext_undeclared_baz"));
        compare(new ExtensionDefinition("ext_undeclared_foo", "http://sssom.invalid/ext_undeclared_foo"),
                definitions.get("ext_undeclared_foo"));


        // Check the set-level extensions
        Assertions.assertNotNull(ms.getExtensions());
        Assertions.assertEquals(2, ms.getExtensions().size());
        compare(new ExtensionValue("Foo A", false),
                ms.getExtensions().get("https://example.org/properties/fooProperty"));
        compare(new ExtensionValue("Foo B", false), ms.getExtensions().get("http://sssom.invalid/ext_undeclared_foo"));

        // Check the mapping-level extensions
        Mapping m1 = ms.getMappings().get(0);
        Assertions.assertNotNull(m1.getExtensions());
        Assertions.assertEquals(3, m1.getExtensions().size());
        compare(new ExtensionValue(111), m1.getExtensions().get("https://example.org/properties/barProperty"));
        compare(new ExtensionValue("https://example.org/entities/BAZ_0001", true),
                m1.getExtensions().get("https://example.org/properties/bazProperty"));
        compare(new ExtensionValue("BAZ A", false), m1.getExtensions().get("http://sssom.invalid/ext_undeclared_baz"));
    }

    /*
     * Test that the parser can handle escaped YAML strings.
     */
    @Test
    void testEscapedYAML() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-escaping-yaml.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertEquals("O2C set\u0009with\u00A0non-printable\u0080characters", ms.getMappingSetTitle());
    }

    /*
     * Test that the parser can handle escaped TSV values.
     */
    @Test
    void testEscapedTSV() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-escaping-tsv.sssom.tsv");
        MappingSet ms = reader.read();
        Mapping m = ms.getMappings().get(0);

        Assertions.assertEquals("Value\u0009with\u0009tab\u0009characters", m.getComment());
        Assertions.assertEquals("Value with \"quote\" characters", m.getObjectLabel());
        Assertions.assertEquals("Value with\nnew line character", m.getIssueTrackerItem());
    }

    /*
     * Test that empty lines in the TSV section are ignored.
     */
    @Test
    void testSkippingEmptyLines() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/test-empty-lines.sssom.tsv");
        MappingSet ms = reader.read();

        Assertions.assertEquals(3, ms.getMappings().size());
        Assertions.assertEquals("alice", ms.getMappings().get(0).getSubjectLabel());
        Assertions.assertEquals("bob", ms.getMappings().get(1).getSubjectLabel());
        Assertions.assertEquals("daphne", ms.getMappings().get(2).getSubjectLabel());
    }

    private void compare(ExtensionDefinition expected, ExtensionDefinition actual) {
        Assertions.assertEquals(expected.getSlotName(), actual.getSlotName());
        Assertions.assertEquals(expected.getProperty(), actual.getProperty());
        Assertions.assertEquals(expected.getTypeHint(), actual.getTypeHint());
        Assertions.assertEquals(expected.getEffectiveType(), actual.getEffectiveType());
    }

    private void compare(ExtensionValue expected, ExtensionValue actual) {
        Assertions.assertEquals(expected.getType(), actual.getType());
        Assertions.assertEquals(expected.getValue(), actual.getValue());
    }
}
