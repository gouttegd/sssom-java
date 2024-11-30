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

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents the SSSOM/T standard preprocessor function "edit".
 * <p>
 * This function expects arguments of the form <code>key=value</code>, where
 * <em>key</em> is the name of a SSSOM metadata slot and <em>value</em> is the
 * value to assign to that slot.
 * 
 * @deprecated Maintained for backwards compatibility only. The "assign"
 *             function ({@link SSSOMTAssignFunction}) should be preferred.
 */
@Deprecated
public class SSSOMTEditFunction implements ISSSOMTFunction<IMappingTransformer<Mapping>> {

    private PrefixManager pfxMgr;

    /**
     * Creates a new instance.
     * 
     * @param <T>         The type of objects produced by the application.
     * @param application The application this function is a part of.
     */
    public <T> SSSOMTEditFunction(SSSOMTransformApplication<T> application) {
        pfxMgr = application.getPrefixManager();
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getSignature() {
        return "S+";
    }

    @Override
    public IMappingTransformer<Mapping> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        MappingEditor editor = new MappingEditor(pfxMgr);

        for ( String argument : arguments ) {
            String[] items = argument.split("=", 2);
            if ( items.length != 2 ) {
                throw new SSSOMTransformError(
                        "Invalid argument for function edit: expected \"key=value\" pair, found %s", argument);
            }

            try {
                editor.addSimpleAssign(items[0], items[1]);
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMTransformError("Invalid argument for function edit: %s", e.getMessage());
            }
        }

        return editor;
    }
}
