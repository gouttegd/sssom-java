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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.transform;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents the SSSOM/T filter function "has_extension".
 * <p>
 * This function may be used to select mappings that have a particular extension
 * property.
 * <p>
 * For example, to select mappings that have an extension associated with the
 * property {@code https://example.org/properties/fooProperty} (assuming the
 * prefix name {@code PROP} has been declared to correspond to the
 * {@code https://example.org/properties/} prefix):
 * 
 * <pre>
 * has_extension(PROP:fooProperty) -&gt; ...;
 * </pre>
 */
public class SSSOMTHasExtensionFunction implements ISSSOMTFunction<IMappingFilter>, IMappingFilter {

    private String targetExtension;

    public SSSOMTHasExtensionFunction() {
    }

    private SSSOMTHasExtensionFunction(String targetExtension) {
        this.targetExtension = targetExtension;
    }

    @Override
    public String getName() {
        return "has_extension";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public IMappingFilter call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        return new SSSOMTHasExtensionFunction(arguments.get(0));
    }

    @Override
    public boolean filter(Mapping mapping) {
        Map<String, ExtensionValue> map = mapping.getExtensions();
        if ( map == null ) {
            return false;
        }
        ExtensionValue ev = map.get(targetExtension);
        if ( ev == null || ev.getValue() == null ) {
            return false;
        }
        return true;
    }
}
