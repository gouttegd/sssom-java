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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.cli;

import org.incenp.obofoundry.sssom.model.Version;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * Converts an string option value into a SSSOM version.
 */
public class VersionConverter implements ITypeConverter<Version> {

    @Override
    public Version convert(String value) throws Exception {
        Version version = null;
        if ( value.equalsIgnoreCase("latest") ) {
            version = Version.LATEST;
        } else if ( (version = Version.fromString(value)) == Version.UNKNOWN ) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            sb.append(" is not a recognised SSSOM version; allowed values: ");
            for ( Version v : Version.values() ) {
                if ( v != Version.UNKNOWN ) {
                    sb.append(v.toString());
                    sb.append(", ");
                }
            }
            sb.append("latest.");
            throw new TypeConversionException(sb.toString());
        }
        return version;
    }

}
