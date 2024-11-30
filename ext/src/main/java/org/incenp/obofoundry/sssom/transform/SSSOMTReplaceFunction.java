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
 * Represents the SSSOM/T standard preprocessor function "replace".
 * <p>
 * This function expects triplets of arguments where:
 * <ul>
 * <li>the first item is the name of a SSSOM metadata slot;
 * <li>the second item is the pattern to find a replace in the value of that
 * slot;
 * <li>the third item is the value the pattern should be replaced by.
 * </ul>
 */
public class SSSOMTReplaceFunction implements ISSSOMTFunction<IMappingTransformer<Mapping>> {

    private PrefixManager pfxMgr;

    /**
     * Creates a new instance.
     * 
     * @param <T>         The type of objects produced by the application.
     * @param application The application this function is a part of.
     */
    public <T> SSSOMTReplaceFunction(SSSOMTransformApplication<T> application) {
        pfxMgr = application.getPrefixManager();
    }

    @Override
    public String getName() {
        return "replace";
    }

    @Override
    public String getSignature() {
        return "(SSS)+";
    }

    @Override
    public IMappingTransformer<Mapping> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        MappingEditor editor = new MappingEditor(pfxMgr);

        int len = arguments.size();
        for ( int i = 0; i < len; i += 3 ) {
            try {
                editor.addReplacement(arguments.get(i), arguments.get(i + 1), arguments.get(i + 2));
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMTransformError("Invalid argument for function replace: %s", e.getMessage());
            }
        }

        return editor;
    }
}
