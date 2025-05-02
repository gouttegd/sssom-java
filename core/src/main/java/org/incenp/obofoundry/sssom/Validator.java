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

package org.incenp.obofoundry.sssom;

import java.util.HashSet;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.VersionSlot;

/**
 * Helper class to validate MappingSet and Mapping objects.
 * <p>
 * This class is primarily intended to be used internally by the parsers for
 * post-parsing validation, to ensure that the parsed objects are compliant with
 * the requirements from the SSSOM specification. However it may also be used
 * independently of the parsers, for example to validate a set that was
 * constructed <em>ex nihilo</em>.
 */
public class Validator {

    public static final String MISSING_SUBJECT_LABEL = "Missing subject_label";
    public static final String MISSING_SUBJECT_ID = "Missing subject_id";
    public static final String MISSING_OBJECT_LABEL = "Missing object_label";
    public static final String MISSING_OBJECT_ID = "Missing object_id";
    public static final String MISSING_PREDICATE = "Missing predicate_id";
    public static final String MISSING_JUSTIFICATION = "Missing mapping_justification";
    public static final String INVALID_PREDICATE_TYPE = "Invalid predicate_type";

    /**
     * Validates an individual mapping. This method checks that the slots that are
     * required by the specification are present and non-empty.
     * 
     * @param mapping The mapping to validate.
     * @return An error message if the mapping is invalid, otherwise {@code null}.
     */
    public String validate(Mapping mapping) {
        if ( mapping.getSubjectType() == EntityType.RDFS_LITERAL ) {
            if ( mapping.getSubjectLabel() == null || mapping.getSubjectLabel().isEmpty() ) {
                return MISSING_SUBJECT_LABEL;
            }
        } else {
            if ( mapping.getSubjectId() == null || mapping.getSubjectId().isEmpty() ) {
                return MISSING_SUBJECT_ID;
            }
        }

        if ( mapping.getObjectType() == EntityType.RDFS_LITERAL ) {
            if ( mapping.getObjectLabel() == null || mapping.getObjectLabel().isEmpty() ) {
                return MISSING_OBJECT_LABEL;
            }
        } else {
            if ( mapping.getObjectId() == null || mapping.getObjectId().isEmpty() ) {
                return MISSING_OBJECT_ID;
            }
        }

        if ( mapping.getPredicateId() == null || mapping.getPredicateId().isEmpty() ) {
            return MISSING_PREDICATE;
        }

        if ( mapping.getMappingJustification() == null || mapping.getMappingJustification().isEmpty() ) {
            return MISSING_JUSTIFICATION;
        }

        if ( mapping.getPredicateType() == EntityType.RDFS_LITERAL
                || mapping.getPredicateType() == EntityType.COMPOSED_ENTITY_EXPRESSION ) {
            return INVALID_PREDICATE_TYPE;
        }

        return null;
    }

    /**
     * Gets the minimum version of the SSSOM specification that the given set is
     * compliant with.
     * <p>
     * FIXME: This does not really have anything to do with “validation”, so this
     * class may not be the best place for such a method.
     * 
     * @param set The set whose compliance is to be checked.
     * @return The earliest version of the SSSOM specification that defines all
     *         slots and values required by the set.
     */
    public Version getCompliantVersion(MappingSet set) {
        Set<Version> versions = new HashSet<>();

        // Check minimal version required by set metadata
        SlotHelper.getMappingSetHelper().visitSlots(set, new VersionVisitor<MappingSet>(versions), false);
        Version highest = Version.getHighestVersion(versions);
        if ( highest == Version.SSSOM_1_1 || set.getMappings() == null ) {
            return highest;
        }

        // Then check the mappings themselves. If one mapping requires the highest
        // supported version, then we can stop immediately, no need to loop over the
        // entire set.
        int nMappings = set.getMappings().size();
        int i = 0;
        VersionVisitor<Mapping> v = new VersionVisitor<>(versions);
        while ( i < nMappings && highest != Version.SSSOM_1_1 ) {
            SlotHelper.getMappingHelper().visitSlots(set.getMappings().get(i++), v, false);
            highest = Version.getHighestVersion(versions);
        }

        return highest;
    }

    /*
     * Helper class to visit all the slots in a mapping set or a mapping and collect
     * the minimum required SSSOM version.
     */
    private class VersionVisitor<T> extends SlotVisitorBase<T> {
        Set<Version> versions;

        VersionVisitor(Set<Version> versions) {
            this.versions = versions;
        }

        @Override
        public void visit(Slot<T> slot, T object, Object value) {
            // For almost all slots, all we need to do is to use directly the version the
            // slot itself declares to be needing
            versions.add(slot.getCompliantVersion());
        }

        @Override
        public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
            Version slotVersion = slot.getCompliantVersion();
            if ( slotVersion == Version.SSSOM_1_0 && value == EntityType.COMPOSED_ENTITY_EXPRESSION ) {
                // Even if the slot itself is compliant with 1.0, the "composed entity
                // expression" value was added in 1.1
                slotVersion = Version.SSSOM_1_1;
            }
            versions.add(slotVersion);
        }

        @Override
        public void visit(VersionSlot<T> slot, T object, Version value) {
            // Ignore the sssom_version slot, so that we do not consider a set as requiring
            // SSSOM 1.1 just because it has a sssom_version slot
        }

    }
}
