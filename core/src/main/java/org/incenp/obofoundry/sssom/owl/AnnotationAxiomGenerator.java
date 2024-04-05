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

    private OWLDataFactory factory;
    private OWLAnnotationProperty property;
    private IMappingTransformer<String> texter;
    boolean invert;

    /**
     * Creates a new instance. This generator will create an annotation assertion
     * axiom for the subject of the mapping.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param property The property to create an annotation with.
     * @param text     The value of the annotation.
     */

    /**
     * Creates a new instance. This generator will create an annotation assertion
     * axiom for the subject of the mapping.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param property The property to create an annotation with.
     * @param texter   A transformer that will format a mapping into the annotation
     *                 value. For example, to create an annotation with mapping's
     *                 subject label as value:
     * 
     *                 <pre>
     *                 AnnotationAxiomGenerator g = new AnnotationAxiomGenerator(ontology, property,
     *                         (mapping) -&gt; mapping.getSubjectLabel());
     *                 </pre>
     */
    public AnnotationAxiomGenerator(OWLOntology ontology, IRI property, IMappingTransformer<String> texter) {
        this(ontology, property, texter, false);
    }

    /**
     * Creates a new instance to annotate the object of a mapping rather than the
     * subject.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param property The property to create an annotation with.
     * @param texter   A transformer that will format a mapping into the annotation
     *                 value.
     * @param invert   If {@code true}, the object of the mapping will be annotated,
     *                 rather than the subject.
     */
    public AnnotationAxiomGenerator(OWLOntology ontology, IRI property, IMappingTransformer<String> texter,
            boolean invert) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.property = factory.getOWLAnnotationProperty(property);
        this.texter = texter;
        this.invert = invert;
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        String value = texter.transform(mapping);
        if ( value == null ) {
            return null;
        }
        IRI target = IRI.create(invert ? mapping.getObjectId() : mapping.getSubjectId());
        return factory.getOWLAnnotationAssertionAxiom(property, target, factory.getOWLLiteral(value));
    }

}
