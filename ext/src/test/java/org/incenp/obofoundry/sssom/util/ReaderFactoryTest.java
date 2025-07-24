/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024,2025 Damien Goutte-Gattat
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.incenp.obofoundry.sssom.JSONReader;
import org.incenp.obofoundry.sssom.SSSOMFormatException;
import org.incenp.obofoundry.sssom.SSSOMReader;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.rdf.RDFReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReaderFactoryTest {

    private final static String sampleTSVFile = "../core/src/test/resources/sets/exo2c.sssom.tsv";
    private final static String sampleCSVFile = "../core/src/test/resources/sets/exo2c.sssom.csv";
    private final static String sampleJSONFile = "../core/src/test/resources/sets/exo2c.sssom.json";
    private final static String sampleTTFFile = "src/test/resources/sets/exo2c.ttl";

    @Test
    void testInferFormat() throws IOException {
        Reader reader;
        ReaderFactory factory = new ReaderFactory();

        reader = new BufferedReader(new FileReader(new File(sampleTSVFile)));
        Assertions.assertEquals(SerialisationFormat.TSV, factory.inferFormat(reader));
        reader.close();

        reader = new BufferedReader(new FileReader(new File(sampleCSVFile)));
        // Without the filename, this is inferred as a TSV file, not a CSV file.
        Assertions.assertEquals(SerialisationFormat.TSV, factory.inferFormat(reader));
        reader.close();

        reader = new BufferedReader(new FileReader(new File(sampleJSONFile)));
        Assertions.assertEquals(SerialisationFormat.JSON, factory.inferFormat(reader));
        reader.close();

        reader = new BufferedReader(new FileReader(new File(sampleTTFFile)));
        Assertions.assertEquals(SerialisationFormat.RDF_TURTLE, factory.inferFormat(reader));
        reader.close();
    }

    @Test
    void testGetReaderFromFile() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory();
        SSSOMReader reader = factory.getReader(new File(sampleTSVFile));
        Assertions.assertInstanceOf(TSVReader.class, reader);
    }

    @Test
    void testGetReaderFromStream() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory();
        SSSOMReader reader = factory.getReader(new FileInputStream(sampleJSONFile));
        Assertions.assertInstanceOf(JSONReader.class, reader);
    }

    @Test
    void testGetReaderFromFilename() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory();
        SSSOMReader reader = factory.getReader(sampleTTFFile);
        Assertions.assertInstanceOf(RDFReader.class, reader);
    }

    @Test
    void testGetReaderFromFilenameWithExternalMetdata() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory();
        SSSOMReader reader = factory.getReader(
                "../core/src/test/resources/sets/test-external-metadata.sssom.tsv",
                "../core/src/test/resources/sets/test-explicit-external-metadata.sssom.yml", true);
        Assertions.assertInstanceOf(TSVReader.class, reader);
        MappingSet ms = reader.read();
        Assertions.assertEquals("https://example.org/sets/test-explicit-external-metadata", ms.getMappingSetId());
    }

    @Test
    void testGetReaderFromFilenameNoExternalMetadata() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory();
        SSSOMReader reader = factory.getReader(sampleJSONFile, null, false);
        Assertions.assertInstanceOf(JSONReader.class, reader);
    }

    @Test
    void testGetReaderFromExtension() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory(true);
        SSSOMReader reader = factory.getReader(sampleTSVFile);
        Assertions.assertInstanceOf(TSVReader.class, reader);
    }

    @Test
    void testGetReaderFromExtensionFallbackToPeeking() throws IOException, SSSOMFormatException {
        Reader reader = new BufferedReader(new FileReader(new File(sampleTSVFile)));
        ReaderFactory factory = new ReaderFactory(true);
        SSSOMReader sssomReader = factory.getReader(reader, "anything.txt");
        Assertions.assertInstanceOf(TSVReader.class, sssomReader);
        reader.close();
    }

    @Test
    void testGetReaderFromFileWithExplicitFormat() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory(true);
        SSSOMReader reader = factory.getReader(sampleJSONFile, true, SerialisationFormat.TSV);
        Assertions.assertInstanceOf(TSVReader.class, reader);

        reader = factory.getReader(sampleJSONFile, true);
        Assertions.assertInstanceOf(JSONReader.class, reader);
    }

    @Test
    void testGetReaderWithExplicitFormat() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory();
        Reader input = new BufferedReader(new FileReader(new File(sampleTSVFile)));
        SSSOMReader reader;

        reader = factory.getReader(input, SerialisationFormat.TSV);
        Assertions.assertInstanceOf(TSVReader.class, reader);

        reader = factory.getReader(input, SerialisationFormat.CSV);
        Assertions.assertInstanceOf(TSVReader.class, reader);

        reader = factory.getReader(input, SerialisationFormat.JSON);
        Assertions.assertInstanceOf(JSONReader.class, reader);

        reader = factory.getReader(input, SerialisationFormat.RDF_TURTLE);
        Assertions.assertInstanceOf(RDFReader.class, reader);

        try {
            reader = factory.getReader(input, (SerialisationFormat) null);
            Assertions.fail("Exception not thrown when serialisation format is not specified");
        } catch ( IOException ioe ) {
            Assertions.assertEquals("Expected serialisation format must be specified", ioe.getMessage());
        }

        input.close();
    }

    @Test
    void testReadingFromTwoFilesImpliesTSV() throws IOException, SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory(true);
        SSSOMReader reader = factory.getReader(sampleTTFFile, sampleTSVFile);
        Assertions.assertInstanceOf(TSVReader.class, reader);

        reader = factory.getReader(sampleTTFFile, null);
        Assertions.assertInstanceOf(RDFReader.class, reader);

        try {
            reader = factory.getReader(sampleTSVFile, sampleTSVFile, false, SerialisationFormat.JSON);
            Assertions.fail("Exception not thrown on trying to read non-TSV/non-CSV from two files");
        } catch ( IOException ioe ) {
            Assertions.assertEquals("Cannot read metadata from a separate file if the format is not TSV or CSV",
                    ioe.getMessage());
        }

        reader = factory.getReader(sampleTTFFile, sampleTSVFile, false, null);
        Assertions.assertInstanceOf(TSVReader.class, reader);
    }

    @Test
    void testCannotReadTwoFilesFromStdin() throws SSSOMFormatException {
        ReaderFactory factory = new ReaderFactory(true);
        try {
            factory.getReader("-", "-", true, SerialisationFormat.CSV);
            Assertions.fail("Exception not thrown on trying to read two files from stdin");
        } catch ( IOException ioe ) {
            Assertions.assertEquals("Cannot read both TSV section and metadata from standard input", ioe.getMessage());
        }
    }
}
