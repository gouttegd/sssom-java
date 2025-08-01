/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * This class represents an arbitrary treatment applied to a mapping.
 * <p>
 * A processing rule is made of:
 * <ul>
 * <li>a filter, to decide whether the rule should be applied to a given
 * mapping;</li>
 * <li>a preprocessor, to modify the mapping the rule is applied to;</li>
 * <li>a generator, to produce an object from the mapping.</li>
 * </ul>
 * 
 * @param <T> The type of object that should be produced by the generator from a
 *            mapping.
 */
public class MappingProcessingRule<T> {

    private IMappingFilter filter;
    private IMappingTransformer<Mapping> preprocessor;
    private IMappingTransformer<T> generator;
    private IMappingProcessorCallback callback;
    private HashSet<String> tags = null;

    /**
     * Creates a new instance.
     * 
     * @param filter       The filter to select the mappings this rule will be
     *                     applied to; if the filter returns {@code true} for a
     *                     given mapping, the rule is applied. If {@code null}, the
     *                     rule is applied to any mapping.
     * @param preprocessor The preprocessor to modify the mapping; it takes a
     *                     mapping and returns another mapping; if {@code null}, the
     *                     mapping is unmodified.
     * @param generator    The generator to produce the desired object from the
     *                     mapping; if {@code null}, the rule will produce
     *                     {@code null}.
     */
    public MappingProcessingRule(IMappingFilter filter, IMappingTransformer<Mapping> preprocessor,
            IMappingTransformer<T> generator) {
        this.filter = filter;
        this.preprocessor = preprocessor;
        this.generator = generator;
    }

    /**
     * Creates a new instance that includes a custom processing step.
     * 
     * @param filter       The filter to select the mappings this rule will be
     *                     applied to; if the filter returns {@code true} for a
     *                     given mapping, the rule is applied. If {@code null}, the
     *                     rule is applied to any mapping.
     * @param preprocessor The preprocessor to modify the mapping; it takes a
     *                     mapping and returns another mapping; if {@code null}, the
     *                     mapping is unmodified.
     * @param generator    The generator to produce the desired object from the
     *                     mapping; if {@code null}, the rule will produce
     *                     {@code null}.
     * @param callback     A callback method to call when this rule is being
     *                     processed; if set, the callback will be called before the
     *                     processor starts iterating over the mappings.
     */
    public MappingProcessingRule(IMappingFilter filter, IMappingTransformer<Mapping> preprocessor,
            IMappingTransformer<T> generator, IMappingProcessorCallback callback) {
        this.filter = filter;
        this.preprocessor = preprocessor;
        this.generator = generator;
        this.callback = callback;
    }

    /**
     * Indicates whether this rule makes use of cardinality information.
     * 
     * @return {@code true} if the rule needs accurate cardinality information,
     *         {@code false} otherwise.
     */
    public boolean needsCardinality() {
        // TODO: There should be a better way to do this.
        return filter != null && filter.toString().contains("cardinality==");
    }

    /**
     * Indicates whether this rule takes care of computing cardinality information.
     * 
     * @return {@code true} if the rule infer cardinality values, {@code false}
     *         otherwise.
     */
    public boolean doesInferCardinality() {
        // TODO: There should be a better way to do this.
        return callback != null && callback instanceof SSSOMTInferCardinalityFunction;
    }

    /**
     * Gets the tags associated with the rule.
     * 
     * @return The set of tags for this rule.
     */
    public Set<String> getTags() {
        if ( tags == null ) {
            tags = new HashSet<String>();
        }

        return tags;
    }

    /**
     * Checks whether the rule should be applied to the given mapping.
     * 
     * @param mapping The mapping to check.
     * @return {@code true} if the rule applies to the mapping, otherwise
     *         {@code false}; if no filter has been set, always {@code true}.
     */
    public boolean apply(Mapping mapping) {
        return filter != null ? filter.filter(mapping) : true;
    }

    /**
     * Applies the preprocessing step to the given mapping.
     * 
     * @param mapping The mapping to preprocess.
     * @return The preprocessed mapping; if no preprocessor has been set, the
     *         original, unmodified mapping.
     */
    public Mapping preprocess(Mapping mapping) {
        if ( preprocessor != null ) {
            return preprocessor.transform(mapping);
        }

        return mapping;
    }

    /**
     * Generates the output object from the given mapping.
     * 
     * @param mapping The mapping to generate an object from.
     * @return The object derived from the mapping by application of the rule;
     *         always {@code null} if no generator has been set.
     */
    public T generate(Mapping mapping) {
        if ( generator != null ) {
            return generator.transform(mapping);
        }

        return null;
    }

    /**
     * Calls the custom callback associated with the rule, if any.
     * 
     * @param mappings The current set of mappings.
     * @return {@code true} if the processor should still proceed with that rule
     *         (i.e. iterate over the mappings and apply the preprocessor and
     *         generator as needed), or {@code false} if it should proceed to the
     *         next rule instead.
     */
    public boolean call(List<Mapping> mappings) {
        if ( callback != null ) {
            callback.process(filter, mappings);
        }

        return preprocessor != null || generator != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ( tags != null && !tags.isEmpty() ) {
            sb.append("[");
            sb.append(String.join(",", tags));
            sb.append("] ");
        }

        if ( filter != null ) {
            sb.append(filter.toString());
        } else {
            sb.append("*");
        }

        if ( preprocessor != null ) {
            sb.append(" -> ");
            sb.append(preprocessor.toString());
        }

        if ( generator != null ) {
            sb.append(" -> ");
            sb.append(generator.toString());
        }

        if ( callback != null ) {
            sb.append(" -> ");
            sb.append(callback.toString());
        }

        return sb.toString();
    }
}
