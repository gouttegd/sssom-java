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

package org.incenp.obofoundry.sssom.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obolibrary.robot.IOHelper;
import org.obolibrary.robot.OntologyHelper;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.semanticweb.owlapi.vocab.Namespaces;

/**
 * An axiom dispatcher. This class allows to dispatch axioms to different
 * ontologies.
 * <p>
 * The table is filled with entries corresponding to an output ontology and
 * associated with a label. Axioms are then added with the
 * {@link #addAxiom(String, OWLAxiom)} method. Once all axioms have been added,
 * call {@link #saveAll(IOHelper)} to write them to the destination ontologies.
 * Only the destination ontologies to which some axioms have been added will be
 * actually written to disk.
 */
public class AxiomDispatchTable {
    private static Map<String, IRI> ANNOTATIONS;

    static {
        ANNOTATIONS = new HashMap<String, IRI>();
        ANNOTATIONS.put("dc-title", IRI.create(Namespaces.DCTERMS.toString(), "title"));
        ANNOTATIONS.put("dc-description", IRI.create(Namespaces.DCTERMS.toString(), "description"));
        ANNOTATIONS.put("dc-creator", IRI.create(Namespaces.DCTERMS.toString(), "creator"));
        ANNOTATIONS.put("dc-contributor", IRI.create(Namespaces.DCTERMS.toString(), "contributor"));
    }

    private Map<String, DispatchTableEntry> table = new HashMap<String, DispatchTableEntry>();
    private OWLOntologyManager manager;

    /**
     * Creates a new instance.
     * 
     * @param manager The OWL ontology manager used to create the destination
     *                ontologies.
     */
    public AxiomDispatchTable(OWLOntologyManager manager) {
        this.manager = manager;
    }

    /**
     * Adds an axiom to one of the destination ontologies.
     * 
     * @param target The label identifying the destination ontologies. If the table
     *               does not contain any entry with that label, the axiom is
     *               ignored.
     * @param axiom  The axiom to add.
     */
    public void addAxiom(String target, OWLAxiom axiom) {
        DispatchTableEntry entry = table.get(target);
        if ( entry != null ) {
            entry.axioms.add(axiom);
        }
    }

    /**
     * Adds a new entry to the dispatch table.
     * 
     * @param label    The label of the new entry.
     * @param filename The filename where the destination ontology is to be written.
     */
    public void addEntry(String label, String filename) {
        DispatchTableEntry entry = new DispatchTableEntry();
        entry.filename = filename;
        table.put(label, entry);
    }

    /**
     * Adds a new entry to the dispatch table.
     * 
     * @param label      The label of the new entry.
     * @param filename   The filename where the destination ontology is to be
     *                   written.
     * @param ontologyID The ontology IRI for the destination ontology.
     */
    public void addEntry(String label, String filename, String ontologyID) {
        DispatchTableEntry entry = new DispatchTableEntry();
        entry.filename = filename;
        entry.ontologyID = ontologyID;
        table.put(label, entry);
    }

    /**
     * Adds an ontology annotation to an entry.
     * 
     * @param label      The label of an existing entry to the table. The annotation
     *                   will be ignored if there is no entry with that label.
     * @param annotation An annotation to add to the destination ontology when it
     *                   will be written to disk.
     */
    public void addEntryAnnotation(String label, OWLAnnotation annotation) {
        DispatchTableEntry entry = table.get(label);
        if ( entry != null ) {
            entry.annotations.add(annotation);
        }
    }

    /*
     * Adds a pre-built entry to the table.
     */
    private void addEntry(String label, DispatchTableEntry entry) {
        if ( entry.filename != null || label.equals("__default") ) {
            table.put(label, entry);
        }
    }

    /**
     * Writes all the destination ontologies that received some axioms.
     * 
     * @param ioHelper A ROBOT I/O helper object that will be used to save the
     *                 ontologies.
     * @throws IOException                  If a I/O error occurs when trying to
     *                                      write the ontologies.
     * @throws OWLOntologyCreationException If the OWL ontology manager cannot
     *                                      create one of the destination
     *                                      ontologies.
     */
    public void saveAll(IOHelper ioHelper) throws IOException, OWLOntologyCreationException {
        for ( DispatchTableEntry entry : table.values() ) {
            if ( entry.axioms.size() > 0 && entry.filename != null ) {
                OWLOntology o = manager.createOntology();
                OntologyHelper.setOntologyIRI(o, getOntologyID(entry), getOntologyVersion(entry));
                for ( OWLAnnotation annotation : entry.annotations ) {
                    manager.applyChange(new AddOntologyAnnotation(o, annotation));
                }
                manager.addAxioms(o, entry.axioms);
                if ( entry.additionalAxioms.size() > 0 ) {
                    manager.addAxioms(o, entry.additionalAxioms);
                }
                ioHelper.saveOntology(o, entry.filename);
            }
        }
    }

    /*
     * Get the ontology ID for a given entry, with fallback to the default entry if
     * available.
     */
    private String getOntologyID(DispatchTableEntry entry) {
        if ( entry.ontologyID != null ) {
            return entry.ontologyID;
        }

        DispatchTableEntry defEntry = table.get("__default");
        if ( defEntry == null || defEntry.ontologyID == null ) {
            return null;
        }

        String basename = new File(entry.filename).getName();
        int lastDot = basename.lastIndexOf('.');
        if ( lastDot != -1 ) {
            basename = basename.substring(0, lastDot);
        }

        return defEntry.ontologyID.replace("%filename", basename);
    }

