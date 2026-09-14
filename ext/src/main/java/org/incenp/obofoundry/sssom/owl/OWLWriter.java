package org.incenp.obofoundry.sssom.owl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.incenp.obofoundry.sssom.ExtraMetadataPolicy;
import org.incenp.obofoundry.sssom.SSSOMWriter;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * A writer to serialise a SSSOM mapping set into a OWL format.
 */
public class OWLWriter extends SSSOMWriter {

    private OutputStream stream;
    private OWLDocumentFormat format;

    /**
     * Creates a new instance that will write data to the specified stream.
     * 
     * @param stream The stream to write the mapping set to.
     */
    public OWLWriter(OutputStream stream) {
        this(stream, new FunctionalSyntaxDocumentFormat());
    }

    /**
     * Creates a new instance that will write data to the specified stream in the
     * specified format.
     * 
     * @param stream The stream to write the mapping set to.
     * @param format The OWL format to write into. The default is OWL Functional
     *               Syntax.
     */
    public OWLWriter(OutputStream stream, OWLDocumentFormat format) {
        this.stream = stream;
        this.format = format;
        setSortingEnabled(false);
    }

    /**
     * Creates a new instance that will write data to the specified file.
     * 
     * @param file The file to write the mapping set to.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public OWLWriter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file), new FunctionalSyntaxDocumentFormat());
    }

    /**
     * Creates a new instance that will write data to the specified file in the
     * specified format.
     * 
     * @param file   The file to write the mapping set to.
     * @param format The OWL format to write into. The default is OWL Functional
     *               Syntax.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public OWLWriter(File file, OWLDocumentFormat format) throws FileNotFoundException {
        this(new FileOutputStream(file), format);
    }

    /**
     * Creates a new instance that will write data to a file with the specified
     * filename.
     * 
     * @param filename The name of the file to write the mapping set to.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public OWLWriter(String filename) throws FileNotFoundException {
        this(new File(filename), new FunctionalSyntaxDocumentFormat());
    }

    /**
     * Creates a new instance that will write data to a file with the specified
     * filename in the specified format.
     * 
     * @param filename The name of the file to write the mapping set to.
     * @param format   The OWL format to write into. The default is OWL Functional
     *                 Syntax.
     * @throws FileNotFoundException If the file exists but cannot be written to for
     *                               some reason.
     */
    public OWLWriter(String filename, OWLDocumentFormat format) throws FileNotFoundException {
        this(new File(filename), format);
    }

    @Override
    protected void doWrite(MappingSet mappingSet) throws IOException {
        try {
            // All the required logic is already available in the OWLHelper class. In fact,
            // this object is merely a wrapper to access that logic using the standard
            // SSSOMWriter interface.
            OWLOntology ontology = OWLHelper.exportToOWL(mappingSet, OWLManager.createOWLOntologyManager(),
                    extraPolicy != ExtraMetadataPolicy.NONE);
            ontology.saveOntology(format, stream);
        } catch ( OWLOntologyCreationException e ) {
            throw new IOException("Cannot create OWL ontology", e);
        } catch ( OWLOntologyStorageException e ) {
            throw new IOException("Cannot write OWL ontology", e);
        }
    }
}
