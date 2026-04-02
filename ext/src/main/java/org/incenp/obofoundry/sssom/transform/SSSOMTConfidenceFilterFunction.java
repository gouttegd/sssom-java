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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.util.ConfidenceHelper;

/**
 * Represents the SSSOM/T filter function "confidence".
 * <p>
 * That function may be used to select mappings depending on their
 * <em>aggregated confidence</em>. The aggregated confidence is a single value
 * that reflects both the confidence of the mapping’s creators (as found in the
 * <code>confidence</code> slot) and the agreement of the mapping’s reviewer (as
 * found in the <code>reviewer_agreement</code> slot).
 * <p>
 * The function takes a single argument which is the threshold value to filter
 * against. By default, the function will select mappings whose aggregated
 * confidence is <em>higher</em> than the threshold. To select mappings with a
 * <em>lower</em> aggregated confidence, prefix the threshold value with
 * <code>&lt;</code>.
 * <p>
 * For example, to select mappings with an aggregated confidence below 0.5:
 * 
 * <pre>
 * confidence("&lt;0.5") -&gt; ...;
 * </pre>
 */
public class SSSOMTConfidenceFilterFunction implements IMappingFilter, ISSSOMTFunction<IMappingFilter> {

    private ConfidenceHelper helper;
    private double threshold;
    private boolean lowPass;

    /**
     * Creates a new instance.
     */
    public SSSOMTConfidenceFilterFunction() {
    }

    private SSSOMTConfidenceFilterFunction(double threshold, boolean lowPass) {
        this.threshold = threshold;
        this.lowPass = lowPass;
        helper = new ConfidenceHelper();
    }

    @Override
    public String getName() {
        return "confidence";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public IMappingFilter call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        String arg = arguments.get(0);
        boolean lowPass = false;
        if ( arg.startsWith("<") ) {
            lowPass = true;
            arg = arg.substring(1);
        } else if ( arg.startsWith(">") ) {
            arg = arg.substring(1);
        }
        try {
            double threshold = Double.valueOf(arg);
            return new SSSOMTConfidenceFilterFunction(threshold, lowPass);
        } catch ( NumberFormatException e ) {
            throw new SSSOMTransformError("Invalid argument for confidence_filter function: %s", arguments.get(0));
        }
    }

    @Override
    public boolean filter(Mapping mapping) {
        double agg = helper.aggregate(mapping);
        if ( lowPass ) {
            return agg < threshold;
        } else {
            return agg > threshold;
        }
    }
}
