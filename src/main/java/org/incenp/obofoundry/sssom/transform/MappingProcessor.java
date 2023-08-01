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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Helper object to apply processing rules to mappings.
 *
 * @param <T> The type of object that should be generated when applying a rule
 *            to a mapping.
 */
public class MappingProcessor<T> {

    private List<MappingProcessingRule<T>> rules = new ArrayList<MappingProcessingRule<T>>();
    private List<IMappingProcessorListener<T>> listeners = new ArrayList<IMappingProcessorListener<T>>();
    private Set<String> selectedTags;
    private boolean includeSelectedTags;

    /**
     * Adds a rule to be applied to mappings. The order in which rules are added is
     * significant. Rules are applied in the order in which they are added, and an
     * earlier rule may impact the behaviour of a later rule.
     * 
     * @param rule The new rule to add.
     */
    public void addRule(MappingProcessingRule<T> rule) {
        rules.add(rule);
    }

    /**
     * Adds a list of rules to be applied. This has the same effect as calling
     * {@link #addRule(MappingProcessingRule)} repeatedly.
     * 
     * @param rules The new rules to add.
     */
    public void addRules(List<MappingProcessingRule<T>> rules) {
        this.rules.addAll(rules);
    }

    /**
     * Adds a rule that stops any further processing for selected mappings.
     * 
     * @param filter The filter to determine whether the rule applies to a given
     *               mapping.
     */
    public void addStopingRule(IMappingFilter filter) {
        rules.add(new MappingProcessingRule<T>(filter, (mapping) -> null, null));
    }

    /**
     * Adds a listener to react to "generated" events, when an object is generated
     * by the application of a rule to a mapping.
     * 
     * @param listener The listener to add.
     */
    public void addGeneratedListener(IMappingProcessorListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Sets the processor to run only the rules that have at least one tag in the
     * specified tag set.
     * 
     * @param tags The tags to select the rules to run. Any rules with no matching
     *             tag will be excluded.
     */
    public void includeRules(Set<String> tags) {
        selectedTags = tags;
        includeSelectedTags = true;
    }

    /**
     * Sets the processor to exclude the rules that have at least one tag in the
     * specified tag set.
     * 
     * @param tags The tags to select the rules to exclude. Only the rules with no
     *             matching tag will be run.
     */
    public void excludeRules(Set<String> tags) {
        selectedTags = tags;
        includeSelectedTags = false;
    }

    /**
     * Applies all the rules to the given mappings.
     * 
     * @param mappings The mappings the rules should be applied to.
     * @return A list of all the objects that were produced by the application of
     *         the rules.
     */
    public List<T> process(List<Mapping> mappings) {
        List<T> products = new ArrayList<T>();

        List<MappingProcessingRule<T>> effectiveRules;
        if ( selectedTags != null ) {
            effectiveRules = new ArrayList<MappingProcessingRule<T>>();
            for ( MappingProcessingRule<T> rule : rules ) {
                boolean match = compareTags(selectedTags, rule.getTags());
                if ( (includeSelectedTags && match) || (!includeSelectedTags && !match) ) {
                    effectiveRules.add(rule);
                }
            }
        } else {
            effectiveRules = rules;
        }

        for ( Mapping mapping : mappings ) {
            for ( MappingProcessingRule<T> rule : effectiveRules ) {
                if ( mapping != null && rule.apply(mapping) ) {
                    mapping = rule.preprocess(mapping);
                    if ( mapping != null ) {
                        T product = rule.generate(mapping);
                        if ( product != null ) {
                            onGeneratedProduct(rule, mapping, product);
                            products.add(product);
                        }
                    }
                }
            }
        }

        return products;
    }

    private boolean compareTags(Set<String> a, Set<String> b) {
        Set<String> tmp = new HashSet<String>(a);
        tmp.retainAll(b);
        return tmp.size() > 0;
    }

    /**
     * Called when a rule generates an object from a mapping.
     * 
     * @param rule    The rule producing the object.
     * @param mapping The mapping the rule has been applied to.
     * @param product The object produced by the rule.
     */
    protected void onGeneratedProduct(MappingProcessingRule<T> rule, Mapping mapping, T product) {
        listeners.forEach((l) -> l.generated(rule, mapping, product));
    }
}
