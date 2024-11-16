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

/**
 * Represents the SSSOM/T standard directive function "set_var".
 * <p>
 * This function expects two arguments: the name of the new variable to define,
 * and its default value.
 */
public class SSSOMTSetvarFunction implements ISSSOMTFunction<Void> {

    private VariableManager varMgr;
    private MappingFormatter formatter;

    /**
     * Creates a new instance.
     * 
     * @param <T>         The type of objects produced by the application.
     * @param application The application this function is a part of.
     */
    public <T> SSSOMTSetvarFunction(SSSOMTransformApplication<T> application) {
        varMgr = application.getVariableManager();
        formatter = application.getFormatter();
    }

    @Override
    public String getName() {
        return "set_var";
    }

    @Override
    public String getSignature() {
        return "SS";
    }

    @Override
    public Void call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        String name = arguments.get(0);

        varMgr.addVariable(name, arguments.get(1));
        formatter.setSubstitution(name, varMgr.getTransformer(name));

        return null;
    }
}
