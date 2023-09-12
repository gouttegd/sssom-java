package org.incenp.obofoundry.sssom.owl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.Slot;
import org.incenp.obofoundry.sssom.SlotHelper;
import org.incenp.obofoundry.sssom.SlotVisitorBase;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * A class to generate OWL axioms “directly”, that is without any external input
 * outside of the mappings themselves. The axioms are derived based on the “OWL
 * reification” rules set forth in the SSSOM specification.
 *
 * @see <a href=
 *      "https://mapping-commons.github.io/sssom/spec/#rdfxml-serialised-re-ified-owl-axioms">SSSOM
 *      specification</a>
 */
public class DirectAxiomGenerator implements IMappingTransformer<OWLAxiom> {

    private final static Set<String> ANNOTATION_PREDICATES = new HashSet<String>();
    private final static String OWL_EQUIVALENT_CLASS = "http://www.w3.org/2002/07/owl#equivalentClass";
    private final static String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
    private final static String SSSOM_BASE = "https://w3id.org/sssom/";

    static {
        ANNOTATION_PREDICATES.add("http://www.geneontology.org/formats/oboInOwl#hasDbXref");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2000/01/rdf-schema#seeAlso");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#exactMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#closeMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#relatedMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#narrowMatch");
        ANNOTATION_PREDICATES.add("http://www.w3.org/2004/02/skos/core#broadMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesExactMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesCloseMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesNarrowMatch");
        ANNOTATION_PREDICATES.add("https://w3id.org/semapv/vocab/crossSpeciesBroadMatch");
    }

    private OWLDataFactory factory;
    private OWLOntology ontology;
    private SlotHelper<Mapping> slotHelper;

    /**
     * Creates a new instance.
     * 
     * @param ontology The ontology to generate axioms for.
     */
    public DirectAxiomGenerator(OWLOntology ontology) {
        this.ontology = ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        slotHelper = SlotHelper.getMappingHelper(true);
        slotHelper.excludeSlots(Arrays.asList(new String[] { "subject_id", "predicate_id", "object_id" }));
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        OWLAxiom axiom = null;
        String predicate = mapping.getPredicateId();
        IRI subject = IRI.create(mapping.getSubjectId());
        IRI object = IRI.create(mapping.getObjectId());

        if ( predicate.equals(OWL_EQUIVALENT_CLASS) ) {
            axiom = factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(object), factory.getOWLClass(object));
        } else if ( predicate.equals(RDFS_SUBCLASS_OF) ) {
            axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLClass(object));
        } else if ( ANNOTATION_PREDICATES.contains(predicate) ) {
            axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(IRI.create(predicate)),
                    subject, object);
        } else {
            IRI predicateIRI = IRI.create(predicate);
            if ( ontology.containsAnnotationPropertyInSignature(predicateIRI) ) {
                axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(predicateIRI), subject,
                        object);
            } else if ( ontology.containsObjectPropertyInSignature(predicateIRI) ) {
                axiom = factory.getOWLSubClassOfAxiom(factory.getOWLClass(subject), factory.getOWLObjectSomeValuesFrom(
                        factory.getOWLObjectProperty(predicateIRI), factory.getOWLClass(object)));
            }
        }

        if ( axiom == null ) {
            return null;
        }

        // Add mapping metadata as annotations on the generated axiom.
        AnnotationVisitor visitor = new AnnotationVisitor(factory);
        slotHelper.visitSlots(mapping, visitor);
        if ( !visitor.annots.isEmpty() ) {
            axiom = axiom.getAnnotatedAxiom(visitor.annots);
        }

        return axiom;
    }

    private class AnnotationVisitor extends SlotVisitorBase<Mapping, Void> {
        Set<OWLAnnotation> annots = new HashSet<OWLAnnotation>();
        OWLDataFactory factory;

        AnnotationVisitor(OWLDataFactory factory) {
            this.factory = factory;
        }

        @Override
        public Void visit(Slot<Mapping> slot, Mapping mapping, String value) {
            annots.add(
                    factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SSSOM_BASE + slot.getName())),
                    slot.isEntityReference() ? IRI.create(value) : factory.getOWLLiteral(value)));
            return null;
        }

        @Override
        public Void visit(Slot<Mapping> slot, Mapping mapping, List<String> values) {
            OWLAnnotationProperty p = factory.getOWLAnnotationProperty(IRI.create(SSSOM_BASE + slot.getName()));
            for ( String value : values ) {
                annots.add(factory.getOWLAnnotation(p,
                        slot.isEntityReference() ? IRI.create(value) : factory.getOWLLiteral(value)));
            }
            return null;
        }

        @Override
        public Void visit(Slot<Mapping> slot, Mapping mapping, Double value) {
            annots.add(
                    factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SSSOM_BASE + slot.getName())),
                            factory.getOWLLiteral(value)));
            return null;
        }

        @Override
        public Void visit(Slot<Mapping> slot, Mapping mapping, LocalDate value) {
            annots.add(
                    factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SSSOM_BASE + slot.getName())),
                            factory.getOWLLiteral(value.format(DateTimeFormatter.ISO_DATE),
                                    factory.getOWLDatatype(XSDVocabulary.DATE.getIRI()))));
            return null;
        }

        @Override
        protected Void getDefault(Slot<Mapping> slot, Mapping mapping, Object value) {
            annots.add(
                    factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SSSOM_BASE + slot.getName())),
                            factory.getOWLLiteral(value.toString())));
            return null;
        }
    }

}
