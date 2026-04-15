/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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
 * The type of hash that the {@link MappingHasher} class should produce.
 */
public enum HashType {
    /**
     * The “SSSOM standard hash” as defined by the SSSOM specification.
     */
    STANDARD,

    /**
     * The hash that was used by default in prior versions of SSSOM-Java, before the
     * SSSOM standard hash had been defined.
     */
    LEGACY,

    /**
     * The “mapping sameness identifier”, a hash that is computed only on the
     * subject ID, predicate ID, and object ID.
     * 
     * @see <a href=
     *      "https://ts4nfdi.github.io/mapping-sameness-identifier/">Proposed
     *      specification for the mapping sameness identifier</a>
     */
    MAPPING_SAMENESS_ID
}
