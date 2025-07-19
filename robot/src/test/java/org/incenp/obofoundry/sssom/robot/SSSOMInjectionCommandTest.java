/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.robot;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class SSSOMInjectionCommandTest {

    /*
     * Check generation of a FBbt/FBdv-to-Uberon/CL bridge.
     */
    @Test
    void testSampleBridge() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--input", "../ext/src/test/resources/owl/uberon.ofn.gz",
                "--sssom", "../ext/src/test/resources/sets/fbbt.sssom.tsv",
                "--sssom", "../ext/src/test/resources/sets/fbdv.sssom.tsv",
                "--ruleset", "../ext/src/test/resources/rules/fbbt-bridge.rules",
                "--exclude-rule", "xrefs",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/fbbt-bridge.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("fbbt-bridge.ofn");
    }

    /*
     * Likewise, but using the "legacy" ruleset.
     */
    @Test
    void testSampleBridgeLegacy() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--input", "../ext/src/test/resources/owl/uberon.ofn.gz",
                "--sssom", "../ext/src/test/resources/sets/fbbt.sssom.tsv",
                "--sssom", "../ext/src/test/resources/sets/fbdv.sssom.tsv",
                "--ruleset", "../ext/src/test/resources/rules/fbbt-bridge-legacy.rules",
                "--exclude-rule", "xrefs",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/fbbt-bridge.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("fbbt-bridge.ofn");
    }

    /*
     * Check the production of cross-references from mappings.
     */
    @Test
    void testSampleXref() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--input", "../ext/src/test/resources/owl/uberon.ofn.gz",
                "--sssom", "../ext/src/test/resources/sets/fbbt.sssom.tsv",
                "--sssom", "../ext/src/test/resources/sets/fbdv.sssom.tsv",
                "--ruleset", "../ext/src/test/resources/rules/fbbt-bridge.rules",
                "--exclude-rule", "fbbt",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/fbbt-xrefs.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("fbbt-xrefs.ofn");
    }

    /*
     * Likewise, but using the "legacy" ruleset.
     */
    @Test
    void testSampleXrefLegacy() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--input", "../ext/src/test/resources/owl/uberon.ofn.gz",
                "--sssom", "../ext/src/test/resources/sets/fbbt.sssom.tsv",
                "--sssom", "../ext/src/test/resources/sets/fbdv.sssom.tsv",
                "--ruleset", "../ext/src/test/resources/rules/fbbt-bridge-legacy.rules",
                "--exclude-rule", "fbbt",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/fbbt-xrefs.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("fbbt-xrefs.ofn");
    }

    @Test
    void testUriExpressionExpansion() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--create",
                "--sssom", "../ext/src/test/resources/sets/test-uriexpression-ids.sssom.tsv",
                "--ruleset", "../ext/src/test/resources/rules/uriexpr-to-owl.rules",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/uriexpr-bridge.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("uriexpr-bridge.ofn");
    }

    @Test
    void testDirectSerialisation() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--create",
                "--sssom", "../core/src/test/resources/sets/exo2c.sssom.tsv",
                "--direct",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/exo2c-direct.ofn.out");
        // @formatter:off
        TestUtils.checkOutput("exo2c-direct.ofn");
    }

    @Test
    void testOWLExport() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--create",
                "--sssom", "../core/src/test/resources/sets/exo2c.sssom.tsv",
                "--direct",
                "convert", "--format", "ofn",
                "--output", "src/test/resources/output/exo2c-owl-export.ofn.out");
        // @formatter:on
        TestUtils.checkOutput("exo2c-owl-export.ofn");
    }

    @Test
    void testDirectAnnotations() throws IOException {
        // @formatter:off
        TestUtils.runCommand("sssom-inject",
                "--create",
                "--sssom", "../core/src/test/resources/sets/exo2c.sssom.tsv",
                "--ruleset", "../ext/src/test/resources/rules/annotations.rules",
                "--bridge-format", "ofn",
                "--bridge-file", "src/test/resources/output/exo2c-annotated.ofn.out");
        // @formatter:off
        TestUtils.checkOutput("exo2c-annotated.ofn");
    }
}
