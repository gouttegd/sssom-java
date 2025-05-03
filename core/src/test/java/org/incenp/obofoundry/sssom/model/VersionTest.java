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

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {

    @Test
    void testVersionCompatibility() {
        Assertions.assertTrue(Version.SSSOM_1_0.isCompatibleWith(Version.SSSOM_1_0));
        Assertions.assertTrue(Version.SSSOM_1_0.isCompatibleWith(Version.SSSOM_1_1));
        Assertions.assertFalse(Version.SSSOM_1_1.isCompatibleWith(Version.SSSOM_1_0));
        Assertions.assertTrue(Version.SSSOM_1_1.isCompatibleWith(Version.SSSOM_1_1));
    }

    @Test
    void testGetHighestVersion() {
        Assertions.assertEquals(Version.SSSOM_1_0, Version.getHighestVersion(Set.of(Version.SSSOM_1_0)));
        Assertions.assertEquals(Version.SSSOM_1_1, Version.getHighestVersion(Set.of(Version.SSSOM_1_1)));
        Assertions.assertEquals(Version.SSSOM_1_1,
                Version.getHighestVersion(Set.of(Version.SSSOM_1_0, Version.SSSOM_1_1)));
    }

    @Test
    void testUnknownVersion() {
        Version unknown = Version.fromString("not a SSSOM version");
        Assertions.assertEquals(Version.UNKNOWN, unknown);

        // An unknown version cannot be compatible with any known version
        Assertions.assertFalse(unknown.isCompatibleWith(Version.SSSOM_1_0));
        Assertions.assertFalse(unknown.isCompatibleWith(Version.SSSOM_1_1));

        // No known version can be compatible with an unknown version
        Assertions.assertFalse(Version.SSSOM_1_0.isCompatibleWith(unknown));
        Assertions.assertFalse(Version.SSSOM_1_1.isCompatibleWith(unknown));

        // "Highest" version in a set containing an unknown version is unknown
        Assertions.assertEquals(Version.UNKNOWN, Version.getHighestVersion(Set.of(unknown, Version.SSSOM_1_1)));
    }
}
