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

package org.incenp.obofoundry.sssom.extract;

import org.incenp.obofoundry.sssom.SSSOMFormatException;

/**
 * An exception thrown when parsing an invalid “extractor expression”.
 */
public class ExtractorSyntaxException extends SSSOMFormatException {

    private static final long serialVersionUID = -2180645130483335354L;

    /**
     * Creates a new instance with a generic error message.
     */
    public ExtractorSyntaxException() {
        super("Invalid extractor expression");
    }

    /**
     * Creates a new instance with the specified error message.
     * 
     * @param msg A message describing the error;
     */
    public ExtractorSyntaxException(String msg) {
        super(msg);
    }

    /**
     * Creates a new instance with a constructed error message.
     * 
     * @param msg  The format string used to construct the error message.
     * @param args The arguments to use in the format string.
     */
    public ExtractorSyntaxException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
