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

package org.incenp.obofoundry.sssom.owl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T-Owl variant of the SSSOM/T directive "set_var".
 * <p>
 * This function overrides the standard SSSOM/T directive to:
 * <ul>
 * <li>make it possible to refer to a declared variable using “old-style”,
 * unbracketed placeholders (e.g., <code>%MY_VAR</code> instead of
 * <code>%{MY_VAR}</code>);
 * <li>support the 3-argument form, for backwards compatibility.
 * </ul>
 * <p>
 * The standard, 2-argument form is used in the same way as the standard SSSOM/T
 * function: it expects the name of the variable to define, and its default
 * value.
 * <p>
 * The 3-argument form expects a 3rd argument that represents the condition a
 * mapping must satisfy for the variable to take the specified value.
 * 
 * @deprecated Maintained for backwards compatibility only.
 */
@Deprecated
public class SSSOMTOwlSetvarFunction implements ISSSOMTFunction<Void> {

    private SSSOMTOwlApplication app;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application object.
     */
    public SSSOMTOwlSetvarFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    @Override
    public String getName() {
        return "set_var";
    }

    @Override
    public String getSignature() {
        return "SSS?";
    }

    @Override
    public Void call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        String name = arguments.get(0);
        String value = arguments.get(1);

        if ( arguments.size() == 2 ) {
            app.getVariableManager().addVariable(name, value);
            app.getFormatter().setSubstitution(name, app.getVariableManager().getTransformer(name));
            app.getFormatter().addSubstitution(name, app.getVariableManager().getTransformer(name));
        } else {
            String condition = arguments.get(2);
            String[] parts = condition.split(" ", 3);
            if ( parts.length != 3 || !parts[1].equals("is_a")
                    || (!parts[0].equals("%subject_id") && !parts[0].equals("%object_id")) ) {
                throw new SSSOMTransformError("Invalid condition for set_var: %s", condition);
            }

            IMappingFilter filter = null;
            Set<String> targetIds = app.getSubClassesOf(app.getPrefixManager().expandIdentifier(parts[2]));
            if ( parts[0].equals("%subject_id") ) {
                filter = (mapping) -> targetIds.contains(mapping.getSubjectId());
            } else {
                filter = (mapping) -> targetIds.contains(mapping.getObjectId());
            }

            app.getVariableManager().addVariable(name, value, filter);
        }

        return null;
    }
}
