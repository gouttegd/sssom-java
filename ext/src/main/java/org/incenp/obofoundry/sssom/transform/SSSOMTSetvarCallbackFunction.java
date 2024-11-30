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
 * Represents the SSSOM/T standard callback function "set_var".
 * <p>
 * This function is similar to the <em>directive</em> function "set_var", but it
 * is called when "set_var" is found after a filter, as in:
 * 
 * <pre>
 * predicate==skos:exactMatch -> set_var("MYVAR", "value for exact mappings");
 * </pre>
 * <p>
 * It sets the value that the indicated variable should take for mappings that
 * match the filter.
 */
public class SSSOMTSetvarCallbackFunction implements ISSSOMTFunction<IMappingProcessorCallback> {

    private VariableManager varMgr;

    /**
     * Creates a new instance.
     * 
     * @param <T>         The type of objects produced by the application.
     * @param application The application this function is a part of.
     */
    public <T> SSSOMTSetvarCallbackFunction(SSSOMTransformApplication<T> application) {
        varMgr = application.getVariableManager();
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
    public IMappingProcessorCallback call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        String name = arguments.get(0);
        String value = arguments.get(1);
        return (filter, mappings) -> varMgr.addVariable(name, value, filter);
    }
}
