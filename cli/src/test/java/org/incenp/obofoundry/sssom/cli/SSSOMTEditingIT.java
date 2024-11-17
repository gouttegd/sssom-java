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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.cli;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests for editing functions of the SSSOM/T common language. Ideally this
 * should rather be done in sssom-core, but for now the CLI provides an easy
 * test harness.
 */
public class SSSOMTEditingIT {

    @Test
    void testMappingEdition() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-edited-all-exact-match.sssom.tsv",
                new String[] { "--prefix-map-from-input", "--rule=subject==* -> edit('predicate_id=skos:exactMatch')",
                        "--include-all" });
    }

    @Test
    void testMappingEditionWithAssign() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-edited-all-exact-match.sssom.tsv",
                new String[] { "--prefix-map-from-input",
                        "--rule=subject==* -> assign('predicate_id', 'skos:exactMatch')", "--include-all" });
    }

    @Test
    void testMappingEditionWithDelayedAssign() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-edited-delayed-change.sssom.tsv",
                new String[] { "--prefix-map-from-input",
                        "--rule=predicate==skos:closeMatch -> assign('comment', 'almost the same as %{subject_label} (%{subject_id|short})')",
                        "--include-all" });
    }

    @Test
    void testMappingEditionWithValuesFromExtensionSlots() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c-with-extensions.sssom.tsv" },
                "exo2c-edited-extension-derived-values.sssom.tsv",
                new String[] { "--prefix-map-from-input", "--accept-extra-metadata=DEFINED",
                        "--rule=subject==* -> assign('curation_rule_text', 'bar: %{https://example.org/properties/barProperty}')",
                        "--include-all" });
    }

    @Test
    void testMappingEditionWithReplace() throws IOException {
        TestUtils.runCommand(0, new String[] { "exo2c.sssom.tsv" }, "exo2c-edited-all-exact-match.sssom.tsv",
                new String[] { "--prefix-map-from-input",
                        "--rule=subject==* -> replace('predicate_id', 'closeMatch', 'exactMatch')", "--include-all" });
    }
}
