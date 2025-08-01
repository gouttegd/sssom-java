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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.Cardinalizer;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents the SSSOM/T standard callback function “infer_cardinality”.
 * <p>
 * This function computes the cardinality for all the mappings it is applied to.
 * By default, the cardinality is computed relatively to all the mappings the
 * function is applied to; to compute cardinality relatively to smaller subsets,
 * specify the slots to use as scope as arguments to the function.
 * <p>
 * For example, to compute cardinality relatively to the subset of mappings that
 * have the same predicate and the same object source:
 * 
 * <pre>
 * ... -> infer_cardinality("predicate_id", "object_source");
 * </pre>
 */
public class SSSOMTInferCardinalityFunction
        implements ISSSOMTFunction<IMappingProcessorCallback>, IMappingProcessorCallback {

    private Cardinalizer cardinalizer;

    public SSSOMTInferCardinalityFunction() {
    }

    private SSSOMTInferCardinalityFunction(List<String> slots) {
        cardinalizer = new Cardinalizer(slots);
    }

    @Override
    public String getName() {
        return "infer_cardinality";
    }

    @Override
    public String getSignature() {
        return "S*";
    }

    @Override
    public IMappingProcessorCallback call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return new SSSOMTInferCardinalityFunction(arguments);
    }

    @Override
    public void process(IMappingFilter filter, List<Mapping> mappings) {
        List<Mapping> subset = mappings;
        if ( !filter.toString().equals("(*)") ) {
            subset = new ArrayList<>();
            for ( Mapping mapping : mappings ) {
                if ( filter.filter(mapping) ) {
                    subset.add(mapping);
                }
            }
        }
        cardinalizer.fillCardinality(subset);
    }
}
