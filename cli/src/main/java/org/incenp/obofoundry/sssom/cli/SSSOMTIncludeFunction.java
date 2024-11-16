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

package org.incenp.obofoundry.sssom.cli;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T-Mapping generator function "include".
 * <p>
 * This function does the opposite of the standard preprocessor function "stop".
 * It unconditionally produces the original mapping. It should be called to
 * include a mapping in the resulting set of the SSSOM/T-Mapping application.
 */
public class SSSOMTIncludeFunction
        implements ISSSOMTFunction<IMappingTransformer<Mapping>>, IMappingTransformer<Mapping> {

    @Override
    public String getName() {
        return "include";
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
        return mapping;
    }

}
