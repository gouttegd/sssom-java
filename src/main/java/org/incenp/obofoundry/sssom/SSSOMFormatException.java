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

/**
 * This exception is thrown when processing data that do not conform to the
 * SSSOM format specification.
 */
public class SSSOMFormatException extends Exception {

    private static final long serialVersionUID = 2425679493957555557L;

    public SSSOMFormatException(String msg) {
        super(msg);
    }

    public SSSOMFormatException(String msg, Throwable inner) {
        super(msg, inner);
    }

}
