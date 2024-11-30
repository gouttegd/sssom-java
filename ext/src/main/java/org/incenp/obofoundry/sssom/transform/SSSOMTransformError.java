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

/**
 * A syntax error encountered when parsing a SSSOM/T ruleset.
 */
public class SSSOMTransformError extends Exception {
    private static final long serialVersionUID = -7704754550392932287L;

    /**
     * Creates a new instance with an error message from the ANTLR parser.
     * 
     * @param line    The line number where the error occurred.
     * @param column  The position in the line where the error occurred.
     * @param message The error message from the ANTLR parser.
     */
    public SSSOMTransformError(int line, int column, String message) {
        super(String.format("SSSOM/Transform syntax error, line %d, column %d: %s", line, column, message));
    }

    /**
     * Creates a new instance for an application-specific error. For those errors we
     * no longer have line and column positions.
     * 
     * @param message The application-specific instruction that could not be parsed.
     */
    public SSSOMTransformError(String message) {
        super(message);
    }

    /**
     * Creates a new instance for an application-specific errors.
     * <p>
     * This is merely a convenience constructor and is equivalent to
     * {@code SSSOMTransformError(String.format(message, args))}.
     * 
     * @param message The application-specific error message, as a format string.
     * @param args    Arguments to insert into the format string.
     */
    public SSSOMTransformError(String message, Object... args) {
        super(String.format(message, args));
    }
}
