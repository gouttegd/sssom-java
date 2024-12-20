/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.owl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.MappingFormatter;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplication;
import org.incenp.obofoundry.sssom.uriexpr.SSSOMTUriExpressionDeclareFormatFunction;
import org.incenp.obofoundry.sssom.uriexpr.SSSOMTUriExpressionExpandFunction;
import org.incenp.obofoundry.sssom.uriexpr.UriExpressionRegistry;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * A specialised application of the SSSOM/Transform language to read mapping
 * processing rules that produce OWL axioms.
 */
public class SSSOMTOwlApplication extends SSSOMTransformApplication<OWLAxiom> {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasonerFactory reasonerFactory;
    private OWLReasoner reasoner;
    private OWLLiteral falseValue;
    private EditableEntityChecker entityChecker;
    private Map<String, Set<String>> subClassesOf = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> subPropertiesOf = new HashMap<String, Set<String>>();
    private UriExpressionRegistry uriExprRegistry = new UriExpressionRegistry();

    /**
     * Creates a new instance.
     * 
     * @param ontology        A helper ontology. This does not need to be the
     *                        ontology the axioms will be injected to afterwards,
     *                        but it must contain declaration axioms for all the
     *                        object and annotation properties that may be used in
     *                        {@code create_axiom} function calls. It is also the
     *                        ontology used by the functions that check whether a
     *                        given entity exists or is a subclass of another
     *                        entity.
     * @param reasonerFactory The reasoner factory to use when this application will
     *                        need to reason over the ontology.
     */
    public SSSOMTOwlApplication(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
        this.ontology = ontology;
        this.reasonerFactory = reasonerFactory;
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
    }

    public void onInit(PrefixManager prefixManager) {
        super.onInit(prefixManager);

        entityChecker = new EditableEntityChecker(ontology, prefixManager);

        MappingFormatter fmt = getFormatter();

        // Override default subject/object_label substitutions to get the label from the
        // ontology if it is missing from the mapping
        fmt.setSubstitution("subject_label", (m) -> getSubjectLabel(m));
        fmt.setSubstitution("object_label", (m) -> getObjectLabel(m));

        // Old-style substitutions for compatibility with previous implementation
        fmt.setSubstitution("subject_curie", (m) -> prefixManager.shortenIdentifier(m.getSubjectId()));
        fmt.setSubstitution("object_curie", (m) -> prefixManager.shortenIdentifier(m.getObjectId()));

        // Current SSSOM/T-Owl functions
        registerDirective(new SSSOMTDeclareFunction(this));
        registerFilter(new SSSOMTExistsFunction(this));
        registerFilter(new SSSOMTIsAFunction(this));
        registerGenerator(new SSSOMTDirectFunction(this));
        registerGenerator(new SSSOMTAnnotateFunction(this));
        registerGenerator(new SSSOMTCreateAxiomFunction(this));

        // Old functions for compatibility with the previous implementation
        registerDirective(new SSSOMTOwlSetvarFunction(this));
        registerDirective(new SSSOMTDeclareClassFunction(this));
        registerDirective(new SSSOMTDeclareObjectPropertyFunction(this));
        registerPreprocessor(new SSSOMTCheckSubjectExistenceFunction(this));
        registerPreprocessor(new SSSOMTCheckObjectExistenceFunction(this));
        registerGenerator(new SSSOMTAnnotateSubjectFunction(this));
        registerGenerator(new SSSOMTAnnotateObjectFunction(this));

        // Enable support for URI Expressions
        registerDirective(new SSSOMTUriExpressionDeclareFormatFunction(this));
        fmt.setModifier(new SSSOMTUriExpressionExpandFunction(this));
    }

    /**
     * Gets the ontology checker used by this application. SSSOM/T-Owl functions may
     * use that object to add entities that should be recognised by OWLAPI classes
     * and methods even if they do not actually exist in the ontology.
     * 
     * @return The application’s entity checker.
     */
    public EditableEntityChecker getEntityChecker() {
        return entityChecker;
    }

    /**
     * Gets the helper ontology used by this application.
     * 
     * @return The helper ontology.
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * Gets the registry of URI Expression templates, needed to expand a URI
     * Expression into a OWL expression.
     * 
     * @return The registry.
     */
    public UriExpressionRegistry getUriExpressionRegistry() {
        return uriExprRegistry;
    }

    /**
     * Checks if a given class exists in the helper ontology and if it is not
     * obsolete.
     * <p>
     * For this method, “exists” means that the class is present in the ontology’s
     * signature.
     * 
     * @param cls The name of the class to check.
     * @return {@code true} if the class exists and is not deprecated, otherwise
     *         {@code false}.
     * 
     * @deprecated Use {@link #entityExists(String)} instead.
     */
    @Deprecated
    public boolean classExists(String cls) {
        return entityExists(cls);
    }

