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
 * Represents the SSSOM/T-OWL directive function "uriexpr_declare_format".
 * <p>
 * Use this function to associate an expansion template to a URI Expression
 * schema. For example:
 * 
 * <pre>
 * uriexpr_declare_format(&lt;http://example.org/schema/0001&gt;, "(&lt;{field1}&gt; and &lt;{field2}&gt;)");
 * </pre>
 * <p>
 * This associates to the schema called
 * <code>http://example.org/schema/0001</code> a template that turns the URI
 * Expression into a OWL ObjectIntersectionOf expression between the values of
 * the <em>field1</em> and <em>field2</em> slots (which are assumed to be valid
 * slots in that schema).
 */
public class SSSOMTUriExpressionDeclareFormatFunction implements ISSSOMTFunction<Void> {

    private SSSOMTOwlApplication app;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application this function belongs to.
     */
    public SSSOMTUriExpressionDeclareFormatFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    @Override
    public String getName() {
        return "uriexpr_declare_format";
    }

    @Override
    public String getSignature() {
        return "SS";
    }

    @Override
    public Void call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        app.getUriExpressionRegistry().registerTemplate(arguments.get(0), "Manchester", arguments.get(1));
        return null;
    }

}
