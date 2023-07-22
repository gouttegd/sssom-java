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
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A class to generate OWL subclassOf axioms from mappings. Given a mapping
 * between a subject {@code S} and an object {@code O}, this class will generate
 * an axiom stating that {@code S} is a subclass of {@code O}.
 */
public class SubclassAxiomGenerator implements IMappingTransformer<OWLAxiom> {

    private OWLDataFactory factory;
    private boolean invert;

    /**
     * Creates a new instance.
     * 
     * @param ontology The ontology to generate axioms for.
     */
    public SubclassAxiomGenerator(OWLOntology ontology) {
        this(ontology, false);
    }

    /**
     * Creates a new instance, with the possibility of inverting the direction of
     * the generated axioms.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param invert   If {@code true}, the generated axiom will state that the
     *                 object is a subclass of the subject, instead of the other way
     *                 round.
     */
    public SubclassAxiomGenerator(OWLOntology ontology, boolean invert) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.invert = invert;
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        OWLClass subject = factory.getOWLClass(IRI.create(mapping.getSubjectId()));
        OWLClass object = factory.getOWLClass(IRI.create(mapping.getObjectId()));

        return invert ? factory.getOWLSubClassOfAxiom(object, subject) : factory.getOWLSubClassOfAxiom(subject, object);
    }

}
