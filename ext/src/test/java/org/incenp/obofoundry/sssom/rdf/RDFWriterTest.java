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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RDFWriterTest {

    /*
     * Basic test of the RDF Turtle writer.
     */
    @Test
    void testSimpleRDFWrite() {
        try {
            assertWrittenAsExpected(getTestSet(), "test-ttl-output", null, null);
        } catch ( IOException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testWriteSSSOM11Version() {
        MappingSet ms = getTestSet();
        ms.getMappings().get(0).setPredicateType(EntityType.OWL_ANNOTATION_PROPERTY);
        try {
            assertWrittenAsExpected(ms, "test-sssom11-version", null, null);
        } catch ( IOException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testWriteEnumValuesAsResources() {
        MappingSet ms = getTestSet();
        ms.getMappings().get(0).setSubjectType(EntityType.COMPOSED_ENTITY_EXPRESSION);
        ms.getMappings().get(1).setPredicateModifier(PredicateModifier.NOT);
        try {
            assertWrittenAsExpected(ms, "test-enum-values-written-as-resources", null, null);
        } catch ( IOException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testWriteRecordIDsAsResources() {
        MappingSet ms = getTestSet();
        ms.getCurieMap().put("ORGREC", "https://example.org/records/");
        for ( int i = 0; i < ms.getMappings().size(); i++ ) {
            ms.getMappings().get(i).setRecordId(String.format("https://example.org/records/%04d", i));
        }
        try {
            assertWrittenAsExpected(ms, "test-record-ids-as-resources", null, null);
        } catch ( IOException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testWriteShortIRIsWithEscapedCharacters() throws IOException {
        MappingSet ms = getTestSet();
        ms.getCreatorId().add("https://example.com/people/0000+0000?0002@5678");

        assertWrittenAsExpected(ms, "test-ttl-output-escaped-short-iris", null, null);
    }

    @Test
    void testWriteDefinedNonStandardMetadata() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-ttl-output-extensions-defined", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED));
    }

    @Test
    void testWriteUndefinedNonStandardMetadata() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-ttl-output-extensions-undefined", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED));
    }

    @Test
    void testWriteNoNonStandardMetadata() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-ttl-output-extensions-none", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.NONE));
    }

    @Test
    void testWriteURIExtensionValue() throws SSSOMFormatException, IOException {
        TSVReader reader = new TSVReader("../core/src/test/resources/sets/test-uri-extension-values.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-ttl-output-uri-extensions", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED));
    }

    @Test
    void testWriteDirectTriples() throws IOException {
        MappingSet ms = getTestSet();
        ms.getMappings()
                .add(Mapping.builder().subjectId("https://example.org/entities/0003")
                        .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch").objectLabel("Third entity")
                        .objectType(EntityType.RDFS_LITERAL)
                        .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration").build());
        ms.getMappings()
                .add(Mapping.builder().subjectId("https://example.org/entities/0004")
                        .predicateId("http://www.w3.org/2004/02/skos/core#exactMatch")
                        .objectId("https://w3id.org/sssom/NoTermFound")
                        .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration").build());
        ms.getMappings()
                .add(Mapping.builder().subjectId("https://example.org/entities/0005")
                        .predicateId("http://www.w3.org/2004/02/skos/core#exactMatch")
                        .objectId("https://example.com/entities/0005").predicateModifier(PredicateModifier.NOT)
                        .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration").build());

        assertWrittenAsExpected(ms, "test-ttl-output-with-direct-triples", null, (w) -> w.setInjectDirectTriples(true));
    }

    private void assertWrittenAsExpected(MappingSet ms, String expectedBasename, String actualBasename,
            Consumer<RDFWriter> consumer) throws IOException {
        if ( actualBasename == null ) {
            actualBasename = expectedBasename;
        }

        File written = new File("src/test/resources/output/" + actualBasename + ".ttl.out");
        RDFWriter writer = new RDFWriter(written);
        if ( consumer != null ) {
            consumer.accept(writer);
        }
        writer.write(ms.toBuilder().build());

        File expected = new File("src/test/resources/output/" + expectedBasename + ".ttl");
        if ( !expected.exists() ) {
            expected = new File("src/test/resources/sets/" + expectedBasename + ".ttl");
        }

        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }

    private MappingSet getTestSet() {
        // @formatter:off
        MappingSet ms = MappingSet.builder()
                .mappings(new ArrayList<Mapping>())
                .curieMap(new HashMap<String,String>())
                .mappingSetId("https://example.org/sets/exo2c")
                .license("https://creativecommons.org/licenses/by/4.0/")
                .creatorId(new ArrayList<String>())
                .build();
        ms.getMappings().add(Mapping.builder()
                .subjectId("https://example.org/entities/0001")
                .subjectLabel("alice")
                .subjectType(EntityType.OWL_CLASS)
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("https://example.com/entities/0011")
                .objectLabel("alpha")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        ms.getMappings().add(Mapping.builder()
                .subjectId("https://example.org/entities/0002")
                .subjectLabel("bob")
                .subjectType(EntityType.OWL_CLASS)
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("https://example.com/entities/0012")
                .objectLabel("beta")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .confidence(0.7)
                .build());
        ms.getMappings().add(Mapping.builder()
                .subjectId("http://purl.obolibrary.org/obo/FBbt_12345678")
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("http://purl.obolibrary.org/obo/UBERON_1234567")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        // @formatter:on

        ms.getCurieMap().put("ORGENT", "https://example.org/entities/");
        ms.getCurieMap().put("COMENT", "https://example.com/entities/");
        ms.getCurieMap().put("ORGPID", "https://example.org/people/");
        ms.getCurieMap().put("COMPID", "https://example.com/people/");
        ms.getCurieMap().put("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        ms.getCurieMap().put("UBERON", "http://purl.obolibrary.org/obo/UBERON_");

        ms.getCreatorId().add("https://example.org/people/0000-0000-0001-1234");
        ms.getCreatorId().add("https://example.com/people/0000-0000-0002-5678");

        return ms;
    }
}
