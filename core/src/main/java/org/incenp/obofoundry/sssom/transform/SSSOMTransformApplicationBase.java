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

import java.util.List;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.CommonPredicate;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A base implementation of the {@link ISSSOMTransformApplication} interface.
 * Applications can extend this class instead of implementing the interface
 * directly.
 * <p>
 * This implementation recognises two preprocessing actions:
 * <ul>
 * <li>{@code stop()}, to stop any further processing for the current mapping;
 * <li>{@code invert()}, to invert the current mapping.
 * </ul>
 *
 * @param <T> The type of objects that should be produced by the
 *            SSSOM/Transform-described processing rules.
 */
public class SSSOMTransformApplicationBase<T> implements ISSSOMTransformApplication<T> {

    protected PrefixManager pm = null;

    @Override
    public void onInit(PrefixManager prefixManager) {
        pm = prefixManager;
    }

    @Override
    public void onHeaderAction(String name, List<String> arguments) throws SSSOMTransformError {
        throw new SSSOMTransformError(String.format("Unrecognised function: %s", name));
    }

    @Override
    public IMappingTransformer<Mapping> onPreprocessingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        switch ( name ) {
        case "stop":
            checkArguments(name, 0, arguments);
            return new NamedMappingTransformer<Mapping>("stop()", (mapping) -> null);

        case "invert":
            checkArguments(name, 0, arguments);
            return new NamedMappingTransformer<Mapping>("invert()", (mapping) -> CommonPredicate.invert(mapping));

        case "edit":
            checkArguments(name, 1, arguments, true);
            MappingEditor editEditor = new MappingEditor(pm);
            for (String argument : arguments) {
                String[] items = argument.split("=", 2);
                if (items.length != 2) {
                    throw new SSSOMTransformError(String.format(
                            "Invalid argument for function edit: expected \"key=value\" pair, found \"%\"", argument));
                }
                try {
                    editEditor.addSimpleAssign(items[0], items[1]);
                } catch ( IllegalArgumentException iae ) {
                    throw new SSSOMTransformError(
                            String.format("Invalid argument for function edit: %s", iae.getMessage()));
                }
            }
            return new NamedMappingTransformer<Mapping>("edit()", editEditor);

        case "assign":
            if ( arguments.size() % 2 != 0 ) {
                throw new SSSOMTransformError(String.format(
                        "Invalid number of arguments for function assign: expected multiple of 2, found %d",
                        arguments.size()));
            }
            MappingEditor assignEditor = new MappingEditor();
            for ( int i = 0; i < arguments.size(); i += 2 ) {
                assignEditor.addSimpleAssign(arguments.get(i), arguments.get(i + 1));
            }
            return new NamedMappingTransformer<Mapping>("assign()", assignEditor);

        case "replace":
            if ( arguments.size() % 3 != 0 ) {
                throw new SSSOMTransformError(String.format(
                        "Invalid number of arguments for function replace: expected multiple of 3, found %d",
                        arguments.size()));
            }
            MappingEditor replaceEditor = new MappingEditor(pm);
            for ( int i = 0; i < arguments.size(); i += 3 ) {
                replaceEditor.addReplacement(arguments.get(i), arguments.get(i + 1), arguments.get(i + 2));
            }
            return new NamedMappingTransformer<Mapping>("replace()", replaceEditor);
        }
        return null;
    }

    @Override
    public IMappingTransformer<T> onGeneratingAction(String name, List<String> arguments) throws SSSOMTransformError {
        throw new SSSOMTransformError(String.format("Unrecognised function: %s", name));
    }

    @Override
    public String getCurieExpansionFormat() {
        return null;
    }

    protected void checkArguments(String name, int expected, List<String> arguments) throws SSSOMTransformError {
        checkArguments(name, expected, arguments, false);
    }

    protected void checkArguments(String name, int expected, List<String> arguments, boolean min) throws SSSOMTransformError {
        if ( (min && arguments.size() < expected) || (!min && arguments.size() != expected) ) {
            throw new SSSOMTransformError(
                    String.format("Invalid number of arguments for function %s: expected %d, found %d", name, expected,
                            arguments.size()));
        }
    }
}
