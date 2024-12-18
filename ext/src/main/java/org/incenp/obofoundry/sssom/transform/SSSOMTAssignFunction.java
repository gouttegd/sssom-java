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
 * Represents the SSSOM/T standard preprocessor function "assign".
 * <p>
 * That function expects pairs of arguments where the first item of the pair is
 * the name of a SSSOM metadata slot and the second item is the value to assign
 * to that slot.
 * <p>
 * The second argument may contain SSSOM/T placeholders referring to any SSSOM
 * metadata slots or any declared variables.
 */
public class SSSOMTAssignFunction implements ISSSOMTFunction<IMappingTransformer<Mapping>> {

    private PrefixManager pfxMgr;
    private MappingFormatter formatter;

    /**
     * Creates a new instance.
     * 
     * @param <T>         The type of objects produced by the application.
     * @param application The application this function is a part of.
     */
    public <T> SSSOMTAssignFunction(SSSOMTransformApplication<T> application) {
        pfxMgr = application.getPrefixManager();
        formatter = application.getFormatter();
    }

    @Override
    public String getName() {
        return "assign";
    }

    @Override
    public String getSignature() {
        return "(SS)+";
    }

    @Override
    public IMappingTransformer<Mapping> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        MappingEditor editor = new MappingEditor(pfxMgr);

        int len = arguments.size();
        for ( int i = 0; i < len; i += 2 ) {
            try {
                String slotName = arguments.get(i);
                String value = arguments.get(i + 1);
                if ( value.contains("%{") ) {
                    // It's not really worth trying to detect if that percent sign is escaped; if it
                    // is, and there is no actual placeholder in the value, then we are delaying for
                    // nothing, but it will still do the job.
                    editor.addDelayedAssign(slotName, formatter.getTransformer(value));
                } else {
                    editor.addSimpleAssign(slotName, value);
                }
            } catch ( IllegalArgumentException e ) {
                throw new SSSOMTransformError("Invalid argument for function assign: %s", e.getMessage());
            }
        }

        return editor;
    }
}
