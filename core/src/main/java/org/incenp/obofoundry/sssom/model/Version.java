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

package org.incenp.obofoundry.sssom.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents a version of the SSSOM specification.
 */
public enum Version {

    /**
     * SSSOM specification version 1.0.
     */
    SSSOM_1_0(1, 0),

    /**
     * SSSOM specification version 1.1.
     */
    SSSOM_1_1(1, 1),

    /**
     * Represents an unrecognised version of the specification (possibly, a version
     * more recent than the latest supported by this implementation).
     */
    UNKNOWN(0, 0);

    /**
     * The latest version of the specification currently supported by this
     * implementation.
     */
    public static final Version LATEST = SSSOM_1_1;

    private final int major;
    private final int minor;

    Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public String toString() {
        return String.format("%d.%d", major, minor);
    }

    /**
     * Checks whether this version is compatible with the indicated version.
     * <p>
     * For the purpose of this method, a version <em>A</em> is said to be compatible
     * with another version <em>B</em> if an implementation compliant with
     * <em>B</em> can accept data compliant with <em>A</em>. This is case iff: (1)
     * both <em>A</em> and <em>B</em> have the same major version number, and (2)
     * <em>A</em>’s minor version number is at most as high as <em>B</em>’s.
     * <p>
     * For example, version 1.0 is compatible with version 1.1; version 1.2 is
     * <em>not</em> compatible with version 1.1.
     * 
     * @param target The version to compare to.
     * @return {@code True} if this version is compatible with the target version,
     *         {@code false} otherwise.
     */
    public boolean isCompatibleWith(Version target) {
        return (major == target.major) && (minor <= target.minor);
    }

    /**
     * Gets the most recent version of all the versions in the given collection.
     * 
     * @param versions A collection of versions.
     * @return The most recent version, or {@link #SSSOM_1_0} if the collection is
     *         empty.
     */
    public static Version getHighestVersion(Collection<Version> versions) {
        Version ret = Version.SSSOM_1_0;
        for ( Version v : versions ) {
            if ( v.compareTo(ret) > 0 ) {
                ret = v;
            }
        }
        return ret;
    }

    /**
     * Parses a string into the corresponding SSSOM version object.
     * 
     * @param v The string to parse.
     * @return The corresponding version, or {@code #UNKNOWN} if the string does not
     *         match any recognised version.
     */
    @JsonCreator
    public static Version fromString(String v) {
        if ( v.equals("1.0") ) {
            return SSSOM_1_0;
        } else if ( v.equals("1.1") ) {
            return SSSOM_1_1;
        } else {
            return UNKNOWN;
        }
    }
}
