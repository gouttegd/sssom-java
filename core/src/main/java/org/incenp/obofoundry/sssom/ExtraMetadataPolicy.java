/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
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

/**
 * Represents the behaviour to adopt regarding non-standard metadata slots.
 */
public enum ExtraMetadataPolicy {
    /**
     * No non-standard metadata is ever allowed.
     * <p>
     * When reading a mapping set, this policy instructs the parser to discard any
     * non-standard metadata slot. When writing, this policy instructs the writer
     * never to write the contents of the {@code extra_metadata} field.
     */
    NONE,

    /**
     * Requires that non-standard metadata slots be declared.
     * <p>
     * When reading a mapping set, this policy instructs the parser to discard:
     * <ul>
     * <li>any set-level non-standard metadata except those under the
     * {@code extra_metadata} slot;
     * <li>any mapping-level non-standard slot except those declared in the
     * set-level {@code extra_columns} slot.
     * </ul>
     * When writing, this policy instructs the writer to write all available
     * non-standard metadata and to make sure the non-standard metadata are
     * <em>declared</em>:
     * <ul>
     * <li>for the set-level non-standard metadata, by writing them all under the
     * {@code extra_metadata} slot (instead of the root of the YAML metadata block);
     * <li>for the mapping-level non-standard metadata, by listing the names of the
     * non-standard columns in the set-level {@code extra_columns} slot.
     * </ul>
     */
    DECLARATION_REQUIRED,

    /**
     * Accepts all non-standard metadata without requiring a declaration.
     * <p>
     * When reading a mapping set, this policy instructs the parser to:
     * <ul>
     * <li>accept any set-level non-standard metadata and store it into the
     * {@code extra_metadata} slot;
     * <li>likewise accept any mapping-level non-standard metadata (non-standard
     * column) and store it into the mapping-level {@code extra_metadata} slot.
     * </ul>
     * When writing, this policy instructs the writer to:
     * <ul>
     * <li>write all set-level non-standard metadata as if they were standard
     * metadata, under the root of the YAML metadata block;
     * <li>write all mapping-level non-standard metadata as supplementary columns
     * (without declaring them first).
     * </ul>
     */
    ALL
}
