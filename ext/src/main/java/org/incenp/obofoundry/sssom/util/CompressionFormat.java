/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.util;

/**
 * Represents a compression format we can read from or write to.
 * <p>
 * For now we only support GZip (and so this enumeration is kind of pointless),
 * but we may expand that to other formats (e.g. BZip2, XZ) in the future.
 */
public enum CompressionFormat {
    /**
     * No compression.
     */
    NONE(null),

    /**
     * The GZip file format, as per RFC 1952.
     * 
     * @see <a href="https://www.rfc-editor.org/info/rfc1952/">RFC 1952</a>
     */
    GZIP(".gz"),

    /**
     * The XZ file format from the XZ Utils project.
     * 
     * @see <a href="https://tukaani.org/xz/xz-file-format.txt">XZ file format</a>
     */
    XZ(".xz");

    private String extension;

    CompressionFormat(String extension) {
        this.extension = extension;
    }

    /**
     * Gets the typical filename extension associated with the format.
     * 
     * @return The format filename extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets a compression format by its name.
     * 
     * @param name The name of the format.
     * @return The compression format; {@link #NONE} if the given name does not
     *         match any known format.
     */
    public static CompressionFormat fromName(String name) {
        if ( name.equalsIgnoreCase("gzip") ) {
            return GZIP;
        }
        return NONE;
    }

    /**
     * Infers a compression format from a filename.
     * 
     * @param name The filename to infer the compression format from.
     * @return The inferred compression format; {@link #NONE} if no compression
     *         format could be recognised.
     */
    public static CompressionFormat fromFilename(String name) {
        for ( CompressionFormat fmt : CompressionFormat.values() ) {
            if ( fmt != NONE && name.endsWith(fmt.extension) ) {
                return fmt;
            }
        }
        return NONE;
    }
}
