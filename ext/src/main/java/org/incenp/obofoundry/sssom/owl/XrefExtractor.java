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

package org.incenp.obofoundry.sssom.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Extract mappings from cross-references (oboInOwl:hasDbXref annotations) in a
 * ontology.
 */
public class XrefExtractor {

    private static final String OBO_IN_OWL = "http://www.geneontology.org/formats/oboInOwl#";

    private static final String HAS_DB_XREF = OBO_IN_OWL + "hasDbXref";

    private static final String XREF_AS_EQUIVALENT = OBO_IN_OWL + "treat-xrefs-as-equivalent";

    private static final String XREF_AS_IS_A = OBO_IN_OWL + "treat-xrefs-as-is_a";

    private static final String XREF_AS_HAS_SUBCLASS = OBO_IN_OWL + "treat-xrefs-as-has-subclass";

    private static final String XREF_AS_GENUS_DIFFERENTIA = OBO_IN_OWL + "treat-xrefs-as-genus-differentia";

    private static final String XREF_AS_REVERSE_GENUS_DIFFERENTIA = OBO_IN_OWL
            + "treat-xrefs-as-reverse-genus-differentia";

    private static final String SEMAPV = "https://w3id.org/semapv/vocab/";

    private HashMap<String, String> prefixToPredicateMap = new HashMap<>();
    private PrefixManager prefixManager = new PrefixManager();
    private OWLLiteral falseValue = null;
    private boolean ignoreObsoleteTerms = true;

    /**
     * Sets the prefix map to be used when processing cross-reference annotations.
     * By default, only cross-references using a known prefix name will be derived
     * into mappings.
     * 
     * @param map A map of prefix names to URL prefixes.
     */
    public void setPrefixMap(Map<String, String> map) {
        prefixManager.add(map);
    }

    /**
     * Maps a prefix name to a predicate. By default, mappings derived from
     * cross-references have a {@code oboInOwl:hasDbXref} predicate. Use this method
     * to force the use of another predicate for cross-references that use a
     * specific prefix name.
     * 
     * @param prefixName The prefix name, as used in cross-references.
     * @param predicate  The mapping predicate to use.
     */
    public void addPrefixToPredicateMapping(String prefixName, String predicate) {
        prefixToPredicateMap.put(prefixName, predicate);
    }

    /**
     * Gets the mappings between prefix names and predicates from annotations in the
     * given ontology.
     * <p>
     * This method recognises the following annotations:
     * <ul>
     * <li>{@code oboInOwl#treat-xrefs-as-equivalent}: map a prefix name to the
     * {@code skos:exactMatch} predicate;
     * <li>{@code oboInOwl#treat-xrefs-as-is_a}: map a prefix name to the
     * {@code skos:broadMatch} predicate;
     * <li>{@code oboInOwl#treat-xrefs-as-has-subclass}: map a prefix name to the
     * {@code skos:narrowMatch} predicate;
     * <li>{@code oboInOwl#treat-xrefs-as-genus-differentia}: map a prefix name to
     * the {@code semapv:crossSpeciesExactMatch} predicate;
     * <li>{@code oboInOwl#treat-xrefs-as-reverse-genus-differentia}: likewise.
     * </ul>
     * 
     * @param ontology The ontology to extract the prefix-to-predicate mappings
     *                 from.
     */
    public void fillPrefixToPredicateMap(OWLOntology ontology) {
        for ( OWLAnnotation annot : ontology.getAnnotations() ) {
            OWLAnnotationValue value = annot.getValue();
            if ( !value.isLiteral() ) {
                continue;
            }

            String v = value.asLiteral().get().getLiteral();

            switch ( annot.getProperty().getIRI().toString() ) {
            case XREF_AS_EQUIVALENT:
                prefixToPredicateMap.put(v, CommonPredicate.SKOS_EXACT_MATCH.toString());
                break;

            case XREF_AS_IS_A:
                prefixToPredicateMap.put(v, CommonPredicate.SKOS_BROAD_MATCH.toString());
                break;

            case XREF_AS_HAS_SUBCLASS:
                prefixToPredicateMap.put(v, CommonPredicate.SKOS_NARROW_MATCH.toString());
                break;

            case XREF_AS_GENUS_DIFFERENTIA:
            case XREF_AS_REVERSE_GENUS_DIFFERENTIA:
                // The value should be of the form "PREFIX part_of NCBITaxon:XXXX"
                String[] parts = v.split(" ");
                if ( parts.length == 3 ) {
                    prefixToPredicateMap.put(parts[0], CommonPredicate.SEMAPV_CROSS_SPECIES_EXACT_MATCH.toString());
                }
                break;
            }
        }
    }

