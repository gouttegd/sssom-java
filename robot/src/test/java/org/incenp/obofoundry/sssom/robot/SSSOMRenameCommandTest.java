/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.robot;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class SSSOMRenameCommandTest {

    @Test
    void testSimpleRename() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-rename",
                "--input", "../core/src/test/resources/owl/fbdv.ofn",
                "--sssom", "../core/src/test/resources/sets/fbdv-renames.sssom.tsv",
                "convert",
                "--format", "ofn",
                "--output", "src/test/resources/output/fbdv-renaming-test.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("fbdv-renaming-test.ofn");
    }
}
