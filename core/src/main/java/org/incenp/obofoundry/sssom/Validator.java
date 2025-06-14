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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.incenp.obofoundry.sssom.checks.DuplicatedRecordIdValidator;
import org.incenp.obofoundry.sssom.checks.IMappingSetValidator;
import org.incenp.obofoundry.sssom.checks.IMappingValidator;
import org.incenp.obofoundry.sssom.checks.MissingJustificationValidator;
import org.incenp.obofoundry.sssom.checks.MissingLicenseValidator;
import org.incenp.obofoundry.sssom.checks.MissingObjectValidator;
import org.incenp.obofoundry.sssom.checks.MissingPredicateValidator;
import org.incenp.obofoundry.sssom.checks.MissingRecordIdValidator;
import org.incenp.obofoundry.sssom.checks.MissingSetIdValidator;
import org.incenp.obofoundry.sssom.checks.MissingSubjectValidator;
import org.incenp.obofoundry.sssom.checks.PredicateTypeValidator;
import org.incenp.obofoundry.sssom.checks.RedefinedBuiltinPrefixValidator;
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

    private ArrayList<IMappingSetValidator> setValidators = new ArrayList<>();
    private ArrayList<IMappingValidator> mappingValidators = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public Validator() {
        this(ValidationLevel.FULL);
    }

    /**
     * Creates a new instance with the specified validation level.
     * 
     * @param validationLevel Indicates which checks should be performed.
     */
    public Validator(ValidationLevel validationLevel) {
        switch ( validationLevel ) {
        case DISABLED:
            break;

        case FULL:
            MissingRecordIdValidator v1 = new MissingRecordIdValidator();
            DuplicatedRecordIdValidator v2 = new DuplicatedRecordIdValidator();
            setValidators.add(v1);
            setValidators.add(v2);
            mappingValidators.add(v1);
            mappingValidators.add(v2);
            // Fall-through

        case EXTENDED:
            setValidators.add(new MissingSetIdValidator());
            setValidators.add(new MissingLicenseValidator());
            setValidators.add(new RedefinedBuiltinPrefixValidator());
            // Fall-through

        case MINIMAL:
            mappingValidators.add(new MissingSubjectValidator());
            mappingValidators.add(new MissingObjectValidator());
            mappingValidators.add(new MissingPredicateValidator());
            mappingValidators.add(new MissingJustificationValidator());
            mappingValidators.add(new PredicateTypeValidator());
            break;
        }
    }

    /**
     * Checks whether the given mapping set, including the mappings it contains, is
     * valid.
     * 
     * @param ms The mapping set to validate.
     * @return A set of all the validation errors encountered when checking the
     *         mapping set (will be empty if the mapping set is in fact valid).
     */
    public EnumSet<ValidationError> validate(MappingSet ms) {
        return validate(ms, true);
    }

    /**
     * Checks whether the given mapping set is valid.
     * <p>
     * This method allows to skip checking the individual mappings if the set itself
     * is invalid.
     * 
     * @param ms                  The mapping set to validate.
     * @param alwaysCheckMappings If {@code true}, individual mappings will always
     *                            be checked; otherwise, they will be checked only
     *                            if there are no errors already at the level of the
     *                            mapping set.
     * @return A set of all the validation errors encountered when checking the
     *         mapping set (will be empty if the mapping set is in fact valid).
     */
    public EnumSet<ValidationError> validate(MappingSet ms, boolean alwaysCheckMappings) {
        EnumSet<ValidationError> result = EnumSet.noneOf(ValidationError.class);

        for ( IMappingSetValidator validator : setValidators ) {
            ValidationError error = validator.validate(ms);
            if ( error != null ) {
                result.add(error);
            }
        }

        if ( (!result.isEmpty() && !alwaysCheckMappings) || mappingValidators.isEmpty() ) {
            return result;
        }

        for ( Mapping m : ms.getMappings() ) {
            for ( IMappingValidator validator : mappingValidators ) {
                ValidationError error = validator.validate(m);
                if ( error != null ) {
                    result.add(error);
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the given mapping set is valid, and throws an exception if it
     * is not.
     * 
     * @param ms The mapping set to validate.
     * @throws SSSOMFormatException If the mapping set is invalid.
     */
    public void check(MappingSet ms) throws SSSOMFormatException {
        EnumSet<ValidationError> result = validate(ms, false);
        if ( !result.isEmpty() ) {
            throw new SSSOMFormatException(ValidationError.getMessage(result));
        }
    }

    /**
     * Validates an individual mapping. This method checks that the slots that are
     * required by the specification are present and non-empty.
     * 
     * @param mapping The mapping to validate.
     * @return An error message if the mapping is invalid, otherwise {@code null}.
     * @deprecated Use {@link #validate(MappingSet)} to validate an entire mapping
     *             set instead.
     */
    @Deprecated
    public String validate(Mapping mapping) {
        for ( IMappingValidator v : mappingValidators ) {
            ValidationError error = v.validate(mapping);
            if ( error != null ) {
                return error.getMessage();
            }
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
        if ( highest == Version.LATEST || set.getMappings() == null ) {
            return highest;
        }

        // Then check the mappings themselves. If one mapping requires the highest
        // supported version, then we can stop immediately, no need to loop over the
        // entire set.
        int nMappings = set.getMappings().size();
        int i = 0;
        VersionVisitor<Mapping> v = new VersionVisitor<>(versions);
        while ( i < nMappings && highest != Version.LATEST ) {
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
