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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.owl;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A class of helper methods to work with OWL ontologies.
 */
public class OWLHelper {

    /**
     * Gets the label of an OWL entity, if any.
     * 
     * @param ontology The ontology to get the label from.
     * @param entity   The entity whose label we are looking for.
     * @param language The language tag of the label. May be {@code null} if the
     *                 caller is happy to get any label regardless of the language.
     *                 May be the empty string to specifically request a
     *                 language-neutral label.
     * @param strict   If {@code true}, return only a label with the specified
     *                 language tag, or {@code null} if no such label could be
     *                 found; otherwise, may return a language-neutral label.
     * @return The entity's label, or {@code null} if no label was found.
     */
    public static String getLabel(OWLOntology ontology, IRI entity, String language, boolean strict) {
        String neutralLabel = null;
        String preferredLabel = null;
        String otherLabel = null;

        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(entity) ) {
            if ( ax.getProperty().isLabel() && ax.getValue().isLiteral() ) {
                OWLLiteral value = ax.getValue().asLiteral().get();
                if ( language != null && value.getLang().equalsIgnoreCase(language) ) {
                    preferredLabel = value.getLiteral();
                } else if ( value.getLang().isEmpty() ) {
                    neutralLabel = value.getLiteral();
                } else {
                    otherLabel = value.getLiteral();
                }
            }
        }

        if ( language != null ) {
            if ( strict ) {
                return preferredLabel;
            } else {
                return preferredLabel != null ? preferredLabel : neutralLabel;
            }
        } else {
            return preferredLabel != null ? preferredLabel : neutralLabel != null ? neutralLabel : otherLabel;
        }
    }

    /**
     * Updates the subject label, object label, subject source, and object source in
     * the mapping set using informations from the specified ontology.
     * 
     * @param ms       The mapping set to update.
     * @param ontology The ontology to use a source for the updated metadata.
     * @param language If non-{@code null}, update labels from labels in the
     *                 ontology that have a matching language tag, or no language
     *                 tag at all.
     * @param strict   If {@code true}, do not fall back to using language-neutral
     *                 labels.
     */
    public static void updateMappingSet(MappingSet ms, OWLOntology ontology, String language, boolean strict) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().orNull();
        for ( Mapping m : ms.getMappings() ) {
            IRI subject = IRI.create(m.getSubjectId());
            IRI object = IRI.create(m.getObjectId());

            if ( ontology.containsEntityInSignature(subject) ) {
                String label = getLabel(ontology, subject, language, strict);
                if ( label != null ) {
                    m.setSubjectLabel(label);
                    if ( ontologyIRI != null && m.getSubjectSource() == null ) {
                        m.setSubjectSource(ontologyIRI.toString());
                    }
                }
            }

            if ( ontology.containsEntityInSignature(object) ) {
                String label = getLabel(ontology, object, language, strict);
                if ( label != null ) {
                    m.setObjectLabel(label);
                    if ( ontologyIRI != null && m.getObjectSource() == null ) {
                        m.setObjectSource(ontologyIRI.toString());
                    }
                }
            }
        }
    }
}
