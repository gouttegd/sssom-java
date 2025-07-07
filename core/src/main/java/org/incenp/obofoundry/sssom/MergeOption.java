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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom;

import java.util.EnumSet;

/**
 * Represents various possible behaviours when merging a mapping set into
 * another.
 * <p>
 * This is used to control the behaviour of the {@link SetMerger} object.
 */
public enum MergeOption {
    /**
     * Add mappings of the source set to the mappings of the destination set.
     */
    MERGE_MAPPINGS,

    /**
     * Fill the scalar (single-valued) slots of the destination set with the values
     * from the source set (overwriting any existing value).
     */
    MERGE_SCALARS,

    /**
     * Merge the multi-valued slots of the source set into the corresponding slots
     * of the destination set.
     */
    MERGE_LISTS,

    /**
     * Merge extension slots from the source set into the destination set. If both
     * sets have an extension slot with the same property, the value from the source
     * set takes precedence.
     */
    MERGE_EXTENSIONS,

    /**
     * Merge the curie map from the source set into the destination set. If both
     * maps define the same prefix name, the definition from the source set takes
     * precedence.
     */
    MERGE_CURIE_MAP;

    /**
     * Merge all metadata from the source set into the destination set.
     * <p>
     * This is equivalent to {@link #MERGE_SCALARS} | {@link #MERGE_LISTS} |
     * {@link #MERGE_EXTENSIONS} | {@link #MERGE_CURIE_MAP}.
     */
    public final static EnumSet<MergeOption> MERGE_METADATA = EnumSet.of(MERGE_SCALARS, MERGE_LISTS, MERGE_CURIE_MAP,
            MERGE_EXTENSIONS);

    /**
     * Merge all mappings and all metadata from the source set into the destination
     * set, except the scalar (single-valued) slots.
     * <p>
     * This is the default behaviour of the {@link SetMerger} object, for
     * compatibility with established behaviour of the {@code sssom-cli} command.
     */
    public final static EnumSet<MergeOption> DEFAULT = EnumSet.of(MERGE_MAPPINGS, MERGE_LISTS, MERGE_CURIE_MAP,
            MERGE_EXTENSIONS);
}
