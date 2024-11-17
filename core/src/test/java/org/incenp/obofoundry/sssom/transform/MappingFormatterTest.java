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

package org.incenp.obofoundry.sssom.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.ExtensionValue;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.PredicateModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MappingFormatterTest {

    private MappingFormatter formatter = new MappingFormatter();

    @Test
    void testSimpleBracketedExpansion() {
        formatter.setSubstitution("subject", (m) -> m.getSubjectId());

        IMappingTransformer<String> f = formatter.getTransformer("Subject is <%{subject}>");
        Assertions.assertEquals("Subject is <https://example.org/entities/0001>", f.transform(getSampleMapping()));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testSimpleLegacyExpansion() {
        formatter.addSubstitution("subject", (m) -> m.getSubjectId());

        IMappingTransformer<String> f = formatter.getTransformer("Subject is <%subject>");
        Assertions.assertEquals("Subject is <https://example.org/entities/0001>", f.transform(getSampleMapping()));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testMixedExpansion() {
        formatter.setSubstitution("subject_id", (m) -> m.getSubjectId());
        formatter.addSubstitution("subject_label", (m) -> m.getSubjectLabel());

        IMappingTransformer<String> f = formatter.getTransformer("%{subject_id} = %subject_label");
        Assertions.assertEquals("https://example.org/entities/0001 = alice", f.transform(getSampleMapping()));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDifferentExpansionInOpenOrBracketedMode() {
        formatter.setSubstitution("subject_label", (m) -> m.getSubjectLabel());
        formatter.addSubstitution("subject_label", (m) -> m.getSubjectLabel().toUpperCase());

        IMappingTransformer<String> f = formatter.getTransformer("%subject_label %{subject_label}");
        Assertions.assertEquals("ALICE alice", f.transform(getSampleMapping()));
    }

    @Test
    void testModifier() {
        formatter.setSubstitution("subject_label", (m) -> m.getSubjectLabel());
        formatter.setModifier("upper", (s) -> s.toUpperCase());

        IMappingTransformer<String> f = formatter
                .getTransformer("%{subject_label} (uppercase: %{subject_label|upper})");
        Assertions.assertEquals("alice (uppercase: ALICE)", f.transform(getSampleMapping()));

        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.getTransformer("%{subject_label|mod}"),
                "Unknown modifier: mod");
    }

    @Test
    @SuppressWarnings("deprecation")
    void testPercentEscape() {
        formatter.setSubstitution("subject_label", (m) -> m.getSubjectLabel());
        formatter.addSubstitution("subject_label", (m) -> m.getSubjectLabel());
        Mapping mapping = getSampleMapping();

        IMappingTransformer<String> f = formatter.getTransformer("%%{subject_label}");
        Assertions.assertEquals("%{subject_label}", f.transform(mapping));

        f = formatter.getTransformer("%%subject_label");
        Assertions.assertEquals("%subject_label", f.transform(mapping));
    }

    @Test
    void testIgnoreUnrecognisedOpenPlaceholder() {
        Mapping mapping = getSampleMapping();

        IMappingTransformer<String> f = formatter.getTransformer("unknown %placeholder here");
        Assertions.assertEquals("unknown %placeholder here", f.transform(mapping));

        f = formatter.getTransformer("at the %end");
        Assertions.assertEquals("at the %end", f.transform(mapping));

        f = formatter.getTransformer("really at the end%");
        Assertions.assertEquals("really at the end%", f.transform(mapping));

        f = formatter.getTransformer("followed by non-letters: %!");
        Assertions.assertEquals("followed by non-letters: %!", f.transform(mapping));
    }

    @Test
    void testBracketedPlaceholderCornerCases() {
        Mapping mapping = getSampleMapping();

        IMappingTransformer<String> f = formatter.getTransformer("Empty %{}");
        Assertions.assertEquals("Empty %{}", f.transform(mapping));

        f = formatter.getTransformer("%{inexisting}");
        Assertions.assertEquals("%{inexisting}", f.transform(mapping));

        formatter.setModifier("upper", (s) -> s.toUpperCase());
        f = formatter.getTransformer("%{|upper}");
        Assertions.assertEquals("%{}", f.transform(mapping));

        f = formatter.getTransformer("%{inexisting|upper}");
        Assertions.assertEquals("%{INEXISTING}", f.transform(mapping));
    }

    @Test
    void testUnterminatedBracketedPlaceholder() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.getTransformer("%{some"),
                "Unterminated placeholder in format string");
        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.getTransformer("%{some|mod"),
                "Unterminated placeholder in format string");
    }

    @Test
    @SuppressWarnings("deprecation")
    void testIllegalValues() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.setSubstitution("sub|", null),
                "Invalid placeholder name");
        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.setSubstitution("sub}", null),
                "Invalid placeholder name");
        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.addSubstitution("su:", null),
                "Invalid placeholder name");

        Assertions.assertThrows(IllegalArgumentException.class, () -> formatter.setModifier("mod|", null),
                "Invalid modifier name");
    }

    @Test
    void testOverridingSubstitutions() {
        Mapping mapping = getSampleMapping();

        formatter.setSubstitution("subject", (m) -> m.getSubjectLabel());
        Assertions.assertEquals("Subject: alice", formatter.format("Subject: %{subject}", mapping));
        IMappingTransformer<String> f1 = formatter.getTransformer("Subject: %{subject}");

        formatter.setSubstitution("subject", (m) -> m.getSubjectLabel().toUpperCase());
        Assertions.assertEquals("Subject: ALICE", formatter.format("Subject: %{subject}", mapping));
        IMappingTransformer<String> f2 = formatter.getTransformer("Subject: %{subject}");

        Assertions.assertEquals("Subject: alice", f1.transform(mapping));
        Assertions.assertEquals("Subject: ALICE", f2.transform(mapping));
    }

    @Test
    void testStandardSubstitutions() {
        formatter.setStandardSubstitutions();

        Mapping m = getSampleMapping();
        Assertions.assertEquals("alice", formatter.getTransformer("%{subject_label}").transform(m));

        // Check that we can override a standard substitution
        formatter.setSubstitution("subject_label", (mapping) -> mapping.getSubjectLabel().toUpperCase());
        Assertions.assertEquals("ALICE", formatter.getTransformer("%{subject_label}").transform(m));

        // Check we get an empty string for a null field
        Assertions.assertEquals("justification: ",
                formatter.getTransformer("justification: %{mapping_justification}").transform(m));

        // Check formatting of doubles
        m.setConfidence(0.7);
        Assertions.assertEquals("confidence: 0.7", formatter.getTransformer("confidence: %{confidence}").transform(m));

        // Check formatting of enum values
        m.setPredicateModifier(PredicateModifier.NOT);
        Assertions.assertEquals("modifier: Not",
                formatter.getTransformer("modifier: %{predicate_modifier}").transform(m));

        // Check formatting of lists
        m.setAuthorLabel(new ArrayList<String>());
        m.getAuthorLabel().add("Alice");
        m.getAuthorLabel().add("Bob");
        Assertions.assertEquals("Alice|Bob", formatter.getTransformer("%{author_label}").transform(m));
    }

    @Test
    void testSubstitutionWithExtensionSlots() {
        Mapping m = getSampleMapping();
        Map<String, ExtensionValue> extensions = new HashMap<String, ExtensionValue>();
        extensions.put("https://example.org/properties/barProperty", new ExtensionValue("extended value"));
        m.setExtensions(extensions);

        Assertions.assertEquals("bar: extended value",
                formatter.getTransformer("bar: %{https://example.org/properties/barProperty}").transform(m));
    }

    private Mapping getSampleMapping() {
        // @formatter:off
        Mapping m = Mapping.builder()
                .subjectId("https://example.org/entities/0001")
                .subjectLabel("alice")
                .predicateId("http://www.w3.org/2004/02/skos/core#closeMatch")
                .objectId("https://example.com/entities/0011")
                .objectLabel("alpha")
                .build();
        // @formatter:on

        return m;
    }
}
