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

package org.incenp.obofoundry.sssom.transform;

import org.incenp.obofoundry.sssom.slots.Slot;

/**
 * An interface to transform a metadata slot into another object.
 * 
 * @param <T> The type of SSSOM object (mapping or mapping set) whose slots
 *            should be transformed.
 * @param <V> The type of object to transform the slot into.
 */
public interface IMetadataTransformer<T, V> {

    /**
     * Transforms a metadata slot into something else.
     * 
     * @param slot The slot to transform.
     * @return The object generated from the slot.
     */
    public V transform(Slot<T> slot);

}