    /*
     * Get the ontology version IRI for a given entry, with fallback to the default
     * entry if available.
     */
    private String getOntologyVersion(DispatchTableEntry entry) {
        if ( entry.ontologyVersion != null ) {
            return entry.ontologyVersion;
        }

        DispatchTableEntry defEntry = table.get("__default");
        if ( defEntry == null || defEntry.ontologyVersion == null ) {
            return null;
        }

        String basename = new File(entry.filename).getName();
        int lastDot = basename.lastIndexOf('.');
        if ( lastDot != -1 ) {
            basename = basename.substring(0, lastDot);
        }

        return defEntry.ontologyVersion.replace("%filename", basename);
    }

    /**
     * Creates a dispatch table from a file.
     * <p>
     * The file must be in a format similar to INI, where each section represents a
     * table entry. The section name (between '[' and ']') is the label for the
     * entry. Each section should contain fields of the form {@code name: value}.
     * Possible fields are:
     * <ul>
     * <li>{@code file}: The filename where the ontology should be written; that
     * field is mandatory, the entry will be ignored if it is absent.
     * <li>{@code ontology-iri}: The ontology IRI for the ontology (will be
     * auto-generated if absent).
     * <li>{@code ontology-version}: The version IRI for the ontology; within the
     * value, the string {@code %date} will be replaced by the current date in the
     * YYYY-MM-DD format.
     * <li>{@code add-axiom:} An arbitrary axiom to add to the ontology, in
     * Manchester syntax. Be careful that any entity referenced in such an axiom
     * MUST have been properly declared in the ontology given as argument to this
     * function.
     * </ul>
     * <p>
     * In addition, the entry may also contain fields named {@code dc-title},
     * {@code dc-description}, {@code dc-creator}, and {@code dc-contributor}. They
     * will become ontology annotations.
     * 
     * @param filename      The name of the file to read the table from.
     * @param manager       An OWL ontology manager to create the target ontologies.
     * @param entityChecker An entity checker that will be used to resolve
     *                      identifiers in {@code add-axiom:} expressions.
     * @return A dispatch table built according to the sections found in the file.
     * @throws IOException If any I/O error occurs when reading from the file.
     */
    public static AxiomDispatchTable readFromFile(String filename, OWLOntologyManager manager,
            OWLEntityChecker entityChecker) throws IOException {

        AxiomDispatchTable table = new AxiomDispatchTable(manager);
        OWLDataFactory factory = manager.getOWLDataFactory();

        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        ManchesterOWLSyntaxParser manParser = null;
        if ( entityChecker != null ) {
            OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
            manParser = new ManchesterOWLSyntaxParserImpl(() -> config, factory);
            manParser.setOWLEntityChecker(entityChecker);
        }

        File parentDir = new File(filename).getParentFile();
        BufferedReader r = new BufferedReader(new FileReader(filename));
        String line;
        String label = null;
        DispatchTableEntry entry = null;
        while ( (line = r.readLine()) != null ) {
            if ( line.length() == 0 || line.charAt(0) == '#' ) {
                continue;
            }

            if ( line.charAt(0) == '[' ) {
                if ( entry != null ) {
                    table.addEntry(label, entry);
                }
                label = line.substring(1, line.length() - 1);
                entry = table.new DispatchTableEntry();
            } else {
                String[] parts = line.split(": ", 2);
                if ( parts.length != 2 ) {
                    continue;
                }

                if ( ANNOTATIONS.containsKey(parts[0]) ) {
                    entry.annotations
                            .add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(ANNOTATIONS.get(parts[0])),
                                    factory.getOWLLiteral(parts[1])));
                } else if ( parts[0].equals("file") ) {
                    entry.filename = getEntryFilename(parentDir, parts[1]);
                } else if ( parts[0].equals("ontology-iri") ) {
                    entry.ontologyID = parts[1];
                } else if ( parts[0].equals("ontology-version") ) {
                    entry.ontologyVersion = parts[1].replace("%date", dateFormatter.format(today));
                } else if ( parts[0].equals("add-axiom") && manParser != null ) {
                    manParser.setStringToParse(parts[1]);
                    entry.additionalAxioms.add(manParser.parseAxiom());
                }
            }
        }

        if ( entry != null ) {
            table.addEntry(label, entry);
        }

        r.close();

        return table;
    }

    /*
     * Make the filename of an entry relative to the directory containing the table
     * itself.
     */
    private static String getEntryFilename(File parent, String entryName) {
        File entry = new File(entryName);
        if ( entry.isAbsolute() || parent == null ) {
            return entryName;
        } else {
            return new File(parent, entryName).getPath();
        }
    }

    /*
     * TODO: Transform into a public class with a proper interface? Or keep as an
     * internal implementation detail?
     */
    private class DispatchTableEntry {
        String filename;
        String ontologyID;
        String ontologyVersion;
        Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        Set<OWLAxiom> additionalAxioms = new HashSet<OWLAxiom>();
    }
}
