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

import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T-OWL "declare_class" function.
 * <p>
 * This function may be used to declare OWL classes to make sure they are known
 * to the SSSOM/T-OWL application, before they can be used in
 * {@code create_axiom}.
 * 
 * @deprecated Maintained for backwards compatibility only; use
 *             <code>declare(CLASS[,...])</code> instead.
 */
@Deprecated
public class SSSOMTDeclareClassFunction implements ISSSOMTFunction<Void> {

    private SSSOMTOwlApplication app;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application object.
     */
    public SSSOMTDeclareClassFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    @Override
    public String getName() {
        return "declare_class";
    }

    @Override
    public String getSignature() {
        return "S+";
    }

    @Override
    public Void call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        arguments.forEach((c) -> app.getEntityChecker().addClass(c));
        return null;
    }

}
