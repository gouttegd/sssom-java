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

import java.util.List;
import java.util.function.Function;

/**
 * Represents a simple format modifier function that does not take any argument.
 * <p>
 * This class is intended to allow creating simple modifiers without having to
 * create a new dedicated class. For example, to create a modifier that turns
 * its input into lowercase:
 * 
 * <pre>
 * new SimpleStringModifierFunction("lower", (input) -&gt; input.toLowerCase());
 * </pre>
 */
public class SimpleStringModifierFunction extends BaseStringModifierFunction {

    private String name;
    private Function<String, String> func;

    /**
     * Creates a new instance.
     * 
     * @param name The name of the modifier function to create.
     * @param func The implementation of the function.
     */
    public SimpleStringModifierFunction(String name, Function<String, String> func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSignature() {
        return "";
    }

    @Override
    protected String apply(Object value, List<String> extra) {
        return func.apply(value.toString());
    }

}
