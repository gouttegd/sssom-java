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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A helper class to track <em>variables</em> that can have a different value
 * depending on the mapping that is currently being processed.
 * <p>
 * Example:
 * 
 * <pre>
 * VariableManager vm = new VariableManager();
 * vm.addVariable("myvar", "my default value");
 * vm.addVariable("myvar", "my value", (mapping) -&gt; mapping.getObjectId().startsWith("ABC");
 * ...
 * Mapping m = ...
 * String value = vm.expandVariable("myvar", m);
 * ...
 * IMappingTransformer&lt;String&gt; myVarTransformer = vm.getTransformer("myvar");
 * Mapping m = ...
 * String value = myVarTransformer.transform(m);
 * </pre>
 */
public class VariableManager {

    private Map<String, List<MappingVariable>> vars = new HashMap<String, List<MappingVariable>>();

    /**
     * Defines a new variable to track.
     * 
     * @param name  The name of the new variable.
     * @param value The variable's default value.
     */
    public void addVariable(String name, String value) {
        addVariable(name, value, null);
    }

    /**
     * Defines a new variable to track for certain mappings.
     * 
     * @param name   The name of the variable; if no variable with that name has
     *               been declared yet, it is created as needed,
     * @param value  The value of the variable.
     * @param filter The filter determining for which mappings the variable has the
     *               specified value.
     */
    public void addVariable(String name, String value, IMappingFilter filter) {
        if ( !vars.containsKey(name) ) {
            vars.put(name, new ArrayList<MappingVariable>());
        }
        if ( filter == null ) {
            filter = (mapping) -> true;
            // This is the default value, append it to the end
            vars.get(name).add(new MappingVariable(value, filter));
        } else {
            // Insert at the beginning of the list so that it takes precedence over any
            // previously set filter/value
            vars.get(name).add(0, new MappingVariable(value, filter));
        }
    }

    /**
     * Checks if a name is the name of a known variable.
     * 
     * @param name The name to check
     * @return {@code true} if a variable with the given name is known to this
     *         manager, otherwise {@code false}.
     */
    public boolean hasVariable(String name) {
        return vars.containsKey(name);
    }

    /**
     * Gets the value of a variable for a given mapping.
     * 
     * @param name    The name of the variable to lookup.
     * @param mapping The mapping for which to get the variable's value.
     * @return The value of the variable, according to the filter/values registered
     *         for that variable.
     */
    public String expandVariable(String name, Mapping mapping) {
        List<MappingVariable> values = vars.get(name);
        if ( values == null ) {
            throw new IllegalArgumentException(String.format("Undefined variable: %s", name));
        }

        for ( MappingVariable mv : values ) {
            if ( mv.filter.filter(mapping) ) {
                return mv.value;
            }
        }
        return "";
    }

    /**
     * Gets a mapping transformer that will return the value of the variable for the
     * mapping it is applied to.
     * 
     * @param name The variable to look up.
     * @return A mapping transformer that, when applied to a mapping, will yield the
     *         value the variable should have for this mapping.
     */
    public IMappingTransformer<Object> getTransformer(String name) {
        List<MappingVariable> values = vars.get(name);
        if ( values == null ) {
            throw new IllegalArgumentException(String.format("Undefined variable: %s", name));
        }

        IMappingTransformer<Object> transformer = (mapping) -> {
            for ( MappingVariable mv : values ) {
                if ( mv.filter.filter(mapping) ) {
                    return mv.value;
                }
            }
            return "";
        };

        return transformer;
    }

    private class MappingVariable {
        IMappingFilter filter;
        String value;

        MappingVariable(String value, IMappingFilter filter) {
            this.value = value;
            this.filter = filter;
        }
    }
}
