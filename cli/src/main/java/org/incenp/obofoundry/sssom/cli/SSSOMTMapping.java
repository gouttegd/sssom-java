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

package org.incenp.obofoundry.sssom.cli;

import java.util.List;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplicationBase;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * A specialised application of the SSSOM/Transform language to read mapping
 * processing rules that produce mappings from mappings.
 * <p>
 * This application recognises the following actions:
 * <ul>
 * <li>{@code stop()} to stop any further processing for the current mapping;
 * <li>{@code invert()} to invert the current mapping;
 * <li>{@code include()} to produce the current mapping as it is.
 * </ul>
 */
public class SSSOMTMapping extends SSSOMTransformApplicationBase<Mapping> {

    @Override
    public IMappingTransformer<Mapping> onGeneratingAction(String name, List<String> arguments)
            throws SSSOMTransformError {
        if ( name.equals("include") ) {
            return (mapping) -> mapping;
        }
        return super.onGeneratingAction(name, arguments);
    }

}
