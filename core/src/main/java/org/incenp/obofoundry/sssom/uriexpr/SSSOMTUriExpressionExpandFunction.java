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

package org.incenp.obofoundry.sssom.uriexpr;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.owl.SSSOMTOwlApplication;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T-OWL substitution modifier function "uriexpr_expand".
 * <p>
 * Use this function to turn a URI Expression into whatever expression has been
 * associated with the expression’s schema (through the
 * <code>uriexpr_declare_format</code> directive).
 * <p>
 * Example:
 * 
 * <pre>
 * declare_uriexpr_format(&lt;https://example.org/schema/0001&gt;, "(&lt;{field1}&gt; and &lt;{field2}&gt;)");
 * 
 * ... -&gt; create_axiom("%{subject_id|uriexpr_expand} EquivalentTo: &lt;%{object_id}&gt;");
 * </pre>
 */
public class SSSOMTUriExpressionExpandFunction implements ISSSOMTFunction<String> {

    private SSSOMTOwlApplication app;

    public SSSOMTUriExpressionExpandFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    @Override
    public String getName() {
        return "uriexpr_expand";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public String call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        UriExpression expr = UriExpression.parse(arguments.get(0), app.getPrefixManager());
        if ( expr == null ) {
            return arguments.get(0);
        }

        for ( String slotName : expr.getComponentNames() ) {
            app.getEntityChecker().addClass(expr.getComponent(slotName));
        }

        String text = app.getUriExpressionRegistry().applyTemplate(expr, "Manchester");
        return text != null ? text : arguments.get(0);
    }

}
