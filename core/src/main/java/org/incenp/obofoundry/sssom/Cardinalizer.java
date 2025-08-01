/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;

/**
 * A helper object to compute cardinality values.
 * <p>
 * This class implements the idea, proposed to the SSSOM specification, that
 * there is not a single “absolute” cardinality, but that cardinality is instead
 * defined relatively to a ”scope”.
 * <p>
 * A “scope” is a list of mapping slot names (S1, S2, ..., Sn). Cardinality is
 * computed in the subset of mappings that have the same value for all slots in
 * the scope. If the scope is empty, cardinality is computed on the entire set.
 */
public class Cardinalizer {

    private Collection<Slot<Mapping>> scope;

    /**
     * Creates a new instance with an empty scope.
     */
    public Cardinalizer() {
        scope = Collections.emptyList();
    }

    /**
     * Creates a new instance with the specified slots as the scope.
     * 
     * @param slots The list of slots to take into account when computing
     *              cardinality. Names that do not correspond to valid SSSOM slots
     *              are silently ignored.
     */
    public Cardinalizer(Collection<String> slots) {
        scope = SlotHelper.getMappingHelper().getSlotsByName(slots);
    }

    /**
     * Computes the cardinality of all mappings in the given set, according to the
     * current scope, and fills the {@code mapping_cardinality} slot and the
     * {@code cardinality_scope} slot accordingly.
     * <p>
     * This overrides any cardinality information that may already be stored in each
     * mapping.
     * 
     * @param mappings The set of mappings on which to compute cardinality.
     */
    public void fillCardinality(List<Mapping> mappings) {
        HashMap<String, HashSet<String>> subjects = new HashMap<>();
        HashMap<String, HashSet<String>> objects = new HashMap<>();

        for ( Mapping m : mappings ) {
            if ( m.isUnmapped() ) {
                continue;
            }

            String subject = getSubject(m);
            String object = getObject(m);

            subjects.computeIfAbsent(object, k -> new HashSet<String>()).add(subject);
            objects.computeIfAbsent(subject, k -> new HashSet<String>()).add(object);
        }

        for ( Mapping m : mappings ) {
            if ( m.isUnmapped() ) {
                m.setMappingCardinality(null);
                m.setCardinalityScope(null);
                continue;
            }

            int nSubjects = subjects.get(getObject(m)).size();
            int nObjects = objects.get(getSubject(m)).size();

            MappingCardinality mc = null;
            if ( nSubjects == 1 ) {
                mc = nObjects == 1 ? MappingCardinality.ONE_TO_ONE : MappingCardinality.ONE_TO_MANY;
            } else {
                mc = nObjects == 1 ? MappingCardinality.MANY_TO_ONE : MappingCardinality.MANY_TO_MANY;
            }
            m.setMappingCardinality(mc);

            if ( !scope.isEmpty() ) {
                for ( Slot<Mapping> slot : scope ) {
                    m.getCardinalityScope(true).add(slot.getName());
                }
            } else {
                m.setCardinalityScope(null);
            }
        }
    }

    /**
     * Gets a string representing the subject of a mapping, that can be used for
     * cardinality computation.
     * 
     * @param mapping The mapping for which to derive a subject string.
     * @return A string that can be used to compare subjects across mappings.
     */
    public String getSubject(Mapping mapping) {
        String tag = null;
        String subject = null;
        if ( mapping.getSubjectType() == EntityType.RDFS_LITERAL ) {
            tag = "L\0";
            subject = mapping.getSubjectLabel();
        } else {
            tag = "E\0";
            subject = mapping.getSubjectId();
        }
        if ( subject == null ) {
            subject = "(no subject)";
        }
        return tag + subject + getScopeString(mapping);
    }

    /**
     * Gets a string representing the object of a mapping, that can be used for
     * cardinality computation.
     * 
     * @param mapping The mapping for which to derive an object string.
     * @return A string that can be used to compare objects across mappings.
     */
    public String getObject(Mapping mapping) {
        String tag = null;
        String object = null;
        if ( mapping.getObjectType() == EntityType.RDFS_LITERAL ) {
            tag = "L\0";
            object = mapping.getObjectLabel();
        } else {
            tag = "E\0";
            object = mapping.getObjectId();
        }
        if ( object == null ) {
            object = "(no object)";
        }
        return tag + object + getScopeString(mapping);
    }

    private String getScopeString(Mapping mapping) {
        StringBuilder sb = new StringBuilder();
        for ( Slot<Mapping> slot : scope ) {
            sb.append("\0");
            Object value = slot.getValue(mapping);
            sb.append(value != null ? value.toString() : "(null)");
        }
        return sb.toString();
    }
}
