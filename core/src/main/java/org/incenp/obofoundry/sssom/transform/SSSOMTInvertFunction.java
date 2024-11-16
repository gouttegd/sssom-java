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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.transform;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents the SSSOM/T standard preprocessor function "invert".
 * <p>
 * This function does not take any argument. It simply returns an inverted copy
 * of the original mapping, or drops the mapping if it cannot be inverted
 * (because the predicate has no known inverse).
 */
public class SSSOMTInvertFunction
        implements ISSSOMTFunction<IMappingTransformer<Mapping>>, IMappingTransformer<Mapping> {

    @Override
    public String getName() {
        return "invert";
    }

    @Override
    public String getSignature() {
        return "";
    }

    @Override
    public IMappingTransformer<Mapping> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return this;
    }

    @Override
    public Mapping transform(Mapping mapping) {
        return mapping.invert();
    }
}
