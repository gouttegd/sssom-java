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

package org.incenp.obofoundry.sssom.owl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.MappingProcessingRule;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Generates OWL axioms from mappings.
 * <p>
 * This object works with a list of rules (represented by
 * {@link MappingProcessingRule} objects) that are applied sequentially to each
 * mappings in a set.
 */
public class OWLGenerator {

    private List<MappingProcessingRule<OWLAxiom>> rules = new ArrayList<MappingProcessingRule<OWLAxiom>>();
    private OWLLiteral falseValue = null;

    /**
     * Adds a processing rule.
     * 
     * @param filter       The filter used to determine whether the rule applies to
     *                     a given mapping. May be {@code null} for a rule that must
     *                     be applied to all mappings.
     * @param preprocessor The preprocessor used to modify the mapping. May be
     *                     {@code null} if no preprocessing is needed. If the
     *                     preprocessor returns {@code null} for a given mapping, no
     *                     further rules are applied for this mapping.
     * @param generator    The OWL axiom generator for the current mapping.
     */
    public void addRule(IMappingFilter filter, IMappingTransformer<Mapping> preprocessor,
            IMappingTransformer<OWLAxiom> generator) {
        rules.add(new MappingProcessingRule<OWLAxiom>(filter, preprocessor, generator));
    }

    /**
     * Adds a rule that stops any further processing for a given mapping.
     * 
     * @param filter The filter used to determine whether the rule applies to a
     *               given mapping.
     */
    public void addStopingRule(IMappingFilter filter) {
        rules.add(new MappingProcessingRule<OWLAxiom>(filter, (mapping) -> null, null));
    }

    /**
     * Enables checking for the existence of the mapping subject. When this option
     * is enabled, no axioms will be generated for mappings whose subject ID does
     * not exist in the given ontology or is marked as deprecated.
     * 
     * @param ontology The ontology where the existence of the mapping subject
     *                 should be checked.
     */
    public void setCheckSubjectExistence(OWLOntology ontology) {
        addStopingRule((mapping) -> entityAbsentOrDeprecated(ontology, mapping.getSubjectId()));
    }

    /**
     * Enables checking for the existence of the mapping object. When this option is
     * enabled, no axioms will be generated for mappings whose object ID does not
     * exist in the given ontology or is marked as deprecated.
     * 
     * @param ontology The ontology where the existence of the mapping object should
     *                 be checked.
     */
    public void setCheckObjectExistence(OWLOntology ontology) {
        addStopingRule((mapping) -> entityAbsentOrDeprecated(ontology, mapping.getObjectId()));
    }

    public Set<OWLAxiom> generate(List<Mapping> mappings) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        for ( Mapping mapping : mappings ) {
            for ( MappingProcessingRule<OWLAxiom> rule : rules ) {
                if ( mapping != null && rule.apply(mapping) ) {
                    mapping = rule.preprocess(mapping);
                    if ( mapping != null ) {
                        OWLAxiom axiom = rule.generate(mapping);
                        if ( axiom != null ) {
                            axioms.add(axiom);
                        }
                    }
                }
            }
        }

        return axioms;
    }

    /*
     * Helper method to check whether a given entity exists in an ontology. Returns
     * true if an entity is either absent or present but with a owl:deprecated
     * annotation.
     */
    private boolean entityAbsentOrDeprecated(OWLOntology ontology, String entity) {
        if ( falseValue == null ) {
            falseValue = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(false);
        }

        IRI entityIRI = IRI.create(entity);

        if ( ontology.containsClassInSignature(entityIRI) ) {
            for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(entityIRI) ) {
                if ( ax.getProperty().getIRI().equals(OWLRDFVocabulary.OWL_DEPRECATED.getIRI()) ) {
                    if ( ax.getValue().asLiteral().or(falseValue).parseBoolean() ) {
                        return true;
                    }
                }
            }

            return false;
        }

        return true;
    }
}
