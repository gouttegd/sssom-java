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

import org.incenp.obofoundry.sssom.PrefixManager;

/**
 * Parser for the application-specific instructions of the SSSOM Transform
 * language. An implementation of that interface takes the contents of a
 * {@code gen} instruction and produces the appropriate
 * {@link IMappingTransformer} object to transform mappings into the desired
 * object type.
 * 
 * @param <T> The type of object that should be produced from a mapping.
 */
public interface ITransformationParser<T> {

    /**
     * Parses a {@code gen} instruction into a mapping transformer object.
     * 
     * @param text          The instruction to parse.
     * @param prefixManager A prefix manager that may be used to expand/shorten
     *                      identifiers within the instruction to parse.
     * @return The mapping transformer.
     */
    public IMappingTransformer<T> parse(String text, PrefixManager prefixManager);
}
