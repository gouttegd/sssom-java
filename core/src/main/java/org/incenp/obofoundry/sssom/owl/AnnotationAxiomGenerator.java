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

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A class to generate OWL annotation assertion axioms from mappings.
 */
public class AnnotationAxiomGenerator implements IMappingTransformer<OWLAxiom> {

    private PrefixManager prefixManager;
    private OWLDataFactory factory;
    private OWLAnnotationProperty property;
    String text;
    boolean invert;

    /**
     * Creates a new instance. This generator will create an annotation assertion
     * axiom for the subject of the mapping.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param property The property to create an annotation with.
     * @param text     The value of the annotation.
     */
    public AnnotationAxiomGenerator(OWLOntology ontology, IRI property, String text) {
        this(ontology, property, text, null, false);
    }

    /**
     * Creates a new instance to annotate the object of the mapping rather than the
     * subject.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param property The property to create an annotation with.
     * @param text     The value of the annotation.
     * @param invert   If {@code true}, the object of the mapping will be annotated,
     *                 rather than the subject.
     */
    public AnnotationAxiomGenerator(OWLOntology ontology, IRI property, String text, boolean invert) {
        this(ontology, property, text, null, invert);
    }

    /**
     * Creates a new instance to annotate the subject of the mapping, with a prefix
     * manager to allow using the {@code %subject_curie} and {@code %object_curie}
     * placeholders in the annotation value.
     * 
     * @param ontology      The ontology to generate axioms for.
     * @param property      The property to create an annotation with.
     * @param text          The value of the annotation.
     * @param prefixManager The prefix manager to use for shortening identifiers.
     */
    public AnnotationAxiomGenerator(OWLOntology ontology, IRI property, String text, PrefixManager prefixManager) {
        this(ontology, property, text, prefixManager, false);
    }

    /**
     * Creates a new instance to annotate the object of the mapping rather than the
     * subject, and with a prefix manager to allow using the {@code %subject_curie}
     * and {@code %object_curie} placeholders in the annotation value.
     * 
     * @param ontology      The ontology to generate axioms for.
     * @param property      The property to create an annotation with.
     * @param text          The value of the annotation.
     * @param prefixManager The prefix manager to use for shortening identifiers.
     * @param invert        If {@code true}, the object of the mapping will be
     *                      annotated, rather than the subject.
     */
    public AnnotationAxiomGenerator(OWLOntology ontology, IRI property, String text, PrefixManager prefixManager,
            boolean invert) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.property = factory.getOWLAnnotationProperty(property);
        this.text = text;
        this.prefixManager = prefixManager;
        this.invert = invert;
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        String value = AxiomGeneratorFactory.substituteMappingVariables(text, mapping, prefixManager);
        IRI target = IRI.create(invert ? mapping.getObjectId() : mapping.getSubjectId());

        return factory.getOWLAnnotationAssertionAxiom(property, target, factory.getOWLLiteral(value));
    }

}
