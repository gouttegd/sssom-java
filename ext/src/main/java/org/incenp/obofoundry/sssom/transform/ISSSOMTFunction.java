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

/**
 * Represents a function in a SSSOM/Transform application.
 * 
 * @param <T> The return type of the function.
 */
public interface ISSSOMTFunction<T> {

    /**
     * Gets the name of the function, by which it can be called in a SSSOM/T
     * ruleset.
     * 
     * @return The function name.
     */
    public String getName();

    /**
     * Gets the expected signature of the function. It should be a string that
     * represents how many arguments (excluding keyed arguments) the function is
     * expecting, where a single <code>S</code> character represents an argument.
     * <p>
     * Regular expression syntax may be used to represent arguments that are
     * optional or represent other kinds of constraints about the arguments list.
     * <p>
     * Examples:
     * <ul>
     * <li><code>SSS</code> for a function that expects exactly 3 arguments;
     * <li><code>S+</code> for a function that expects at least one argument,
     * possibly more;
     * <li><code>(SS)+</code> for a function that one or more pairs of arguments
     * (e.g. 2, 4, 6, but not 3 or 5).
     * </ul>
     * 
     * @return The function signature.
     */
    public String getSignature();

    /**
     * Executes the function.
     * <p>
     * Before this method is called, the arguments list will have been checked
     * against the signature and will be guaranteed to be correct (e.g. if the
     * function declared to expects two arguments, it is guaranteed the
     * {@code arguments} list will contains two items).
     * 
     * @param arguments      The arguments to the function. May be empty, but not
     *                       {@code null}.
     * @param keyedArguments The keyed arguments to the function. May be empty, but
     *                       {@code null}.
     * @return The result of executing the function.
     * @throws SSSOMTransformError If any error occurred when executing the function
     *                             (which may include the case where the
     *                             <em>number</em> of arguments was correct but
     *                             their <em>contents</em> was not).
     */
    public T call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError;
}
