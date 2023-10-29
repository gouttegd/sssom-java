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

package org.incenp.obofoundry.sssom;

import java.util.Map;

/**
 * An interface for applying arbitrary treatments to a YAML-derived dictionary.
 * This interface is intended to be used for updating the YAML-derived
 * dictionary representing a SSSOM object in order to support old versions of
 * the SSSOM specification. Implementations can take a dictionary that is
 * conforming to an old version of the specification and transform it so that it
 * is conforming to the current version.
 */
public interface IYAMLPreprocessor {

    /**
     * Apply a treatment to the specified dictionary.
     * 
     * @param rawMap The dictionary to process.
     * @throws SSSOMFormatException If the contents of the dictionary does not match
     *                              the preprocessor's expectations.
     */
    public void process(Map<String, Object> rawMap) throws SSSOMFormatException;
}
