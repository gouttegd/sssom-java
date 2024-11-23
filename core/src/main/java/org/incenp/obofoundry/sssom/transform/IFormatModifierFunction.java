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

/**
 * Represents a SSSOM/T format modifier function.
 * <p>
 * Such a function may be called in a placeholder inside a format string, after
 * the placeholder name, as follows:
 * </p>
 * 
 * <pre>
 * %{placeholder|modifier_function(extra_arg1)}
 * </pre>
 */
public interface IFormatModifierFunction {

    /**
     * Gets the name by which the function should be called in a placeholder.
     * 
     * @return The function name.
     */
    public String getName();

    /**
     * Gets the expected signature of the function. This works similarly to
     * {@link ISSSOMTFunction#getSignature()}, but it only concerns the additional
     * arguments beyond the value of the substituted placeholder to modify (e.g.
     * <code>extra_arg1</code> in the example above).
     * 
     * @return The function signature.
     */
    public String getSignature();

    /**
     * Executes the function.
     * 
     * @param value The original value this function is supposed to modify. It is
     *              guaranteed never to be {@code null}, but the exact type will
     *              depend on what the substituted placeholder was, and also on what
     *              any previous modifier function may have done with it.
     * @param extra Additional arguments to the function, if any. If the function
     *              declared that it expects some mandatory arguments (through the
     *              {@link #getSignature()} method), it is guaranteed the list will
     *              contains as many arguments as expected.
     * @return The modified value. It may not need be of the same type as the
     *         original value (e.g., a function could transform a list value into a
     *         string value).
     */
    public Object call(Object value, List<String> extra);
}