    /**
     * Checks if a given entity exists in the helper ontology and if it is not
     * obsolete.
     * <p>
     * For this method, “exists” means that the entity is present in the ontology’s
     * signature.
     * 
     * @param entity The name of the entity to check.
     * @return {@code true} if the entity exists and is not deprecated, otherwise
     *         {@code false}.
     */
    public boolean entityExists(String entity) {
        if ( falseValue == null ) {
            falseValue = factory.getOWLLiteral(false);
        }

        IRI entityIRI = IRI.create(entity);
        if ( ontology.containsEntityInSignature(entityIRI) ) {
            for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(entityIRI) ) {
                if ( ax.getProperty().isDeprecated() ) {
                    if ( ax.getValue().asLiteral().or(falseValue).parseBoolean() ) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a list of all descendants of a given class.
     * <p>
     * Implementation detail: the results are cached, so that calling this method
     * several times for the same parent will query the ontology only once.
     * 
     * @param root The name of the parent class.
     * @return A set containing all the descendants of the parent class (including
     *         the parent class itself).
     */
    public Set<String> getSubClassesOf(String root) {
        Set<String> classes = subClassesOf.get(root);
        if ( classes == null ) {
            classes = new HashSet<String>();
            classes.add(root);

            if ( reasoner == null ) {
                reasoner = reasonerFactory.createReasoner(ontology);
            }

            for ( OWLClass c : reasoner.getSubClasses(factory.getOWLClass(IRI.create(root)), false).getFlattened() ) {
                if ( !c.isBottomEntity() ) {
                    classes.add(c.getIRI().toString());
                }
            }

            subClassesOf.put(root, classes);
        }

        return classes;
    }

    /**
     * Gets a list of all descendants of a given property.
     * <p>
     * Implementation details: the results are cached, so that calling this method
     * several times for the same parent will query the ontology only once.
     * 
     * @param root The name of the parent property.
     * @return A set containing all the descendants of the parent property
     *         (including the parent property itself).
     */
    public Set<String> getSubPropertiesOf(String root) {
        Set<String> properties = subPropertiesOf.get(root);
        if ( properties == null ) {
            properties = new HashSet<String>();
            properties.add(root);

            if ( reasoner == null ) {
                reasoner = reasonerFactory.createReasoner(ontology);
            }

            IRI rootIRI = IRI.create(root);
            if ( ontology.containsObjectPropertyInSignature(rootIRI) ) {
                for ( OWLObjectPropertyExpression pe : reasoner
                        .getSubObjectProperties(factory.getOWLObjectProperty(rootIRI), false).getFlattened() ) {
                    if ( !pe.isBottomEntity() && pe.isNamed() ) {
                        properties.add(pe.asOWLObjectProperty().getIRI().toString());
                    }
                }
            } else if ( ontology.containsDataPropertyInSignature(rootIRI) ) {
                for ( OWLDataProperty dp : reasoner.getSubDataProperties(factory.getOWLDataProperty(rootIRI), false)
                        .getFlattened() ) {
                    if ( !dp.isBottomEntity() ) {
                        properties.add(dp.getIRI().toString());
                    }
                }
            }

            subPropertiesOf.put(root, properties);
        }

        return properties;
    }

    /**
     * Given an axiom generator, gets another generator that would produce the same
     * axiom but annotated with metadata from the mapping.
     * 
     * @param innerTransformer The original axiom generator.
     * @param spec             A list of SSSOM metadata slot names to annotate the
     *                         produced axiom with. See
     *                         {@link org.incenp.obofoundry.sssom.slots.SlotHelper#getMappingSlotList(String)}
     *                         for details about the expected format of that
     *                         argument.
     * @return The modified axiom generator.
     */
    public IMappingTransformer<OWLAxiom> createAnnotatedTransformer(IMappingTransformer<OWLAxiom> innerTransformer,
            String spec) {
        spec = spec.replaceAll("( |\r|\n|\t)", "");
        if ( spec.startsWith("direct:") ) {
            spec = spec.substring(7);
        }

        return new AnnotatedAxiomGenerator(ontology, innerTransformer, new DirectMetadataTransformer(),
                SlotHelper.getMappingSlotList(spec));
    }

    /*
     * Given a mapping, get its label. If the mapping does not have a subject_label
     * value, try getting the label from the helper ontology.
     */
    private String getSubjectLabel(Mapping mapping) {
        String label = mapping.getSubjectLabel();
        if ( label == null ) {
            label = getLabelFromOntology(mapping.getSubjectId());
        }
        if ( label == null ) {
            label = "Unknown subject";
        }
        return label;
    }

    /*
     * Likewise, but for the object label.
     */
    private String getObjectLabel(Mapping mapping) {
        String label = mapping.getObjectLabel();
        if ( label == null ) {
            label = getLabelFromOntology(mapping.getObjectId());
        }
        if ( label == null ) {
            label = "Unknown object";
        }
        return label;
    }

    /*
     * Common logic to getSubjectLabel and getObjectLabel.
     */
    private String getLabelFromOntology(String iri) {
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(IRI.create(iri)) ) {
            if ( ax.getProperty().isLabel() && ax.getValue().isLiteral() ) {
                return ax.getValue().asLiteral().get().getLiteral();
            }
        }
        return null;
    }
}
