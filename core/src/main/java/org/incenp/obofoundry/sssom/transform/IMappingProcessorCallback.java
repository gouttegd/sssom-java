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
 * Represents a custom processing step that can be called when applying rules to
 * a mapping set.
 */
public interface IMappingProcessorCallback {

    /**
     * Applies a custom process to a set of mappings. This method is called by
     * {@link MappingProcessor#process(List)} before applying the preprocessor
     * and/or the generator of the current rule.
     * 
     * @param filter   The filter of the rule currently being applied. May be
     *                 {@code null}.
     * @param mappings The current set of mappings.
     */
    public void process(IMappingFilter filter, List<Mapping> mappings);
}
