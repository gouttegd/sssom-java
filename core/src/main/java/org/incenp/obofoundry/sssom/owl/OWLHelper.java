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
import java.util.function.Consumer;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.Navigation;

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

        for ( OWLAnnotationAssertionAxiom ax : ontology.getAxioms(OWLAnnotationAssertionAxiom.class, entity,
                Imports.INCLUDED, Navigation.IN_SUB_POSITION) ) {
            if ( ax.getSubject().equals(entity) && ax.getProperty().isLabel() && ax.getValue().isLiteral() ) {
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
        for ( OWLAnnotationAssertionAxiom ax : ontology.getAxioms(OWLAnnotationAssertionAxiom.class, entity,
                Imports.INCLUDED, Navigation.IN_SUB_POSITION) ) {
            if ( ax.getSubject().equals(entity) && ax.isDeprecatedIRIAssertion() ) {
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
        ArrayList<Mapping> mappings = new ArrayList<Mapping>();

        for ( Mapping m : ms.getMappings() ) {
            boolean keep = true;

            if ( !mode.contains(UpdateMode.ONLY_OBJECT) ) {
                if ( !updateForEntity(m, IRI.create(m.getSubjectId()), ontology, language, langStrict, mode,
                        (s) -> m.setSubjectLabel(s),
                        (s) -> m.setSubjectSource(s)) ) {
                    keep = false;
                }
            }

            if ( !mode.contains(UpdateMode.ONLY_SUBJECT) ) {
                if ( !updateForEntity(m, IRI.create(m.getObjectId()), ontology, language, langStrict, mode,
                        (s) -> m.setObjectLabel(s), (s) -> m.setObjectSource(s)) ) {
                    keep = false;
                }
            }

            if ( keep ) {
                mappings.add(m);
            }
        }

        ms.setMappings(mappings);
    }

    /*
     * Helper method to update a single mapping against a given entity.
     */
    private static boolean updateForEntity(Mapping mapping, IRI entity, OWLOntology ontology, String language,
            boolean langStrict, EnumSet<UpdateMode> mode, Consumer<String> labelUpdater,
            Consumer<String> sourceUpdater) {
        boolean keep = true;
        if ( ontology.containsEntityInSignature(entity, Imports.INCLUDED) ) {
            if ( isObsolete(ontology, entity) && mode.contains(UpdateMode.DELETE_OBSOLETE) ) {
                keep = false;
            }
            if ( mode.contains(UpdateMode.UPDATE_LABEL) ) {
                String label = getLabel(ontology, entity, language, langStrict);
                if ( label != null ) {
                    labelUpdater.accept(label);
                }
            }
            if ( mode.contains(UpdateMode.UPDATE_SOURCE) ) {
                IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().orNull();
                if ( ontologyIRI != null ) {
                    sourceUpdater.accept(ontologyIRI.toString());
                }
            }
        } else if ( mode.contains(UpdateMode.DELETE_MISSING) ) {
            keep = false;
        }
        return keep;
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
         * Removes any mapping whose subject or object does not exist in the ontology.
         */
        DELETE_MISSING,

        /**
         * Removes any mapping whose subject or object is marked as obsolete.
         */
        DELETE_OBSOLETE,

        /**
         * Only consider the subject side of a mapping.
         */
        ONLY_SUBJECT,

        /**
         * Only consider the object side of a mapping.
         */
        ONLY_OBJECT;

        public static final EnumSet<UpdateMode> ALL = EnumSet.allOf(UpdateMode.class);
    }
}
