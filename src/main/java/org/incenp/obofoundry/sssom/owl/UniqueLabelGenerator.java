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
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * A class to generate "unique label" annotations from mappings.
 */
public class UniqueLabelGenerator implements IMappingTransformer<OWLAxiom> {

    private static final IRI OBOFOUNDRY_UNIQUE_LABEL = IRI.create("http://purl.obolibrary.org/obo/IAO_0000589");

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private String format;

    /**
     * Create a new instance.
     * 
     * @param ontology The ontology to generate axioms for.
     * @param format   A format string to use when generating the unique label;
     *                 within that string, {@code %s} will be replaced by the
     *                 {@code rdfs:label} of the mapping subject.
     */
    public UniqueLabelGenerator(OWLOntology ontology, String format) {
        this.ontology = ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.format = format;
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        IRI subjectIRI = IRI.create(mapping.getSubjectId());
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(subjectIRI) ) {
            if ( ax.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI()) ) {
                String uniqueLabel = String.format(format, ax.getValue().asLiteral().get().getLiteral());

                return factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(OBOFOUNDRY_UNIQUE_LABEL),
                        subjectIRI, factory.getOWLLiteral(uniqueLabel));
            }
        }

        return null;
    }

}
