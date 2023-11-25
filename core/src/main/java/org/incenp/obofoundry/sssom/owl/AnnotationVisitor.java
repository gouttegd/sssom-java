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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotVisitorBase;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMetadataTransformer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * A mapping slots visitor that converts mapping metadata to OWL axiom
 * annotations.
 */
public class AnnotationVisitor extends SlotVisitorBase<Mapping, Void> {

    private OWLDataFactory factory;
    private IMetadataTransformer<Mapping, IRI> transformer;
    private Set<OWLAnnotation> annots;

    /**
     * Creates a new instance that creates annotations using properties directly
     * derived from the SSSOM specification.
     * 
     * @param factory The factory to use to create the axiom annotations.
     */
    AnnotationVisitor(OWLDataFactory factory) {
        this.factory = factory;
        transformer = new DirectMetadataTransformer();
        annots = new HashSet<OWLAnnotation>();
    }

    /**
     * Creates a new instance that creates annotations using a custom
     * slot-to-property transformer.
     * 
     * @param factory     The factory to use to create the axiom annotations.
     * @param transformer A transformer to obtain the IRI of an annotation property
     *                    from a metadata slot.
     */
    AnnotationVisitor(OWLDataFactory factory, IMetadataTransformer<Mapping, IRI> transformer) {
        this.factory = factory;
        this.transformer = transformer;
        annots = new HashSet<OWLAnnotation>();
    }

    /**
     * Annotates an axiom with the annotations generated from the visited slots.
     * 
     * @param axiom The axiom to annotate.
     * @return The annotated axiom.
     */
    public OWLAxiom annotate(OWLAxiom axiom) {
        return annots.isEmpty() ? axiom : axiom.getAnnotatedAxiom(annots);
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping mapping, String value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                slot.isEntityReference() ? IRI.create(value) : factory.getOWLLiteral(value)));
        return null;
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping mapping, List<String> values) {
        OWLAnnotationProperty p = factory.getOWLAnnotationProperty(transformer.transform(slot));
        for ( String value : values ) {
            annots.add(factory.getOWLAnnotation(p,
                    slot.isEntityReference() ? IRI.create(value) : factory.getOWLLiteral(value)));
        }
        return null;
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping mapping, Double value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value)));
        return null;
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping mapping, LocalDate value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value.format(DateTimeFormatter.ISO_DATE),
                        factory.getOWLDatatype(XSDVocabulary.DATE.getIRI()))));
        return null;
    }

    @Override
    public Void visit(Slot<Mapping> slot, Mapping mapping, Object value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value.toString())));
        return null;
    }
}