    /**
     * Indicates whether cross-references on obsoleted terms should be extracted.
     * The default behaviour is to ignore obsoleted terms.
     * 
     * @param value {@code true} to extract cross-references on obsoleted terms.
     */
    public void includeObsoletes(boolean value) {
        ignoreObsoleteTerms = !value;
    }

    /**
     * Extract mappings from cross-references in the specified ontology. Only
     * cross-references with prefixes that are (1) known, and (2) associated with a
     * predicate (through either
     * {@link #addPrefixToPredicateMapping(String, String)} or
     * {@link #fillPrefixToPredicateMap(OWLOntology)}) are taken into account.
     * 
     * @param ontology The ontology to extract mappings from.
     * @return The set of mappings extracted from the ontology.
     */
    public MappingSet extract(OWLOntology ontology) {
        return this.extract(ontology, false, false);
    }

    /**
     * Extract mappings from cross-references in the specified ontology.
     * 
     * @param ontology       The ontology to extract mappings from.
     * @param permissive     If {@code true}, cross-references using an
     *                       non-resolvable prefix name will not be ignored.
     * @param includeGeneric If {@code true}, cross-references using a prefix name
     *                       that has not been associated to a mapping predicate
     *                       will not be ignored, and will yield mappings with the
     *                       {@code oboInOwl:hasDbXref} predicate.
     * @return The set of mappings extracted from the ontology.
     */
    public MappingSet extract(OWLOntology ontology, boolean permissive, boolean includeGeneric) {
        Set<String> usedPrefixNames = new HashSet<String>();
        MappingSet ms = MappingSet.builder().curieMap(new HashMap<String, String>()).mappings(new ArrayList<Mapping>())
                .build();

        String ontologyIRI = null;
        if ( ontology.getOntologyID().getOntologyIRI().isPresent() ) {
            ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
        }

        if ( includeGeneric ) {
            prefixManager.add("oboInOwl", OBO_IN_OWL);
            usedPrefixNames.add("oboInOwl");
        }

        for ( OWLClass c : ontology.getClassesInSignature() ) {
            if ( ignoreObsoleteTerms && isObsolete(ontology, c) ) {
                continue;
            }

            String label = null;
            for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(c.getIRI()) ) {
                if ( ax.getProperty().getIRI().toString().equals(HAS_DB_XREF) ) {
                    if ( !ax.getValue().isLiteral() ) {
                        continue;
                    }

                    String value = ax.getValue().asLiteral().get().getLiteral();
                    String[] parts = value.split(":", 2);
                    if ( parts.length != 2 ) {
                        continue;
                    }

                    if ( !prefixToPredicateMap.containsKey(parts[0]) && !includeGeneric ) {
                        continue;
                    }

                    String subjectId = c.getIRI().toString();
                    usedPrefixNames.add(prefixManager.getPrefixName(subjectId));

                    String objectId = prefixManager.expandIdentifier(value);
                    if ( !objectId.equals(value) ) {
                        usedPrefixNames.add(parts[0]);
                    } else if ( !permissive ) {
                        continue;
                    }

                    String predicateId = prefixToPredicateMap.getOrDefault(parts[0], HAS_DB_XREF);
                    if ( label == null ) {
                        label = OWLHelper.getLabel(ontology, c.getIRI(), null, false);
                    }

                    Mapping m = Mapping.builder().subjectId(subjectId).subjectLabel(label).objectId(objectId)
                            .predicateId(predicateId).mappingJustification(SEMAPV + "UnspecifiedMatching")
                            .subjectSource(ontologyIRI).build();

                    ms.getMappings().add(m);
                }
            }
        }

        for ( String usedPrefixName : usedPrefixNames ) {
            ms.getCurieMap().put(usedPrefixName, prefixManager.getPrefix(usedPrefixName));
        }

        return ms;
    }

    /**
     * Gets any prefix names encountered in cross-references that could not be
     * resolved.
     * 
     * @return The list of unknown prefix names.
     */
    public Set<String> getUnknownPrefixNames() {
        return prefixManager.getUnresolvedPrefixNames();
    }

    private boolean isObsolete(OWLOntology ontology, OWLClass c) {
        if ( falseValue == null ) {
            falseValue = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(false);
        }

        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(c.getIRI()) ) {
            if ( ax.getProperty().isDeprecated() ) {
                if ( ax.getValue().asLiteral().or(falseValue).parseBoolean() ) {
                    return true;
                }
            }
        }
        return false;
    }
}
