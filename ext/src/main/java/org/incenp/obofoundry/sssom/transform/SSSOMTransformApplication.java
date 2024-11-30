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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.uriexpr.SSSOMTUriExpressionContainsFunction;
import org.incenp.obofoundry.sssom.uriexpr.SSSOMTUriExpressionSlotValueFunction;
import org.incenp.obofoundry.sssom.uriexpr.SSSOMTUriExpressionToExtFunction;

/**
 * Represents a SSSOM/Transform application, that is, a specialisation of the
 * SSSOM/Transform language (also called a “SSSOM/Transform dialect”) with its
 * own functions and that produces its own type of mapping-derived objects.
 * 
 * @param <T> The type of object that this application produces when the
 *            SSSOM/Transform ruleset is applied to mappings.
 */
public class SSSOMTransformApplication<T> implements ISSSOMTransformApplication<T> {

    private PrefixManager pfxMgr;
    private VariableManager varMgr = new VariableManager();
    private MappingFormatter formatter = new MappingFormatter();

    private Map<String, ISSSOMTFunction<IMappingFilter>> filters = new HashMap<String, ISSSOMTFunction<IMappingFilter>>();
    private Map<String, ISSSOMTFunction<Void>> directives = new HashMap<String, ISSSOMTFunction<Void>>();
    private Map<String, ISSSOMTFunction<IMappingProcessorCallback>> callbacks = new HashMap<String, ISSSOMTFunction<IMappingProcessorCallback>>();
    private Map<String, ISSSOMTFunction<IMappingTransformer<Mapping>>> preprocessors = new HashMap<String, ISSSOMTFunction<IMappingTransformer<Mapping>>>();
    private Map<String, ISSSOMTFunction<IMappingTransformer<T>>> generators = new HashMap<String, ISSSOMTFunction<IMappingTransformer<T>>>();

    /**
     * Gets the prefix manager used by this application. It will have been provided
     * by the SSSOM/Transform parser and will know of all the prefixes declared in
     * the header of the ruleset.
     * 
     * @return The prefix manager.
     */
    public PrefixManager getPrefixManager() {
        return pfxMgr;
    }

    /**
     * Gets the variable manager used by this application. Functions may use this
     * object to declare mapping-dependent variables and get their value for a given
     * mapping.
     * 
     * @return The variable manager.
     */
    public VariableManager getVariableManager() {
        return varMgr;
    }

    /**
     * Gets the mapping formatter used by this application. Functions may use this
     * object to expand placeholders found in their arguments. It may also be used
     * to define more substitutions.
     * <p>
     * The formatter is already configured to recognise placeholders corresponding
     * to the standard SSSOM metadata fields. For example,
     * <code>%{subject_id}</code> will be recognised and replaced by the subject ID
     * of the current mapping.
     * <p>
     * The formatter is also pre-configured with a <code>short</code> modifier to
     * substitute IRI fields into CURIEs. For example,
     * <code>%{subject_id|short}</code> will be replaced by the short form of the
     * mapping’s subject ID.
     * 
     * @return The mapping formatter.
     */
    public MappingFormatter getFormatter() {
        return formatter;
    }

    /**
     * Registers a new SSSOM/T filter function.
     * <p>
     * The function will be called when its name is found in the filter part of a
     * SSSOM/T rule. It should return a mapping filter.
     * 
     * @param function The function to register.
     */
    public void registerFilter(ISSSOMTFunction<IMappingFilter> function) {
        filters.put(function.getName(), function);
    }

    /**
     * Registers a new SSSOM/T directive function.
     * <p>
     * The function will be called when its name is found as the sole element of a
     * SSSOM/T rule (no filters).
     * 
     * @param function The function to register.
     */
    public void registerDirective(ISSSOMTFunction<Void> function) {
        directives.put(function.getName(), function);
    }

    /**
     * Registers a new SSSOM/T callback function.
     * <p>
     * The function will be called when its name is found in the action part of a
     * SSSOM/T rule, before looking up for a preprocessor or generator function. It
     * should return a processor callback.
     * 
     * @param function The function to register.
     */
    public void registerCallback(ISSSOMTFunction<IMappingProcessorCallback> function) {
        callbacks.put(function.getName(), function);
    }

