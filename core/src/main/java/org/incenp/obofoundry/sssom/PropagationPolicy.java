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
 * The policy that defines how to propagate values between the mapping set level
 * and the level of individual mappings.
 */
public enum PropagationPolicy {
    /**
     * Always propagate the value from the source object to the destination objects,
     * regardless of whether a destination object already has a value for the slot
     * being propagated.
     */
    AlwaysReplace,

    /**
     * Propagate the value from the source object to the destination objects, only
     * for objects that do not already have a value for the slot being propagated.
     */
    ReplaceIfUnset,

    /**
     * If any of the destination objects already has a value for the slot being
     * propagated, do not propagate anything.
     */
    NeverReplace,

    /**
     * Never propagate values between mapping set level and individual mappings.
     */
    Disabled
}
