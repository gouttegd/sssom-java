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

package org.incenp.obofoundry.sssom.checks;

import org.incenp.obofoundry.sssom.ValidationError;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents a single check for a possible post-parsing error that can make an
 * individual mapping invalid.
 */
public interface IMappingValidator {

    /**
     * Checks a mapping for a possible post-parsing error.
     * 
     * @param mapping The mapping to check.
     * @return An error code, or {@code null} if the set passes the test represented
     *         by this object.
     */
    public ValidationError validate(Mapping mapping);
}
