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

package org.incenp.obofoundry.sssom.transform;

import java.util.List;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A processor callback that has a string representation. This class merely
 * wraps an existing callback and is mostly intended for debugging, so that
 * filters created using lambda functions can still be displayed in a readable
 * form.
 */
public class NamedMappingProcessorCallback implements IMappingProcessorCallback {

    private String repr;
    private IMappingProcessorCallback impl;

    /**
     * Creates a new instance.
     * 
     * @param name     A string representation of the callback.
     * @param callback The actual callback.
     */
    public NamedMappingProcessorCallback(String name, IMappingProcessorCallback callback) {
        repr = name;
        impl = callback;
    }

    @Override
    public void process(IMappingFilter filter, List<Mapping> mappings) {
        impl.process(filter, mappings);
    }

    @Override
    public String toString() {
        return repr;
    }

}
