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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.incenp.obofoundry.sssom.model.BuiltinPrefix;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

/**
 * Base class to serialise a mapping set.
 * <p>
 * This class should be derived into specialised classes that implement a
 * precise serialisation format.
 */
public abstract class SSSOMWriter {

    protected ExtraMetadataPolicy extraPolicy = ExtraMetadataPolicy.NONE;
    protected PropagationPolicy condensationPolicy = PropagationPolicy.NeverReplace;
    protected ExtensionSlotManager extensionManager;
    protected PrefixManager prefixManager = new PrefixManager();
    private boolean customMap = false;

    /**
     * Sets the Curie map to use to shorten identifiers. The Curie map associated
     * with the mapping set ({@link MappingSet#getCurieMap()}) will then be
     * completely ignored in favour of the specified map.
     * 
     * @param map A map associating prefix names to prefixes.
     */
    public void setCurieMap(Map<String, String> map) {
        prefixManager.add(map);
        customMap = true;
    }

    /**
     * Sets the policy to deal with non-standard metadata in the mapping set to
     * write.
     * 
     * @param policy The policy instructing the writer about what to do with any
     *               non-standard metadata. The default policy is
     *               {@link ExtraMetadataPolicy#NONE}, meaning that no non-standard
     *               metadata is ever written.
     */
    public void setExtraMetadataPolicy(ExtraMetadataPolicy policy) {
        extraPolicy = policy;
    }

    /**
     * Enables or disables the condensation of "propagatable slots".
     * 
     * @param enabled {@code False} to disable condensation; it is enabled by
     *                default.
     */
    public void setCondensationEnabled(boolean enabled) {
        condensationPolicy = enabled ? PropagationPolicy.NeverReplace : PropagationPolicy.Disabled;
    }

    /**
     * Serialises a mapping set into the underlying file. This method performs some
     * prelimiary steps that are common to all serialisers, then delegates the
     * actual serialisation task to {@link #doWrite(MappingSet)}.
     * 
     * @param mappingSet The mapping set to serialise.
     * @throws IOException If an I/O error occurs.
     */
    public void write(MappingSet mappingSet) throws IOException {
        if ( !customMap ) {
            prefixManager.add(mappingSet.getCurieMap());
        }

        // The "license" slot MUST be present.
        if ( mappingSet.getLicense() == null || mappingSet.getLicense().isEmpty() ) {
            mappingSet.setLicense("https://w3id.org/sssom/license/all-rights-reserved");
        }

        // Ditto for the mapping set ID
        if ( mappingSet.getMappingSetId() == null || mappingSet.getMappingSetId().isEmpty() ) {
            mappingSet.setMappingSetId("http://sssom.invalid/" + UUID.randomUUID().toString());
        }

        // Compute effective definitions for non-standard slots
        extensionManager = new ExtensionSlotManager(extraPolicy, prefixManager);
        extensionManager.fillFromExistingExtensions(mappingSet);
        mappingSet.setExtensionDefinitions(extensionManager.getDefinitions(true, false));

        doWrite(mappingSet);
    }

    /**
     * Actually serialises a mapping set. This method should be overridden by
     * derived classes to implement the serialisation in a specific format.
     * 
     * @param mappingSet The mapping set to serialise.
     * @throws IOException If an I/O error occurs.
     */
    protected abstract void doWrite(MappingSet mappingSet) throws IOException;

    /**
     * Gets the prefix names that are actually used in the given set. This excludes
     * prefix names that are considered builtin.
     * 
     * @param mappingSet The mapping set to query for used prefix names.
     * @return A set of all prefix names that are effectively needed to condense all
     *         identifiers in the mapping set.
     */
    protected Set<String> getUsedPrefixes(MappingSet mappingSet) {
        return getUsedPrefixes(mappingSet, false);
    }

    /**
     * Gets the prefix names that are actually used in the given set, possibly
     * including the builtin prefix names.
     * 
     * @param mappingSet     The mapping set to query for used prefix names.
     * @param includeBuiltin If {@code true}, builtin prefix names will be included.
     * @return A set of all prefix names that are effectively needed to condense all
     *         identifiers in the mapping set.
     */
    protected Set<String> getUsedPrefixes(MappingSet mappingSet, boolean includeBuiltin) {
        HashSet<String> usedPrefixes = new HashSet<String>();
        SlotHelper.getMappingSetHelper().visitSlots(mappingSet, new PrefixUsageVisitor<MappingSet>(usedPrefixes));
        PrefixUsageVisitor<Mapping> puv = new PrefixUsageVisitor<Mapping>(usedPrefixes);
        mappingSet.getMappings().forEach(m -> SlotHelper.getMappingHelper().visitSlots(m, puv));
        usedPrefixes.addAll(extensionManager.getUsedPrefixes());
        if ( !includeBuiltin ) {
            for ( BuiltinPrefix bp : BuiltinPrefix.values() ) {
                usedPrefixes.remove(bp.getPrefixName());
            }
        }

        return usedPrefixes;
    }

    /**
     * Performs condensation of "propagatable slots" in a mapping set.
     * <p>
     * If condensation is disabled (see {@link #setCondensationEnabled(boolean)}),
     * this is merely a no-op.
     * 
     * @param mappingSet The mapping set whose slots should be condensed.
     * @return A set of all mapping slot names that have been effectively condensed
     *         (may be empty if no slots have been condensed at all).
     */
    protected Set<String> condenseSet(MappingSet mappingSet) {
        return new SlotPropagator(condensationPolicy).condense(mappingSet, true);
    }

    /*
     * Used by getUsedPrefixes. Visits all slots in a mapping or mapping set to
     * record the IRI prefixes used by entity references. This excludes extension
     * slots.
     */
    private class PrefixUsageVisitor<T> extends SlotVisitorBase<T, Void> {
        
        Set<String> usedPrefixes;
        
        PrefixUsageVisitor(Set<String> usedPrefixes) {
            this.usedPrefixes = usedPrefixes;
        }

        @Override
        public Void visit(Slot<T> slot, T object, String value) {
            if ( slot.isEntityReference() ) {
                String prefix = prefixManager.getPrefixName(value);
                if ( prefix != null ) {
                    usedPrefixes.add(prefix);
                }
            }
            return null;
        }

        @Override
        public Void visit(Slot<T> slot, T object, List<String> values) {
            if ( slot.isEntityReference() ) {
                for ( String value : values ) {
                    String prefix = prefixManager.getPrefixName(value);
                    if ( prefix != null ) {
                        usedPrefixes.add(prefix);
                    }
                }
            }
            return null;
        }
    }
}