    /**
     * Registers a new SSSOM/T preprocessor function.
     * <p>
     * The function will be called when its name is found in the action part of a
     * SSSOM/T rule, if no callback function with that name exists, before looking
     * up for a generator function. It should return a preprocessor, that is, a
     * transformer that produces mappings out of mappings.
     * 
     * @param function The function to register.
     */
    public void registerPreprocessor(ISSSOMTFunction<IMappingTransformer<Mapping>> function) {
        preprocessors.put(function.getName(), function);
    }

    /**
     * Registers a new SSSOM/T generator function.
     * <p>
     * The function will be called when its name is found in the action part of a
     * SSSOM/T rule, if no callback function and no preprocessor function with that
     * name exist. It should return a transformer that produces the desired type of
     * objects from mappings.
     * 
     * @param function The function to register.
     */
    public void registerGenerator(ISSSOMTFunction<IMappingTransformer<T>> function) {
        generators.put(function.getName(), function);
    }

    @Override
    public void onInit(PrefixManager prefixManager) {
        pfxMgr = prefixManager;

        formatter.setPrefixManager(prefixManager);
        formatter.setStandardSubstitutions();
        formatter.setModifier(new SSSOMTShortFunction(pfxMgr));
        formatter.setModifier(new SSSOMTFormatFunction());
        formatter.setModifier(new SSSOMTListItemFunction());
        formatter.setModifier(new SSSOMTFlattenFunction());

        registerDirective(new SSSOMTSetvarFunction(this));
        registerCallback(new SSSOMTSetvarCallbackFunction(this));
        registerPreprocessor(new SSSOMTStopFunction());
        registerPreprocessor(new SSSOMTInvertFunction());
        registerPreprocessor(new SSSOMTAssignFunction(this));
        registerPreprocessor(new SSSOMTReplaceFunction(this));
        registerPreprocessor(new SSSOMTEditFunction(this));

        // Enable support for URI Expressions
        registerFilter(new SSSOMTUriExpressionContainsFunction<T>(this));
        registerPreprocessor(new SSSOMTUriExpressionToExtFunction<T>(this));
        formatter.setModifier(new SSSOMTUriExpressionSlotValueFunction(pfxMgr));
    }

    @Override
    public IMappingFilter onFilter(String name, List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return callFunction(name, filters, arguments, keyedArguments);
    }

    @Override
    public boolean onDirectiveAction(String name, List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        // Directive functions return Void, so we can't use their return value to check
        // whether a call was made. We need to check beforehand whether the function is
        // known.
        if ( !directives.containsKey(name) ) {
            return false;
        }
        callFunction(name, directives, arguments, keyedArguments);
        return true;
    }

    @Override
    public IMappingProcessorCallback onCallback(String name, List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return callFunction(name, callbacks, arguments, keyedArguments);
    }

    @Override
    public IMappingTransformer<Mapping> onPreprocessingAction(String name, List<String> arguments,
            Map<String, String> keyedArguments) throws SSSOMTransformError {
        return callFunction(name, preprocessors, arguments, keyedArguments);
    }

    @Override
    public IMappingTransformer<T> onGeneratingAction(String name, List<String> arguments,
            Map<String, String> keyedArguments) throws SSSOMTransformError {
        return callFunction(name, generators, arguments, keyedArguments);
    }

    private <V> V callFunction(String name, Map<String, ISSSOMTFunction<V>> functions, List<String> arguments,
            Map<String, String> keyedArguments) throws SSSOMTransformError {
        ISSSOMTFunction<V> function = functions.get(name);
        if ( function != null ) {
            // Turn the arguments list into a string with one 'S' per argument
            StringBuilder sb = new StringBuilder();
            int len = arguments.size();
            for ( int i = 0; i < len; i++ ) {
                sb.append('S');
            }
            if ( !Pattern.matches(function.getSignature(), sb) ) {
                throw new SSSOMTransformError("Invalid call for function %s", name);
            }

            return function.call(arguments, keyedArguments);
        }

        return null;
    }
}
