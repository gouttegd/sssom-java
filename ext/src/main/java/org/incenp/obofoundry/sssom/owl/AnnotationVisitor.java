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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.ExtensionDefinition;
import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.incenp.obofoundry.sssom.slots.CurieMapSlot;
import org.incenp.obofoundry.sssom.slots.DateSlot;
import org.incenp.obofoundry.sssom.slots.DoubleSlot;
import org.incenp.obofoundry.sssom.slots.EntityReferenceSlot;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionDefinitionSlot;
import org.incenp.obofoundry.sssom.slots.ExtensionSlot;
import org.incenp.obofoundry.sssom.slots.ISlotVisitor;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.PredicateModifierSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.StringSlot;
import org.incenp.obofoundry.sssom.slots.URISlot;
import org.incenp.obofoundry.sssom.transform.IMetadataTransformer;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * A mapping slots visitor that converts mapping metadata to OWL annotations.
 */
public class AnnotationVisitor<T> implements ISlotVisitor<T> {

    private OWLDataFactory factory;
    private IMetadataTransformer<T, IRI> transformer;
    private Set<OWLAnnotation> annots;
    private boolean uriAsResource = false;

    /**
     * Creates a new instance that creates annotations using properties directly
     * derived from the SSSOM specification.
     * 
     * @param factory The factory to use to create the annotations.
     */
    AnnotationVisitor(OWLDataFactory factory) {
        this.factory = factory;
        transformer = new DirectMetadataTransformer<T>();
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
    AnnotationVisitor(OWLDataFactory factory, IMetadataTransformer<T, IRI> transformer) {
        this.factory = factory;
        this.transformer = transformer;
        annots = new HashSet<OWLAnnotation>();
    }

    /**
     * Specifies whether URI-typed slots (e.g., sssom:license) should be rendered as
     * IRI-identified resources rather than as xsd:anyURI literals.
     * <p>
     * URI-typed slots are rendered as resources in the RDF serialisation, so it
     * seems logical that they should be rendered similarly in the OWL
     * serialisation. But that has not been decided yet, so for now we support both.
     * 
     * @param value If {@code true}, URI-typed slots are rendered as IRIs.
     */
    public void renderURISlotsAsResources(boolean value) {
        uriAsResource = value;
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

    /**
     * Annotates an ontology with the annotations generated from the visited slots.
     * 
     * @param ontology The ontology to annotate.
     */
    public void annotate(OWLOntology ontology) {
        if ( !annots.isEmpty() ) {
            List<OWLOntologyChange> changes = new ArrayList<>();
            for ( OWLAnnotation annot : annots ) {
                changes.add(new AddOntologyAnnotation(ontology, annot));
            }
            ontology.getOWLOntologyManager().applyChanges(changes);
        }
    }

    @Override
    public void visit(StringSlot<T> slot, T object, String value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value)));
    }

    @Override
    public void visit(StringSlot<T> slot, T object, List<String> values) {
        OWLAnnotationProperty p = factory.getOWLAnnotationProperty(transformer.transform(slot));
        for ( String value : values ) {
            annots.add(factory.getOWLAnnotation(p, factory.getOWLLiteral(value)));
        }
    }

    @Override
    public void visit(URISlot<T> slot, T object, String value) {
        OWLAnnotationValue v = uriAsResource ? IRI.create(value)
                : factory.getOWLLiteral(value, OWL2Datatype.XSD_ANY_URI);
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)), v));
    }

    @Override
    public void visit(URISlot<T> slot, T object, List<String> values) {
        OWLAnnotationProperty p = factory.getOWLAnnotationProperty(transformer.transform(slot));
        for ( String value : values ) {
            OWLAnnotationValue v = uriAsResource ? IRI.create(value)
                    : factory.getOWLLiteral(value, OWL2Datatype.XSD_ANY_URI);
            annots.add(factory.getOWLAnnotation(p, v));
        }
    }

    @Override
    public void visit(EntityReferenceSlot<T> slot, T object, String value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                IRI.create(value)));
    }

    @Override
    public void visit(EntityReferenceSlot<T> slot, T object, List<String> values) {
        OWLAnnotationProperty p = factory.getOWLAnnotationProperty(transformer.transform(slot));
        for ( String value : values ) {
            annots.add(factory.getOWLAnnotation(p, IRI.create(value)));
        }
    }

    @Override
    public void visit(DoubleSlot<T> slot, T object, Double value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value)));
    }

    @Override
    public void visit(DateSlot<T> slot, T object, LocalDate value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value.format(DateTimeFormatter.ISO_DATE),
                        factory.getOWLDatatype(XSDVocabulary.DATE.getIRI()))));
    }

    @Override
    public void visit(Slot<T> slot, T mapping, Object value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value.toString())));
    }

    @Override
    public void visit(ExtensionSlot<T> slot, T object, Map<String, ExtensionValue> values) {
        for ( String property : values.keySet() ) {
            ExtensionValue value = values.get(property);
            OWLAnnotationValue annotValue = null;
            switch ( value.getType() ) {
            case BOOLEAN:
                annotValue = factory.getOWLLiteral(value.asBoolean());
                break;
            case DATE:
            case DATETIME:
                annotValue = factory.getOWLLiteral(value.toString(),
                        factory.getOWLDatatype(IRI.create(value.getType().toString())));
                break;
            case DOUBLE:
                annotValue = factory.getOWLLiteral(value.asDouble());
                break;
            case IDENTIFIER:
                annotValue = IRI.create(value.asString());
                break;
            case INTEGER:
                annotValue = factory.getOWLLiteral(value.asInteger());
                break;
            case OTHER:
                annotValue = factory.getOWLLiteral(value.toString());
                break;
            case STRING:
                annotValue = factory.getOWLLiteral(value.asString());
                break;
            case URI:
                annotValue = factory.getOWLLiteral(value.toString(), OWL2Datatype.XSD_ANY_URI);
                break;
            }
            if ( annotValue != null ) {
                annots.add(
                        factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(property)), annotValue));
            }
        }
    }

    @Override
    public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                IRI.create(value.getIRI())));
    }

    @Override
    public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                factory.getOWLLiteral(value.toString())));
    }

    @Override
    public void visit(PredicateModifierSlot<T> slot, T object, PredicateModifier value) {
        annots.add(factory.getOWLAnnotation(factory.getOWLAnnotationProperty(transformer.transform(slot)),
                IRI.create(value.getIRI())));
    }

    @Override
    public void visit(CurieMapSlot<T> slot, T object, Map<String, String> value) {
        // No annotation to generate for the curie map
    }

    @Override
    public void visit(ExtensionDefinitionSlot<T> slot, T object, List<ExtensionDefinition> values) {
        // No annotation to generate for extension definitions
    }
}
