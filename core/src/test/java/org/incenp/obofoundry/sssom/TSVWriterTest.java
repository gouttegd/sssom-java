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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TSVWriterTest {

    /*
     * Basic test of the TSV writer. We make up a minimal mapping set, write it out
     * to disk, and check it comes out as expected.
     */
    @Test
    void testTSVWriter() throws IOException {
        // @formatter:off
        MappingSet ms = MappingSet.builder()
                .mappingSetTitle("Sample mapping set 2")
                .mappingSetVersion("1.0")
                .publicationDate(LocalDate.of(2023, 9, 13))
                .mappings(new ArrayList<Mapping>())
                .curieMap(new HashMap<String, String>())
                .build();
        ms.getMappings().add(Mapping.builder()
                .subjectId("http://purl.obolibrary.org/obo/FBbt_00000001")
                .predicateId("https://w3id.org/semapv/vocab/crossSpeciesExactMatch")
                .objectId("http://purl.obolibrary.org/obo/UBERON_0000468")
                .mappingJustification("https://w3id.org/semapv/vocab/ManualMappingCuration")
                .build());
        // @formatter:on

        ms.getCurieMap().put("FBbt", "http://purl.obolibrary.org/obo/FBbt_");
        ms.getCurieMap().put("UBERON", "http://purl.obolibrary.org/obo/UBERON_");

        File expected = new File("src/test/resources/sample2.sssom.tsv");
        File written = new File("src/test/resources/sample2.sssom.tsv.out");
        TSVWriter writer = new TSVWriter(written);
        writer.write(ms);

        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }

    /*
     * Basic round-trip test. We read a small SSSOM file (in "canonical" format,
     * with proper ordering), write it out, and check it comes out identical to the
     * original file.
     */
    @Test
    void testRoundtrip() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("sample1"));
    }

    /*
     * Same, but with a set where not all mappings have values in all columns. This
     * checks that the writer correctly determines the columns to write based on
     * which slots are used in the set.
     */
    @Test
    void testWriteMappingsWithMissingValues() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("missing-values"));
    }

    /*
     * Same, but with a set that contains (both at the set level and at the mapping
     * level) list-valued slots.
     */
    @Test
    void testWriteListValues() throws IOException, SSSOMFormatException {
        Assertions.assertTrue(roundtrip("list-values"));
    }

    /*
     * Check that superfluous prefixes are not written to the Curie map.
     */
    @Test
    void testStripUnusedPrefix() throws IOException, SSSOMFormatException {
        File source = new File("src/test/resources/sample1.sssom.tsv");
        TSVReader reader = new TSVReader(source);
        MappingSet ms = reader.read();

        // Add unused prefixes to the Curie map
        ms.getCurieMap().put("UNUSED1", "http://purl.obolibrary.org/obo/");
        ms.getCurieMap().put("UNUSED2", "https://example.org/");

        File target = new File("src/test/resources/unused-prefixes.sssom.tsv.out");
        TSVWriter writer = new TSVWriter(target);
        writer.write(ms);

        boolean same = FileUtils.contentEquals(source, target);
        Assertions.assertTrue(same);
        if ( same ) {
            target.delete();
        }
    }

    @Test
    void testCustomCurieMap() throws IOException, SSSOMFormatException {
        File source = new File("src/test/resources/sample1.sssom.tsv");
        TSVReader reader = new TSVReader(source);
        MappingSet ms = reader.read();

        // Build a custom map from the existing one...
        HashMap<String, String> customMap = new HashMap<String, String>();
        customMap.putAll(ms.getCurieMap());

        // then empty the existing map...
        ms.getCurieMap().clear();

        // and write the set with the custom map
        File target = new File("src/test/resources/custom-map.sssom.tsv.out");
        TSVWriter writer = new TSVWriter(target);
        writer.setCurieMap(customMap);
        writer.write(ms);

        boolean same = FileUtils.contentEquals(source, target);
        Assertions.assertTrue(same);
        if ( same ) {
            target.delete();
        }
    }

    /*
     * Read a mapping set, write it out, and compare.
     */
    private boolean roundtrip(String name) throws IOException, SSSOMFormatException {
        File source = new File(String.format("src/test/resources/%s.sssom.tsv", name));
        TSVReader reader = new TSVReader(source);
        MappingSet ms = reader.read();

        File target = new File(String.format("src/test/resources/%s.sssom.tsv.out", name));
        TSVWriter writer = new TSVWriter(target);
        writer.write(ms);

        boolean same = FileUtils.contentEquals(source, target);
        if ( same ) {
            target.delete();
        }

        return same;
    }
}
