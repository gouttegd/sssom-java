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

import java.util.Collection;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.IMetadataTransformer;
import org.semanticweb.owlapi.model.IRI;
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
    private IMappingTransformer<OWLAxiom> generator;
    private IMetadataTransformer<Mapping, IRI> translator;
    private SlotHelper<Mapping> slotHelper;

    /**
     * Creates a new instance that generate “direct” OWL axioms with “direct”
     * annotations from the mapping metadata.
     * 
     * @param ontology The ontology to generate axioms for.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology) {
        this(ontology, new DirectAxiomGenerator(ontology), new DirectMetadataTransformer<Mapping>(), true);
    }

    /**
     * Creates a new instance that generates “direct” OWL axioms with “direct”
     * annotations from either the mapping metadata slots or all the mapping slots.
     * 
     * @param ontology     The ontology to generate axioms for.
     * @param onlyMetadata If {@code true}, only generate annotations from metadata
     *                     slots (excluding subject_id, predicate_id, and object_id,
     *                     as well as mapping_cardinality); otherwise, generate
     *                     annotations from available slots.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, boolean onlyMetadata) {
        this(ontology, new DirectAxiomGenerator(ontology), new DirectMetadataTransformer<Mapping>(), onlyMetadata);
    }

    /**
     * Creates a new instance that generates “direct” OWL axioms with “direct”
     * annotations from the specified list of slots.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param slots    The list of slots from which to derive annotations; if
     *                 {@code null}, all available slots will be used.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, Collection<String> slots) {
        this(ontology, new DirectAxiomGenerator(ontology), new DirectMetadataTransformer<Mapping>(), slots);
    }

    /**
     * Creates a new instance with the specified generator and metadata transformer.
     * Axiom annotations are derived from the mapping metadata slots only.
     * 
     * @param ontology       The ontology to generate axioms for.
     * @param innerGenerator The mapping transformer to produce the axioms to
     *                       annotate.
     * @param slotTranslator The metadata-to-IRI translator indicating the
     *                       annotation property to use for each slot.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, IMappingTransformer<OWLAxiom> innerGenerator,
            IMetadataTransformer<Mapping, IRI> slotTranslator) {
        this(ontology, innerGenerator, slotTranslator, true);
    }

    /**
     * Creates a new instance with the specified generator and metadata transformer.
     * Axiom annotations are derived from either the metadata slots only or from all
     * the mapping slots.
     * 
     * @param ontology       The ontology to generate axioms for.
     * @param innerGenerator The mapping transformer to produce the axioms to
     *                       annotate.
     * @param slotTranslator The metadata-to-IRI translator indicating the
     *                       annotation property to use for each metadata slot.
     * @param onlyMetadata   If {@code true}, only generate annotations from
     *                       metadata slots (excluding subject_id, predicate_id, and
     *                       object_id, as well as mapping_cardinality); otherwise,
     *                       generate annotations from available slots.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, IMappingTransformer<OWLAxiom> innerGenerator,
            IMetadataTransformer<Mapping, IRI> slotTranslator, boolean onlyMetadata) {
        this(ontology, innerGenerator, slotTranslator,
                onlyMetadata ? SlotHelper.getMappingSlotList("metadata,-mapping_cardinality") : null);
    }

    /**
     * Creates a new instance with the specified generator and metadata transformer,
     * and a customised list of slots to use to generate the annotations.
     * 
     * @param ontology       The ontology to generate axioms for.
     * @param innerGenerator The mapping transformer to produce the axioms to
     *                       annotate.
     * @param slotTranslator The metadata-to-IRI translator indicating the
     *                       annotation property to use for each slot.
     * @param slots          The list of slots from which to derive annotations; if
     *                       {@code null}, all available slots will be used.
     */
    public AnnotatedAxiomGenerator(OWLOntology ontology, IMappingTransformer<OWLAxiom> innerGenerator,
            IMetadataTransformer<Mapping, IRI> slotTranslator, Collection<String> slots) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        generator = innerGenerator;
        translator = slotTranslator;
        slotHelper = SlotHelper.getMappingHelper(slots != null);
        if ( slots != null ) {
            slotHelper.setSlots(slots);
        }
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        OWLAxiom axiom = generator.transform(mapping);
        if ( axiom != null ) {
            AnnotationVisitor<Mapping> visitor = new AnnotationVisitor<>(factory, translator);
            slotHelper.visitSlots(mapping, visitor);
            axiom = visitor.annotate(axiom);
        }
        return axiom;
    }
}
