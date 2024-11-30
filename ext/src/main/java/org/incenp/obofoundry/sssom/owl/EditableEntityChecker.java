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

import java.util.HashSet;
import java.util.Set;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

/**
 * A helper class used by {@link SSSOMTOwlApplication} and some of its
 * functions. It is basically a standard OWL entity checker with the possibility
 * to dynamically add new entities to recognise.
 */
public class EditableEntityChecker implements OWLEntityChecker, OWLEntityVisitor {

    private OWLDataFactory factory;
    private PrefixManager pfxMgr;

    private Set<String> classNames = new HashSet<String>();
    private Set<String> objectPropertyNames = new HashSet<String>();
    private Set<String> dataPropertyNames = new HashSet<String>();
    private Set<String> individualNames = new HashSet<String>();
    private Set<String> datatypeNames = new HashSet<String>();
    private Set<String> annotationPropertyNames = new HashSet<String>();

    /**
     * Creates a new instance from an ontology. All entities declared in the
     * ontology are added to the checker.
     * 
     * @param ontology      The ontology whose entities should be added to the
     *                      object.
     * @param prefixManager The prefix manager this object will use to expand CURIEs
     *                      into IRIs when resolving entity names.
     */
    EditableEntityChecker(OWLOntology ontology, PrefixManager prefixManager) {
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        pfxMgr = prefixManager;

        for ( OWLDeclarationAxiom d : ontology.getAxioms(AxiomType.DECLARATION, Imports.INCLUDED) ) {
            d.getEntity().accept(this);
        }
    }

    /**
     * Adds a OWL class to the entities known to this object.
     * 
     * @param iri The name of the class to add.
     */
    public void addClass(String iri) {
        classNames.add(iri);
    }

    /**
     * Adds an object property to the entities known to this object.
     * 
     * @param iri The name of the object property to add.
     */
    public void addObjectProperty(String iri) {
        objectPropertyNames.add(iri);
    }

    /**
     * Adds a data property to the entities known to this object.
     * 
     * @param iri The name of the data property to add.
     */
    public void addDataproperty(String iri) {
        dataPropertyNames.add(iri);
    }

    /**
     * Adds an individual to the entities known to this object.
     * 
     * @param iri The name of the individual to add.
     */
    public void addIndividual(String iri) {
        individualNames.add(iri);
    }

    /**
     * Adds a datatype to the entities known to this object.
     * 
     * @param iri The name of the datatype to add.
     */
    public void addDatatype(String iri) {
        datatypeNames.add(iri);
    }

    /**
     * Adds an annotation property to the entities known to this object.
     * 
     * @param iri The name of the annotation property to add.
     */
    public void addAnnotationProperty(String iri) {
        annotationPropertyNames.add(iri);
    }

    @Override
    public void visit(OWLClass cls) {
        classNames.add(cls.getIRI().toString());
    }

    @Override
    public void visit(OWLObjectProperty property) {
        objectPropertyNames.add(property.getIRI().toString());
    }

    @Override
    public void visit(OWLDataProperty property) {
        dataPropertyNames.add(property.getIRI().toString());
    }

    @Override
    public void visit(OWLNamedIndividual individual) {
        individualNames.add(individual.getIRI().toString());
    }

    @Override
    public void visit(OWLDatatype datatype) {
        datatypeNames.add(datatype.getIRI().toString());
    }

    @Override
    public void visit(OWLAnnotationProperty property) {
        annotationPropertyNames.add(property.getIRI().toString());
    }

    @Override
    public OWLClass getOWLClass(String name) {
        name = getUnquotedIRI(name);
        if (classNames.contains(name)) {
            return factory.getOWLClass(IRI.create(name));
        }
        return null;
    }

    @Override
    public OWLObjectProperty getOWLObjectProperty(String name) {
        name = getUnquotedIRI(name);
        if ( objectPropertyNames.contains(name) ) {
            return factory.getOWLObjectProperty(IRI.create(name));
        }
        return null;
    }

    @Override
    public OWLDataProperty getOWLDataProperty(String name) {
        name = getUnquotedIRI(name);
        if ( dataPropertyNames.contains(name) ) {
            return factory.getOWLDataProperty(IRI.create(name));
        }
        return null;
    }

    @Override
    public OWLNamedIndividual getOWLIndividual(String name) {
        name = getUnquotedIRI(name);
        if ( individualNames.contains(name) ) {
            return factory.getOWLNamedIndividual(IRI.create(name));
        }
        return null;
    }

    @Override
    public OWLDatatype getOWLDatatype(String name) {
        name = getUnquotedIRI(name);
        if ( datatypeNames.contains(name) ) {
            return factory.getOWLDatatype(IRI.create(name));
        }
        return null;
    }

    @Override
    public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
        name = getUnquotedIRI(name);
        if ( annotationPropertyNames.contains(name) ) {
            return factory.getOWLAnnotationProperty(IRI.create(name));
        }
        return null;
    }

    private String getUnquotedIRI(String name) {
        int len = name.length();
        if ( len > 1 && name.charAt(0) == '<' ) {
            name = name.substring(1, len - 1);
        } else {
            name = pfxMgr.expandIdentifier(name);
        }

        return name;
    }
}
