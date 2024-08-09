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

package org.incenp.obofoundry.sssom.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.EntityType;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class SSSOMTransformReaderTest {

    private DummyApplication app;
    private SSSOMTransformReader<Void> reader;

    SSSOMTransformReaderTest(TestInfo info) {
        app = new DummyApplication();
        if ( !info.getTags().contains("no-common-reader") ) {
            reader = new SSSOMTransformReader<Void>(app);
            reader.addPrefix("ORGENT", "https://example.org/entities/");
            reader.addPrefix("NETENT", "https://example.com/entities/");
            reader.addPrefix("ORGPID", "https://example.org/people/");
        }
    }

    /*
     * Check consistency of success indicators.
     */
    @Test
    void testParseValidRule() {
        Assertions.assertTrue(reader.read("predicate==* -> action();\n"));
        Assertions.assertFalse(reader.hasErrors());
        Assertions.assertTrue(reader.getErrors().isEmpty());
        Assertions.assertEquals(1, reader.getRules().size());
    }

    /*
     * Check that the parser automatically add terminator and trailing whitespace as
     * needed.
     */
    @Test
    void testParsingTerminators() {
        // All those calls should be equivalent
        parseRule("predicate==* -> action()", "(*) -> action()");
        parseRule("predicate==* -> action();", "(*) -> action()");
        parseRule("predicate==* -> action();\n", "(*) -> action()");
        parseRule("predicate==* -> action();\n\n", "(*) -> action()");

        // Likewise
        parseRule("predicate==* -> { action(); }", "(*) -> action()");
        parseRule("predicate==* -> { action(); }\n", "(*) -> action()");
    }

    /*
     * Check consistency of failure indicators.
     */
    @Test
    void testParseInvalidRule() {
        Assertions.assertFalse(reader.read("this is not valid SSSOM/T"));
        Assertions.assertTrue(reader.hasErrors());
        Assertions.assertFalse(reader.getErrors().isEmpty());
        Assertions.assertTrue(reader.getRules().isEmpty());
    }

    /*
     * Check the behaviour of the parser when read is called repeatedly (when used
     * to parse strings rather than a file).
     */
    @Test
    void testParseSeveralRules() {
        // Successfully parsed rules are accumulated
        reader.read("predicate==* -> action();\n");
        reader.read("predicate==* -> action();\n");
        Assertions.assertEquals(2, reader.getRules().size());

        // Failure to parse a rule does not affect already parsed rules
        reader.read("this is not valid SSSOM/T");
        Assertions.assertEquals(2, reader.getRules().size());
        Assertions.assertFalse(reader.getErrors().isEmpty());

        // The list of errors is reset when read is called again
        reader.read("predicate==* -> action();\n");
        Assertions.assertEquals(3, reader.getRules().size());
        Assertions.assertTrue(reader.getErrors().isEmpty());
    }

    /*
     * Check parsing some individual rules.
     */
    @Test
    void testParseString() {
        parseRule("predicate==skos:exactMatch -> action('something');\n",
                "(predicate==http://www.w3.org/2004/02/skos/core#exactMatch) -> action(something)");

        parseRule("justification==semapv:ManualMappingCuration -> action();\n",
                "(justification==https://w3id.org/semapv/vocab/ManualMappingCuration) -> action()");
        
        parseRule("!subject==ORGENT:* -> action();\n", "(!subject==https://example.org/entities/*) -> action()");

        parseRule("subject==ORGENT:* && predicate==skos:exactMatch -> action();\n",
                "(subject==https://example.org/entities/* && predicate==http://www.w3.org/2004/02/skos/core#exactMatch) -> action()");

        parseRule("subject==ORGENT:* predicate==skos:exactMatch -> action();\n",
                "(subject==https://example.org/entities/* && predicate==http://www.w3.org/2004/02/skos/core#exactMatch) -> action()");

        parseRule("subject==ORGENT:* || predicate==skos:exactMatch -> action();\n",
                "(subject==https://example.org/entities/* || predicate==http://www.w3.org/2004/02/skos/core#exactMatch) -> action()");

        parseRule("mapping_tool=='foo mapper' -> action();\n", "(mapping_tool==foo mapper) -> action()");

        parseRule("see_also==\"check this*\" -> action();\n", "(see_also==check this*) -> action()");

        parseRule("subject_type=='owl class' -> action();\n", "(subject_type==owl class) -> action()");
    }

    /*
     * Check that undeclared prefixes result in failure.
     */
    @Test
    void testParseRuleWithUndeclaredPrefix() {
        parseRule("subject==COMENT:* -> action();\n", null);
        Assertions.assertEquals("Undeclared prefix: COMENT", reader.getErrors().get(0).getMessage());
    }

    /*
     * Check that short identifiers in strings are expanded if we ask for it.
     */
    @Test
    void testCurieExpansionInStringArguments() {
        app.curieExpansionFormat = "<%s>";
        parseRule(
                "subject==* -> action('ORGENT:0001');\n", "(*) -> action(<https://example.org/entities/0001>)");
    }

    /*
     * Check that we can parse filter with empty values.
     */
    @Test
    void testParseEmptyFilter() {
        parseRule("mapping_tool==\"\" -> action();\n", "(mapping_tool==) -> action()");
        parseRule("subject==~ -> action();\n", "(subject==~) -> action()");
        parseRule("subject_type==\"\" -> action();\n", "(subject_type==) -> action()");
    }

    /*
     * Test that a mapping is correctly selected by a filter on a text slot.
     */
    @Test
    void testStringFilter() {
        Mapping tool = Mapping.builder().mappingTool("tool").build();
        Mapping other = Mapping.builder().mappingTool("other").build();
        Mapping empty = Mapping.builder().mappingTool("").build();
        Mapping none = Mapping.builder().mappingTool(null).build();

        checkFilter("mapping_tool==\"tool\" -> action();\n", tool, true);
        checkFilter("mapping_tool==\"tool\" -> action();\n", other, false);
        checkFilter("mapping_tool==\"tool\" -> action();\n", empty, false);
        checkFilter("mapping_tool==\"tool\" -> action();\n", none, false);

        checkFilter("mapping_tool==\"to*\" -> action();\n", tool, true);
        checkFilter("mapping_tool==\"to*\" -> action();\n", other, false);
        checkFilter("mapping_tool==\"to*\" -> action();\n", empty, false);
        checkFilter("mapping_tool==\"to*\" -> action();\n", none, false);

        checkFilter("mapping_tool==\"\" -> action();\n", tool, false);
        checkFilter("mapping_tool==\"\" -> action();\n", empty, true);
        checkFilter("mapping_tool==\"\" -> action();\n", none, true);

        checkFilter("!mapping_tool==\"\" -> action();\n", empty, false);
        checkFilter("!mapping_tool==\"\" -> action();\n", none, false);
        checkFilter("!mapping_tool==\"\" -> action();\n", tool, true);
        checkFilter("!mapping_tool==\"\" -> action();\n", other, true);
    }

    /*
     * Test that a mapping is correctly selected by a filter on a ID slot.
     */
    @Test
    void testIdFilter() {
        Mapping org1 = Mapping.builder().subjectId("https://example.org/entities/0001").build();
        Mapping org2 = Mapping.builder().subjectId("https://example.org/entities/0002").build();
        Mapping net1 = Mapping.builder().subjectId("https://example.net/entities/0001").build();
        Mapping empty = Mapping.builder().subjectId("").build();
        Mapping none = Mapping.builder().subjectId(null).build();

        checkFilter("subject==ORGENT:0001 -> action();\n", org1, true);
        checkFilter("subject==ORGENT:0001 -> action();\n", org2, false);
        checkFilter("subject==ORGENT:0001 -> action();\n", net1, false);

        checkFilter("subject==ORGENT:* -> action();\n", org1, true);
        checkFilter("subject==ORGENT:* -> action();\n", org2, true);
        checkFilter("subject==ORGENT:* -> action();\n", net1, false);

        checkFilter("subject==* -> action();\n", org1, true);
        checkFilter("subject==* -> action();\n", org2, true);
        checkFilter("subject==* -> action();\n", net1, true);

        checkFilter("subject==~ -> action();\n", org1, false);
        checkFilter("subject==~ -> action();\n", empty, true);
        checkFilter("subject==~ -> action();\n", none, true);

        checkFilter("!subject==~ -> action();\n", org1, true);
        checkFilter("!subject==~ -> action();\n", org2, true);
        checkFilter("!subject==~ -> action();\n", empty, false);
        checkFilter("!subject==~ -> action();\n", none, false);
    }

    /*
     * Test that a mapping is correctly selected by a filter in a multi-valued slot.
     */
    @Test
    void testTextListFilter() {
        Mapping alice = Mapping.builder().authorLabel(new ArrayList<String>()).build();
        alice.getAuthorLabel().add("Alice");

        Mapping aliceAndBob = Mapping.builder().authorLabel(new ArrayList<String>()).build();
        aliceAndBob.getAuthorLabel().add("Alice");
        aliceAndBob.getAuthorLabel().add("Bob");

        Mapping empty = Mapping.builder().authorLabel(new ArrayList<String>()).build();
        Mapping none = Mapping.builder().authorLabel(null).build();

        checkFilter("author_label==\"Alice\" -> action();\n", alice, true);
        checkFilter("author_label==\"Alice\" -> action();\n", aliceAndBob, true);
        checkFilter("author_label==\"Alice\" -> action();\n", empty, false);
        checkFilter("author_label==\"Alice\" -> action();\n", none, false);

        checkFilter("author_label==\"Bob\" -> action();\n", alice, false);
        checkFilter("author_label==\"Bob\" -> action();\n", aliceAndBob, true);
        checkFilter("author_label==\"Bob\" -> action();\n", empty, false);
        checkFilter("author_label==\"Bob\" -> action();\n", none, false);

        checkFilter("author_label==\"\" -> action();\n", alice, false);
        checkFilter("author_label==\"\" -> action();\n", aliceAndBob, false);
        checkFilter("author_label==\"\" -> action();\n", empty, true);
        checkFilter("author_label==\"\" -> action();\n", none, true);
    }

    /*
     * Test that a mapping is correctly selected by a filter in a multi-valued
     * identifier slot.
     */
    @Test
    void testIDListFilter() {
        Mapping alice = Mapping.builder().authorId(new ArrayList<String>()).build();
        alice.getAuthorId().add("https://example.org/people/0000-0000-0001-1234");

        Mapping aliceAndBob = Mapping.builder().authorId(new ArrayList<String>()).build();
        aliceAndBob.getAuthorId().add("https://example.org/people/0000-0000-0001-1234");
        aliceAndBob.getAuthorId().add("https://example.org/people/0000-0000-0001-5678");

        Mapping empty = Mapping.builder().authorId(new ArrayList<String>()).build();
        Mapping none = Mapping.builder().authorId(null).build();

        checkFilter("author==ORGPID:0000-0000-0001-1234 -> action();\n", alice, true);
        checkFilter("author==ORGPID:0000-0000-0001-1234 -> action();\n", aliceAndBob, true);
        checkFilter("author==ORGPID:0000-0000-0001-1234 -> action();\n", empty, false);
        checkFilter("author==ORGPID:0000-0000-0001-1234 -> action();\n", none, false);

        checkFilter("author==ORGPID:0000-0000-0001-5678 -> action();\n", alice, false);
        checkFilter("author==ORGPID:0000-0000-0001-5678 -> action();\n", aliceAndBob, true);
        checkFilter("author==ORGPID:0000-0000-0001-5678 -> action();\n", empty, false);
        checkFilter("author==ORGPID:0000-0000-0001-5678 -> action();\n", none, false);

        checkFilter("author==~ -> action();\n", alice, false);
        checkFilter("author==~ -> action();\n", aliceAndBob, false);
        checkFilter("author==~ -> action();\n", empty, true);
        checkFilter("author==~ -> action();\n", none, true);
    }

    /*
     * Test that a mapping is correctly selected by a filter in on a slot that
     * expects an entity_type value.
     */
    @Test
    void testEntityTypeFilter() {
        Mapping owlClass = Mapping.builder().subjectType(EntityType.OWL_CLASS).build();
        Mapping literal = Mapping.builder().subjectType(EntityType.RDFS_LITERAL).build();
        Mapping none = Mapping.builder().subjectType(null).build();

        checkFilter("subject_type==\"owl class\" -> action();\n", owlClass, true);
        checkFilter("subject_type==\"owl class\" -> action();\n", literal, false);
        checkFilter("subject_type==\"owl class\" -> action();\n", none, false);

        checkFilter("subject_type==\"*\" -> action();\n", owlClass, true);
        checkFilter("subject_type==\"*\" -> action();\n", literal, true);
        checkFilter("subject_type==\"*\" -> action();\n", none, true);

        checkFilter("subject_type==\"\" -> action();\n", owlClass, false);
        checkFilter("subject_type==\"\" -> action();\n", literal, false);
        checkFilter("subject_type==\"\" -> action();\n", none, true);

        checkFilter("!subject_type==\"\" -> action();\n", owlClass, true);
        checkFilter("!subject_type==\"\" -> action();\n", literal, true);
        checkFilter("!subject_type==\"\" -> action();\n", none, false);
    }

    /*
     * Check parsing a complete file. The test file is based on the real use case of
     * the bridge between FBbt and Uberon/CL.
     */
    @Test
    @Tag("no-common-reader")
    void testParseFile() throws IOException {
        reader = new SSSOMTransformReader<Void>(app, "src/test/resources/rules/fbbt-bridge.rules");
        reader.read();

        Assertions.assertFalse(reader.hasErrors());

        Assertions.assertEquals(2, app.headerFunctions.size());
        Assertions.assertEquals("declare_class(http://purl.obolibrary.org/obo/NCBITaxon_7227)",
                app.headerFunctions.get(0));
        Assertions.assertEquals("declare_object_property(http://purl.obolibrary.org/obo/BFO_0000050)",
                app.headerFunctions.get(1));

        List<MappingProcessingRule<Void>> rules = reader.getRules();
        Assertions.assertEquals(8, rules.size());
        
        String[] renditions = new String[] {
                "((subject==http://purl.obolibrary.org/obo/UBERON_* || subject==http://purl.obolibrary.org/obo/CL_*)) -> invert()",
                "(!(object==http://purl.obolibrary.org/obo/UBERON_* || object==http://purl.obolibrary.org/obo/CL_*)) -> stop()",
                "(!cardinality==*:1) -> stop()",
                "[fbbt,uberon-fbbt] ((subject==http://purl.obolibrary.org/obo/FBbt_* && predicate==https://w3id.org/semapv/vocab/crossSpeciesExactMatch) && (object==http://purl.obolibrary.org/obo/UBERON_*)) -> annotate_subject(http://purl.obolibrary.org/obo/IAO_0000589, %subject_label (Drosophila))",
                "[fbbt,uberon-fbbt] ((subject==http://purl.obolibrary.org/obo/FBbt_* && predicate==https://w3id.org/semapv/vocab/crossSpeciesExactMatch) && (object==http://purl.obolibrary.org/obo/UBERON_*)) -> create_axiom(%subject_id EquivalentTo: %object_id and (BFO:0000050 some TAX:7227))",
                "[fbbt,cl-fbbt] ((subject==http://purl.obolibrary.org/obo/FBbt_* && predicate==https://w3id.org/semapv/vocab/crossSpeciesExactMatch) && (object==http://purl.obolibrary.org/obo/CL_*)) -> annotate_subject(http://purl.obolibrary.org/obo/IAO_0000589, %subject_label (Drosophila))",
                "[fbbt,cl-fbbt] ((subject==http://purl.obolibrary.org/obo/FBbt_* && predicate==https://w3id.org/semapv/vocab/crossSpeciesExactMatch) && (object==http://purl.obolibrary.org/obo/CL_*)) -> create_axiom(%subject_id EquivalentTo: %object_id and (BFO:0000050 some TAX:7227))",
                "[xrefs] (*) -> annotate_object(http://www.geneontology.org/formats/oboInOwl#hasDbXref, %subject_curie)"
        };

        for ( int i = 0; i < renditions.length; i++ ) {
            Assertions.assertEquals(renditions[i], rules.get(i).toString());
        }
    }

    /*
     * Parse a single rule as a string and compare with the expected rendition.
     */
    private void parseRule(String rule, String rendition) {
        int n = reader.getRules().size();
        boolean success = reader.read(rule);
        if ( rendition != null ) {
            Assertions.assertTrue(success);
            Assertions.assertEquals(n + 1, reader.getRules().size());
            Assertions.assertEquals(rendition, reader.getRules().get(n).toString());
        } else {
            Assertions.assertFalse(success);
        }
    }
    
    /*
     * Parse a single rule and check if it applies to the given mapping.
     */
    private void checkFilter(String rule, Mapping mapping, boolean selected) {
        Assertions.assertTrue(reader.read(rule));
        MappingProcessingRule<Void> parsedRule = reader.getRules().get(reader.getRules().size() - 1);
        Assertions.assertEquals(selected, parsedRule.apply(mapping));
    }

    /*
     * A dummy SSSOM/T application that accepts any function and ensures the rules
     * have a string representation to which they can be compared.
     */
    private class DummyApplication implements ISSSOMTransformApplication<Void> {

        List<String> headerFunctions = new ArrayList<String>();
        String curieExpansionFormat = null;

        @Override
        public void onInit(PrefixManager prefixManager) {
        }

        @Override
        public void onHeaderAction(String name, List<String> arguments) throws SSSOMTransformError {
            headerFunctions.add(format(name, arguments));
        }

        @Override
        public IMappingTransformer<Mapping> onPreprocessingAction(String name, List<String> arguments)
                throws SSSOMTransformError {
            return new NamedMappingTransformer<Mapping>(format(name, arguments), null);
        }

        @Override
        public IMappingTransformer<Void> onGeneratingAction(String name, List<String> arguments)
                throws SSSOMTransformError {
            return new NamedMappingTransformer<Void>(format(name, arguments), null);
        }

        @Override
        public String getCurieExpansionFormat() {
            return curieExpansionFormat;
        }

        private String format(String name, List<String> arguments) {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append('(');
            sb.append(String.join(", ", arguments));
            sb.append(')');
            return sb.toString();
        }
    }
}
