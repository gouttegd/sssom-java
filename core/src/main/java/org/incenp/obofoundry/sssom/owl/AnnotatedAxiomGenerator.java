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

import java.util.Arrays;
import java.util.List;

import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A class to generated annotated OWL axioms from a mapping. This class uses
 * another mapping transformer to create the initial axiom from the mapping
 * itself, then annotates that axiom with the mapping metadata.
 */
public class AnnotatedAxiomGenerator implements IMappingTransformer<OWLAxiom> {

    private OWLDataFactory factory;
    private IMappingTransformer<OWLAxiom> innerGenerator;
    private SlotHelper<Mapping> slotHelper;

    /**
     * Creates a new instance that generate “direct” OWL axioms.
     * <p>
     * Using this constructor is equivalent to calling
     * 
     * <pre>
     * new AnnotatedAxiomGenerator(ontology, new DirectAxiomGenerator(ontology));
     * </pre>
     * 
     * @param ontology The ontology to generate axioms for.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology) {
        this(ontology, new DirectAxiomGenerator(ontology));
    }

    /**
     * Creates a new instance.
     * 
     * @param ontology The ontology to generate axiom for.
     * @param inner    The mapping transformer to produce the axioms to annotate.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, IMappingTransformer<OWLAxiom> inner) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        innerGenerator = inner;
        slotHelper = SlotHelper.getMappingHelper(true);
        slotHelper.excludeSlots(
                Arrays.asList(new String[] { "subject_id", "predicate_id", "object_id", "mapping_cardinality" }));
    }

    /**
     * Creates a new instance with a customised list of slots to use to generate the
     * annotations.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param inner    The mapping transformer to produce the axioms to annotate.
     * @param slots    The list of slots from which to derive annotations.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, IMappingTransformer<OWLAxiom> inner, List<String> slots) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        innerGenerator = inner;
        slotHelper = SlotHelper.getMappingHelper(true);

        // TODO: Validate the list of slots
        slotHelper.setSlots(slots, false);
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        OWLAxiom axiom = innerGenerator.transform(mapping);
        if ( axiom != null ) {
            AnnotationVisitor visitor = new AnnotationVisitor(factory);
            slotHelper.visitSlots(mapping, visitor);
            axiom = visitor.annotate(axiom);
        }
        return axiom;
    }


}
