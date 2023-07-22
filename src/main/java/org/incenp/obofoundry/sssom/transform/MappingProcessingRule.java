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
}
