/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024,2025 Damien Goutte-Gattat
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a format able to serialise SSSOM data.
 */
public enum SerialisationFormat {
    /**
     * SSSOM/TSV, the main format specifically designed to serialise SSSOM mappings.
     */
    TSV("SSSOM/TSV", "tsv", ".sssom.tsv", true, true),

    /**
     * SSSOM/CSV, variant used comma-separated columns.
     */
    CSV("SSSOM/CSV", "csv", ".sssom.csv", true, true),

    /**
     * SSSOM/JSON, the other format described in the SSSOM specification.
     */
    JSON("SSSOM/JSON", "json", ".sssom.json", true, true),

    /**
     * RDF/Turtle, a RDF representation of SSSOM objects in the Turtle syntax.
     */
    RDF_TURTLE("RDF Turtle", "ttl", ".ttl", true, true);

    private final static Map<String, SerialisationFormat> NAMES_MAP;
    private final static Map<String, SerialisationFormat> EXTENSIONS_MAP;
    private final static List<String> SHORT_NAMES;
    private final static List<String> READABLE_SHORT_NAMES;
    private final static List<String> WRITABLE_SHORT_NAMES;

    static {
        Map<String, SerialisationFormat> byNames = new HashMap<String, SerialisationFormat>();
        Map<String, SerialisationFormat> byExtensions = new HashMap<String, SerialisationFormat>();
        List<String> shortNames = new ArrayList<String>();
        List<String> readableShortNames = new ArrayList<String>();
        List<String> writableShortNames = new ArrayList<String>();

        for ( SerialisationFormat value : SerialisationFormat.values() ) {
            byNames.put(value.shortName, value);
            byNames.put(value.name.toLowerCase(), value);
            byExtensions.put(value.extension, value);
            shortNames.add(value.shortName);
            if ( value.isReadable() ) {
                readableShortNames.add(value.shortName);
            }
            if ( value.isWritable() ) {
                writableShortNames.add(value.shortName);
            }

        }

        NAMES_MAP = Collections.unmodifiableMap(byNames);
        EXTENSIONS_MAP = Collections.unmodifiableMap(byExtensions);
        SHORT_NAMES = Collections.unmodifiableList(shortNames);
        READABLE_SHORT_NAMES = Collections.unmodifiableList(readableShortNames);
        WRITABLE_SHORT_NAMES = Collections.unmodifiableList(writableShortNames);
    }

    private String name;
    private String shortName;
    private String extension;
    private boolean canRead;
    private boolean canWrite;


    SerialisationFormat(String name, String shortName, String extension, boolean canRead, boolean canWrite) {
        this.name = name;
        this.shortName = shortName;
        this.extension = extension;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    /**
     * Gets the user-facing name of the serialisation format.
     * <p>
     * That name is suitable, e.g. for display in graphical interfaces.
     * 
     * @return The format name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the short name of the serialisation format.
     * <p>
     * That name is suitable, e.g. for selecting the format in a command-line
     * option.
     * 
     * @return The format short name.
     */
    public String getSortName() {
        return shortName;
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
     * Indicates whether reading from this format is supported.
     * 
     * @return <code>true</code> if the SSSOM-Java library can read this format.
     */
    public boolean isReadable() {
        return canRead;
    }

    /**
     * Indicates whether writing to this format is supported.
     * 
     * @return <code>true</code> if the SSSOM-Java library can write to this format.
     */
    public boolean isWritable() {
        return canWrite;
    }

    /**
     * Gets a list of all available format short names.
     * 
     * @return The available formats, as a list of short names.
     */
    public static List<String> getShortNames() {
        return SHORT_NAMES;
    }

    /**
     * Gets a list of the short names of all formats supported in reading mode.
     * 
     * @return The formats this library can read from, as a list of short names.
     */
    public static List<String> getReadableShortNames() {
        return READABLE_SHORT_NAMES;
    }

    /**
     * Gets a list of the short names of all formats supported in writing mode.
     * 
     * @return The formats this library can write to, as a list of short names.
     */
    public static List<String> getWritableShortNames() {
        return WRITABLE_SHORT_NAMES;
    }

    /**
     * Gets a serialisation format by its name (long or short).
     * 
     * @param name The name of the format. It can be either the user-facing name (as
     *             returned by {@link #getName()}) or the short name (as returned by
     *             {@link #getShortNames()}).
     * @return The serialisation format, or {@code null} if the given name does not
     *         match any known format.
     */
    public static SerialisationFormat fromName(String name) {
        return NAMES_MAP.get(name.toLowerCase());
    }

    /**
     * Gets a serialisation format by its associated filename extension.
     * 
     * @param extension The filename extension (not including the initial dot).
     * @return The serialisation format, or {@code null} if the given extension does
     *         not match any extension associated with a known format.
     */
    public static SerialisationFormat fromExtension(String extension) {
        return EXTENSIONS_MAP.get(extension);
    }
}
