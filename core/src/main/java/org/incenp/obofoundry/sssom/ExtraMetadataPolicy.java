/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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
     * Requires that non-standard metadata slots be defined.
     * <p>
     * When reading a mapping set, this policy instructs the parser to discard any
     * non-standard metadata slot except those defined in the set-level
     * {@code extension_definitions} slot.
     * <p>
     * When writing, this policy instructs the writer to write all available
     * non-standard metadata and to make sure the non-standard metadata are
     * <em>defined</em>.
     */
    DEFINED,

    /**
     * Accepts all non-standard metadata without requiring a definition.
     * <p>
     * When reading a mapping set, this policy instructs the parser to accept any
     * non-standard metadata slot whether it is defined or not.
     * <p>
     * When writing, this policy instructs the writer to write all non-standard
     * metadata slots without defining them.
     */
    UNDEFINED
}
