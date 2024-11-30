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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplication;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T preprocessor function "uriexpr_toext".
 * <p>
 * That functions allows to store the components (slots) of a URI Expression
 * into a mapping’s extension slots. It takes a single argument, which is the
 * URI Expression to encode. Placeholders are expanded in that argument.
 * <p>
 * The value of each slot is stored into a SSSOM extension slot whose property
 * name is formed by concatenating the name of the URI Expression schema and the
 * name of the slot.
 * <p>
 * If the argument does not happen to contain a URI Expression, the original
 * mapping is returned unmodified.
 * 
 * @param <T> The type of objects produced by the application this function
 *            belongs to.
 */
public class SSSOMTUriExpressionToExtFunction<T>
        implements ISSSOMTFunction<IMappingTransformer<Mapping>>, IMappingTransformer<Mapping> {

    private SSSOMTransformApplication<T> app;

    private IMappingTransformer<String> exprSource;

    /**
     * Creates a new instance.
     * 
     * @param application The application this function belongs to.
     */
    public SSSOMTUriExpressionToExtFunction(SSSOMTransformApplication<T> application) {
        app = application;
    }

    private SSSOMTUriExpressionToExtFunction(SSSOMTransformApplication<T> application,
            IMappingTransformer<String> exprSource) {
        app = application;
        this.exprSource = exprSource;
    }

    @Override
    public String getName() {
        return "uriexpr_toext";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public IMappingTransformer<Mapping> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return new SSSOMTUriExpressionToExtFunction<T>(app, app.getFormatter().getTransformer(arguments.get(0)));
    }

    @Override
    public Mapping transform(Mapping mapping) {
        UriExpression expr = UriExpression.parse(exprSource.transform(mapping), app.getPrefixManager());
        if ( expr == null ) {
            return mapping;
        }

        Mapping m = mapping.toBuilder().build();
        if ( m.getExtensions() == null ) {
            m.setExtensions(new HashMap<String, ExtensionValue>());
        }

        for ( String slotName : expr.getComponentNames() ) {
            String value = app.getPrefixManager().expandIdentifier(expr.getComponent(slotName));

            ExtensionValue extValue = new ExtensionValue(value, true);
            String propertyName = expr.getSchema() + "/" + slotName;
            m.getExtensions().put(propertyName, extValue);
        }

        return m;
    }
}
