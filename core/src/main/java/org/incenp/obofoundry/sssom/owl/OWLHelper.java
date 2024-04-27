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

import java.util.ArrayList;
import java.util.EnumSet;

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
     * Checks whether an entity is marked as obsolete in the specified ontology. Of
     * note, this method does not check for <em>existence</em>; if the entity does
     * not exist in the ontology, it will return {@code false}, as if the entity
     * existed and was not obsolete.
     * 
     * @param ontology The ontology in which to look up the entity.
     * @param entity   The entity to look up.
     * @return {@code true} if the entity is obsolete, otherwise {@code false}.
     */
    public static boolean isObsolete(OWLOntology ontology, IRI entity) {
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(entity) ) {
            if ( ax.isDeprecatedIRIAssertion() ) {
                return true;
            }
        }
        return false;
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
        updateMappingSet(ms, ontology, language, strict, EnumSet.of(UpdateMode.UPDATE_LABEL, UpdateMode.UPDATE_SOURCE));
    }

    /**
     * Generic helper method to update a mapping set from a specified ontology. What
     * exactly is updated is specified by the {@link UpdateMode} flag values.
     * 
     * @param ms         The mapping set to update.
     * @param ontology   The ontology against which to update the mapping set.
     * @param language   If non-{@code null}, when updating labels, use labels in
     *                   the ontology that have a matching language tag, or no
     *                   language tag at all.
     * @param langStrict If {@code true}, do not fall back to using language-neutral
     *                   labels.
     * @param mode       What to update in the mapping set.
     */
    public static void updateMappingSet(MappingSet ms, OWLOntology ontology, String language, boolean langStrict, EnumSet<UpdateMode> mode) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().orNull();
        ArrayList<Mapping> mappings = new ArrayList<Mapping>();

        for ( Mapping m : ms.getMappings() ) {
            IRI subject = IRI.create(m.getSubjectId());
            IRI object = IRI.create(m.getObjectId());
            boolean keep = true;

            if ( ontology.containsEntityInSignature(subject) ) {
                if ( isObsolete(ontology, subject) && mode.contains(UpdateMode.DELETE_OBSOLETE_SUBJECT) ) {
                    keep = false;
                }
                if ( mode.contains(UpdateMode.UPDATE_LABEL) ) {
                    String label = getLabel(ontology, subject, language, langStrict);
                    if ( label != null ) {
                        m.setSubjectLabel(label);
                    }
                }
                if ( mode.contains(UpdateMode.UPDATE_SOURCE) && ontologyIRI != null ) {
                    m.setSubjectSource(ontologyIRI.toString());
                }
            }
            else if ( mode.contains(UpdateMode.DELETE_MISSING_SUBJECT) ) {
                keep = false;
            }

            if ( ontology.containsEntityInSignature(object) ) {
                if ( isObsolete(ontology, object) && mode.contains(UpdateMode.DELETE_OBSOLETE_OBJECT) ) {
                    keep = false;
                }
                if ( mode.contains(UpdateMode.UPDATE_LABEL) ) {
                    String label = getLabel(ontology, object, language, langStrict);
                    if ( label != null ) {
                        m.setObjectLabel(label);
                    }
                }
                if ( mode.contains(UpdateMode.UPDATE_SOURCE) && ontologyIRI != null ) {
                    m.setObjectSource(ontologyIRI.toString());
                }
            } else if ( mode.contains(UpdateMode.DELETE_MISSING_OBJECT) ) {
                keep = false;
            }

            if ( keep ) {
                mappings.add(m);
            }
        }

        ms.setMappings(mappings);
    }

    /**
     * Modes of operation for the
     * {@link OWLHelper#updateMappingSet(MappingSet, OWLOntology, String, boolean, EnumSet)}
     * method.
     */
    public enum UpdateMode {
        /**
         * Updates the object and subject labels from the labels of the corresponding
         * entities in the ontology.
         */
        UPDATE_LABEL,

        /**
         * If the subject (respectively the object) exists in the ontology, sets the
         * {@code subject_source} (respectively the {@code object_source}) field to the
         * ontology IRI.
         */
        UPDATE_SOURCE,

        /**
         * Removes any mapping whose subject does not exist in the ontology.
         */
        DELETE_MISSING_SUBJECT,

        /**
         * Removes any mapping whose subject exists in the ontology but is marked as
         * obsolete.
         */
        DELETE_OBSOLETE_SUBJECT,

        /**
         * Removes any mapping whose object does not exist in the ontology.
         */
        DELETE_MISSING_OBJECT,

        /**
         * Removes any mapping whose object exists in the ontology but is marked as
         * obsolete.
         */
        DELETE_OBSOLETE_OBJECT;

        public static final EnumSet<UpdateMode> ALL = EnumSet.allOf(UpdateMode.class);
    }
}
