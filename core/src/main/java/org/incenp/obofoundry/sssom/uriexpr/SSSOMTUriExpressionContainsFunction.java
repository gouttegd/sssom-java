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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplication;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T filter function "uriexpr_contains".
 * <p>
 * That function allows the filtering of mappings depending on the contents of a
 * “URI Expression”. It takes (at least) three arguments:
 * <ul>
 * <li>the Expression URI to look into (typically provided as
 * <code>%subject_id</code> or <code>%object_id</code>);
 * <li>the name of a URI Expression slot;
 * <li>the expected value of that slot.
 * </ul>
 * <p>
 * A mapping will be selected if (1) the first argument is a valid URI
 * Expression, and (2) it contains a slot with a name matching the second
 * argument, and (3) that slot has the expected value. Otherwise, the mapping
 * will be rejected.
 * <p>
 * The first argument may contain standard SSSOM/T placeholders (and that is
 * typically expected).
 * <p>
 * Matching the expected value with the actual value of the slot follows the
 * same rule as all other ID comparisons in SSSOM/T, in that the expected value
 * may end with a '*' character to test whether the actual value starts with the
 * same prefix as the expected value.
 * <p>
 * The function may take additional pairs of arguments to check several slots in
 * the same call.
 * <p>
 * Example:
 * 
 * <pre>
 * uriexpr_contains(%{subject_id}, 'disease', MONDO:1234, 'phenotype', HP:*) -&gt; ...;
 * </pre>
 * <p>
 * This will select any mapping whose subject ID is a URI Expression containing
 * a <em>disease</em> slot with the value <code>MONDO:1234</code> and a
 * <code>phenotype</code> slot with any value starting with the HP prefix.
 * 
 * @param <T> The type of object produced by the application this function
 *            belongs to.
 */
public class SSSOMTUriExpressionContainsFunction<T> implements ISSSOMTFunction<IMappingFilter>, IMappingFilter {

    private SSSOMTransformApplication<T> app;
    private IMappingTransformer<String> uri;
    private Map<String, String> targets;
    
    /**
     * Creates a new instance.
     * 
     * @param application The application this function belongs to.
     */
    public SSSOMTUriExpressionContainsFunction(SSSOMTransformApplication<T> application) {
        app = application;
    }

    private SSSOMTUriExpressionContainsFunction(SSSOMTransformApplication<T> application,
            IMappingTransformer<String> uri, Map<String, String> targets) {
        app = application;
        this.uri = uri;
        this.targets = targets;
    }

    @Override
    public String getName() {
        return "uriexpr_contains";
    }

    @Override
    public String getSignature() {
        return "S(SS)+";
    }

    @Override
    public IMappingFilter call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        IMappingTransformer<String> uri = app.getFormatter().getTransformer(arguments.get(0));
        Map<String, String> toFind = new HashMap<String, String>();
        int len = arguments.size();
        for ( int i = 1; i < len; i += 2 ) {
            toFind.put(arguments.get(i), arguments.get(i + 1));
        }
        return new SSSOMTUriExpressionContainsFunction<T>(app, uri, toFind);
    }

    @Override
    public boolean filter(Mapping mapping) {
        UriExpression expr = UriExpression.parse(uri.transform(mapping), app.getPrefixManager());
        if ( expr != null ) {
            for ( String key : targets.keySet() ) {
                String expected = targets.get(key);
                String actual = expr.getComponent(key);

                if ( actual == null ) {
                    // Expression does not contain the value at all
                    return false;
                }

                int len = expected.length();
                if ( len > 0 && expected.charAt(len - 1) == '*' ) {
                    if ( !actual.startsWith(expected.substring(0, len - 1)) ) {
                        return false;
                    }
                } else if ( !actual.equals(expected) ) {
                    return false;
                }
            }

            // All expected fields found with the expected values
            return true;
        }

        // Not a URI Expression
        return false;
    }

}
