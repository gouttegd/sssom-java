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
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents a specialised application of the SSSOM/Transform language. An
 * implementation of that interface provides the SSSOM/Transform parser with the
 * necessary informations to fully parse a SSSOM/Transform file into a set of
 * mapping processing rules.
 *
 * @param <T> The type of object that should be produced by the
 *            SSSOM/Transform-described processing rules.
 */
public interface ISSSOMTransformApplication<T> {

    /**
     * Initialises the application. This method is called by the SSSOM/Transform
     * parser when all rules have been syntaxically parsed, before the parser
     * attempts to process the action part of each rule.
     * 
     * @param prefixManager A prefix manager initialised with all the prefix
     *                      declarations found in the SSSOM/Transform file.
     */
    public void onInit(PrefixManager prefixManager);

    /**
     * Processes a header action. This method is called when the parser finds a
     * “header action”, that is an action that is not associated to any filter. Such
     * action cannot produce mapping processing rules but may otherwise influence
     * the behaviour of the application.
     * 
     * @param name      The name of the function.
     * @param arguments The list of arguments passed to the function.
     * @throws SSSOMTransformError If the application cannot process the action
     *                             (e.g. the name is not recognised or the arguments
     *                             are invalid).
     */
    public void onHeaderAction(String name, List<String> arguments) throws SSSOMTransformError;

    /**
     * Processes a preprocessing action. This method is called when the parser finds
     * a normal action (any action associated with a filter). It the application
     * recognises the function, it must return a mapping preprocessor; otherwise, it
     * must return {@code null}.
     * 
     * @param name      The name of the function.
     * @param arguments The list of arguments passed to the function.
     * @return A mapping preprocessor implementing the action according to the
     *         application’s needs, or {@code null} if the name is not a valid
     *         preprocessing function name for this application.
     * @throws SSSOMTransformError If the application cannot process the action
     *                             (e.g., the arguments are invalid).
     */
    public IMappingTransformer<Mapping> onPreprocessingAction(String name, List<String> arguments)
            throws SSSOMTransformError;

    /**
     * Processes a generating action. This method is called when the parser finds a
     * normal action that is not recognised as a preprocessing action (i.e.
     * {@link #onPreprocessingAction(String, List)} returned {@code null}). If the
     * application recognises the function, it must return a mapping transformer
     * that produces the kind of objects desired by the application.
     * 
     * @param name      The name of the function.
     * @param arguments The list of arguments passed to the function.
     * @return A mapping transformer implementing the action according to the
     *         application’s needs.
     * @throws SSSOMTransformError If the application cannot process the action
     *                             (e.g. the name is not recognised or the arguments
     *                             are invalid).
     */
    public IMappingTransformer<T> onGeneratingAction(String name, List<String> arguments) throws SSSOMTransformError;
}
