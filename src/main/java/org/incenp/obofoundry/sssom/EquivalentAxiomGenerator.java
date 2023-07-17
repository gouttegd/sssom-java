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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A class to generate OWL equivalence axioms from mappings. Given a mapping
 * between a subject {@code S} and an object {@code O}, this class will generate
 * an equivalence axiom between S and O.
 * <p>
 * If a filler class expression {@code F} is given to the constructor, the
 * equivalence axiom will be between {@code S} and {@code (O and F)}.
 */
public class EquivalentAxiomGenerator implements IMappingTransformer<OWLAxiom> {

    private OWLDataFactory factory;
    private OWLClassExpression filler;

    /**
     * Create a new instance.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param filler   A class expression to combine with the object of the mapping
     *                 (may be {@code null}).
     */
    public EquivalentAxiomGenerator(OWLOntology ontology, OWLClassExpression filler) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.filler = filler;
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        OWLClass subject = factory.getOWLClass(IRI.create(mapping.getSubjectId()));
        OWLClass object = factory.getOWLClass(IRI.create(mapping.getObjectId()));

        OWLClassExpression equivalent;

        if ( filler != null ) {
            equivalent = factory.getOWLObjectIntersectionOf(object, filler);
        } else {
            equivalent = object;
        }

        return factory.getOWLEquivalentClassesAxiom(subject, equivalent);
    }
}
