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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONWriterTest {

    /*
     * Basic test of the JSON writer, using a made up minimal mapping set.
     */
    @Test
    void testSimpleWrite() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        assertWrittenAsExpected(ms, "exo2c-minimal", null, null);
    }

    /*
     * Likewise, but with shortened IRIs.
     */
    @Test
    void testWriteShortIRIs() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        assertWrittenAsExpected(ms, "test-short-iris", null, (w) -> w.setShortenIRIs(true));
    }

    /*
     * Likewise, but with the CURIE map written in a @context key (SSSOM-Py
     * compatibility).
     */
    @Test
    void testWriteLDContext() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();

        assertWrittenAsExpected(ms, "test-ld-context", null, (w) -> {
            w.setShortenIRIs(true);
            w.setWriteCurieMapInContext(true);
        });
    }

    /*
     * Test that we can serialise non-standard slots.
     */
    @Test
    void testWriteExtraSlots() throws IOException, SSSOMFormatException {
        TSVReader reader = new TSVReader("src/test/resources/sets/exo2c-with-extensions.sssom.tsv");
        reader.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED);
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "test-extensions-defined", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.DEFINED));
        assertWrittenAsExpected(ms, "test-extensions-undefined", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.UNDEFINED));
        assertWrittenAsExpected(ms, "test-extensions-none", null,
                (w) -> w.setExtraMetadataPolicy(ExtraMetadataPolicy.NONE));
    }

    @Test
    void testEscapingJSON() throws IOException, SSSOMFormatException {
        MappingSet ms = getTestSet();
        ms.setComment("A string\nwith\tvarious\rcharacters\\that\bshould\u0001be\fescaped");

        assertWrittenAsExpected(ms, "test-escaping-json", null, null);
    }

    @Test
    void testBasicRoundtrip() throws IOException, SSSOMFormatException {
        JSONReader reader = new JSONReader("src/test/resources/sets/exo2c.sssom.json");
        MappingSet ms = reader.read();

        assertWrittenAsExpected(ms, "exo2c", "test-basic-roundtrip", null);
    }

    @Test
    void testTSVAndJSONRoundtrips() throws IOException, SSSOMFormatException {
        TSVtoJSONtoTSVRoundtrip("exo2c", ExtraMetadataPolicy.NONE);
        TSVtoJSONtoTSVRoundtrip("test-extensions-defined", ExtraMetadataPolicy.DEFINED);
    }

    private void TSVtoJSONtoTSVRoundtrip(String tsvFilename, ExtraMetadataPolicy policy)
            throws IOException, SSSOMFormatException {
        File origTSV = new File("src/test/resources/sets/" + tsvFilename + ".sssom.tsv");
        if ( !origTSV.exists() ) {
            origTSV = new File("src/test/resources/output/" + tsvFilename + ".sssom.tsv");
        }

        TSVReader tsvReader = new TSVReader(origTSV);
        tsvReader.setExtraMetadataPolicy(policy);
        MappingSet ms = tsvReader.read();

        File json = new File("src/test/resources/output/" + tsvFilename + ".sssom.json.out");
        JSONWriter jsonWriter = new JSONWriter(json);
        jsonWriter.setShortenIRIs(true);
        jsonWriter.setExtraMetadataPolicy(policy);
        jsonWriter.write(ms);

        JSONReader jsonReader = new JSONReader(json);
        jsonReader.setExtraMetadataPolicy(policy);
        ms = jsonReader.read();

        File newTSV = new File("src/test/resources/output/" + tsvFilename + ".sssom.tsv.out");
        TSVWriter tsvWriter = new TSVWriter(newTSV);
        tsvWriter.setExtraMetadataPolicy(policy);
        tsvWriter.write(ms);

        boolean same = FileUtils.contentEquals(origTSV, newTSV);
        Assertions.assertTrue(same);
        if ( same ) {
            json.delete();
            newTSV.delete();
        }
    }

    /*
     * Checks that a mapping set is written exactly as we expect. This method will
     * write the provided set to a temporary file and compares the written file with
     * a theoretical file, and raises an assertion failure if the two files do not
     * have exactly the same contents.
     * 
     * The theoretical file should be either in src/test/resources/output/ or in
     * src/test/resources/sets/, with a .sssom.json extension.
     * 
     * The temporary file will be written in src/test/resources/output/, with a
     * .sssom.json.out extension. If actualBasename is null, the basename of the
     * theoretical (expected) file will be used. The temporary file will be
     * automatically deleted if it is found to be identical to the theoretical file.
     */
    private void assertWrittenAsExpected(MappingSet ms, String expectedBasename, String actualBasename,
            Consumer<JSONWriter> consumer) throws IOException {
        if ( actualBasename == null ) {
            actualBasename = expectedBasename;
        }

        File written = new File("src/test/resources/output/" + actualBasename + ".sssom.json.out");
        JSONWriter writer = new JSONWriter(written);
        if ( consumer != null ) {
            consumer.accept(writer);
        }
        writer.write(ms.toBuilder().build());

        File expected = new File("src/test/resources/output/" + expectedBasename + ".sssom.json");
        if ( !expected.exists() ) {
            expected = new File("src/test/resources/sets/" + expectedBasename + ".sssom.json");
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
                .predicateId("http://www.w3.org/2004/02/skos#closeMatch")
                .objectId("https://example.com/entities/0011")
                .objectLabel("alpha")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        ms.getMappings().add(Mapping.builder()
                .subjectId("https://example.org/entities/0002")
                .subjectLabel("bob")
                .predicateId("http://www.w3.org/2004/02/skos#closeMatch")
                .objectId("https://example.com/entities/0012")
                .objectLabel("beta")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .confidence(0.7)
                .build());
        // @formatter:on

        ms.getCurieMap().put("ORGENT", "https://example.org/entities/");
        ms.getCurieMap().put("COMENT", "https://example.com/entities/");
        ms.getCurieMap().put("ORGPID", "https://example.org/people/");
        ms.getCurieMap().put("COMPID", "https://example.com/people/");

        ms.getCreatorId().add("https://example.org/people/0000-0000-0001-1234");
        ms.getCreatorId().add("https://example.com/people/0000-0000-0002-5678");

        ms.setSubjectType(EntityType.OWL_CLASS);

        return ms;
    }
}
