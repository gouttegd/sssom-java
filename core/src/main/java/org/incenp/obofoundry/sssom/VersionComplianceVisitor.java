package org.incenp.obofoundry.sssom;

import java.util.HashSet;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;
import org.incenp.obofoundry.sssom.slots.VersionSlot;

/**
 * Helper visitor to determine the minimum version of the SSSOM specification is
 * required by a mapping or a mapping set.
 * 
 * @param <T> The type of object whose compliance must be checked.
 */
public class VersionComplianceVisitor<T> extends SlotVisitorBase<T> {

    private Set<Version> versions;

    /**
     * Creates a new instance.
     * <p>
     * When using this constructor, use {@link #getVersion()} to get the minimum
     * version required by a set or a mapping, after visiting the slots.
     */
    public VersionComplianceVisitor() {
        versions = new HashSet<>();
    }

    /**
     * Creates a new instance.
     * <p>
     * Use this constructor to share the set of visited versions across visitors
     * (e.g., between a visitor for mapping sets and a visitor for mappings).
     * 
     * @param versions The set that will hold the accumulated version objects.
     */
    public VersionComplianceVisitor(Set<Version> versions) {
        this.versions = versions;
    }

    /**
     * Gets the minimum version required, according to the slots visited so far.
     * 
     * @return Gets the version that defines all slots and all values in the slots
     *         that have been visited up to that point.
     */
    public Version getVersion() {
        return Version.getHighestVersion(versions);
    }

    @Override
    public void visit(Slot<T> slot, T object, Object unused) {
        // For almost all slots, all we need to do is to use directly the version the
        // slot itself declares to be needing
        versions.add(slot.getCompliantVersion());
    }

    @Override
    public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
        Version version = slot.getCompliantVersion();
        if ( version == Version.SSSOM_1_0 && value == EntityType.COMPOSED_ENTITY_EXPRESSION ) {
            // Even if the slot itself is compliant with 1.0, the "composed entity
            // expression" value was added in 1.1
            version = Version.SSSOM_1_1;
        }
        versions.add(version);
    }

    @Override
    public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value) {
        Version version = slot.getCompliantVersion();
        if ( version == Version.SSSOM_1_0 && value == MappingCardinality.NONE_TO_NONE ) {
            // "0:0" cardinality value was added in 1.1
            version = Version.SSSOM_1_1;
        }
        versions.add(version);
    }

    @Override
    public void visit(VersionSlot<T> slot, T object, Version unused) {
        // Ignore the sssom_version slot, so that we do not consider a set as requiring
        // SSSOM 1.1 just because it has a sssom_version slot
    }
}
