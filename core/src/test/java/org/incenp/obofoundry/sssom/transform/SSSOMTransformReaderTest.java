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
