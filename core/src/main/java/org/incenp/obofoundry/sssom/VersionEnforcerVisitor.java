package org.incenp.obofoundry.sssom;

import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.MappingCardinality;
import org.incenp.obofoundry.sssom.model.Version;
import org.incenp.obofoundry.sssom.slots.EntityTypeSlot;
import org.incenp.obofoundry.sssom.slots.MappingCardinalitySlot;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;

/**
 * Helper object to forcibly remove from a set or a mapping all slots or slot
 * values that are incompatible with a given version of the SSSOM specification.
 * 
 * @param <T>
 */
public class VersionEnforcerVisitor<T> extends SlotVisitorBase<T> {

    private Version targetVersion = Version.LATEST;

    /**
     * Creates a new instance.
     * 
     * @param version The version of the SSSOM specification to be enforced.
     */
    public VersionEnforcerVisitor(Version version) {
        targetVersion = version;
    }

    @Override
    public void visit(Slot<T> slot, T object, Object unused) {
        if ( !slot.getCompliantVersion().isCompatibleWith(targetVersion) ) {
            slot.setValue(object, (Object) null);
        }
    }

    @Override
    public void visit(EntityTypeSlot<T> slot, T object, EntityType value) {
        if ( targetVersion == Version.SSSOM_1_0 && value == EntityType.COMPOSED_ENTITY_EXPRESSION ) {
            slot.setValue(object, (Object) null);
        }
    }

    @Override
    public void visit(MappingCardinalitySlot<T> slot, T object, MappingCardinality value) {
        if ( targetVersion == Version.SSSOM_1_0 && value == MappingCardinality.NONE_TO_NONE ) {
            slot.setValue(object, (Object) null);
        }
    }
}
