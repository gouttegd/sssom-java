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

package org.incenp.obofoundry.sssom.owl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IFormatModifierFunction;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTFormatFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * Represents the SSSOM/T-OWL generator function "create_axiom".
 * <p>
 * That function can create arbitrary OWL axiom from an expression in OWL
 * Manchester syntax. It takes a single argument which is the OWL Manchester
 * representation of the axiom to create. The argument may contain placeholders
 * so that its value can be partially derived from the mapping the function is
 * being applied to.
 * <p>
 * For example, to create an equivalence axiom between the subject and the
 * object of the current mapping:
 * 
 * <pre>
 * create_axiom("&lt;%{subject_id}&gt; EquivalentTo: &lt;%{object_id}&gt;");
 * </pre>
 * 
 * <p>
 * Note that un-bracketed placeholders, if used, are automatically formatted
 * with enclosing angled brackets, for convenience, so the following call is
 * equivalent to the call above:
 * 
 * <pre>
 * create_axiom("%subject_id EquivalentTo: %object_id");
 * </pre>
 * 
 * <p>
 * The function also accepts an optional <code>/annots=...</code> parameter; if
 * present, it should be a list of SSSOM metadata fields that should be used to
 * annotate the generated axiom. When that parameter is present, another
 * parameter, <code>/annots_uris=...</code> can be used to specify how metadata
 * fields should be rendered into annotation properties (allowed values:
 * <code>direct</code>, <code>standard_map</code>; default is
 * <code>direct</code>).
 */
public class SSSOMTCreateAxiomFunction
        implements ISSSOMTFunction<IMappingTransformer<OWLAxiom>>, IMappingTransformer<OWLAxiom> {

    private SSSOMTOwlApplication app;
    private ManchesterOWLSyntaxParser manParser;
    private IMappingTransformer<String> expr;
    private IFormatModifierFunction defaultModifier;
    private List<String> defaultModifierArgs;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application object.
     */
    public SSSOMTCreateAxiomFunction(SSSOMTOwlApplication application) {
        app = application;

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        manParser = new ManchesterOWLSyntaxParserImpl(() -> config,
                app.getOntology().getOWLOntologyManager().getOWLDataFactory());
        manParser.setOWLEntityChecker(app.getEntityChecker());

        defaultModifier = new SSSOMTFormatFunction();
        defaultModifierArgs = new ArrayList<String>();
        defaultModifierArgs.add("<%s>");
    }

    private SSSOMTCreateAxiomFunction(SSSOMTOwlApplication application, ManchesterOWLSyntaxParser parser,
            IMappingTransformer<String> expression) {
        app = application;
        manParser = parser;
        expr = expression;
    }

    @Override
    public String getName() {
        return "create_axiom";
    }

    @Override
    public String getSignature() {
        return "SS?";
    }

    @Override
    public IMappingTransformer<OWLAxiom> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        String text = arguments.get(0);
        IMappingTransformer<OWLAxiom> t = null;

        try {
            expr = app.getFormatter().getTransformer(text, defaultModifier, defaultModifierArgs);
            testParse(expr);
            t = new SSSOMTCreateAxiomFunction(app, manParser, expr);
        } catch ( OWLParserException e ) {
            throw new SSSOMTransformError("Cannot parse Manchester expression \"%s\"", text);
        } catch ( IllegalArgumentException e ) {
            throw new SSSOMTransformError(e.getMessage());
        }

        return SSSOMTHelper.maybeCreateAnnotatedTransformer(app, t, keyedArguments);
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        return parse(mapping, expr);
    }

    private OWLAxiom parse(Mapping mapping, IMappingTransformer<String> expression) {
        app.getEntityChecker().addClass(mapping.getSubjectId());
        app.getEntityChecker().addClass(mapping.getObjectId());

        manParser.setStringToParse(expression.transform(mapping));
        return manParser.parseAxiom();
    }

    private void testParse(IMappingTransformer<String> expression) {
        Mapping dummy = new Mapping();
        dummy.setSubjectId("http://example.org/EX_0001");
        dummy.setObjectId("http://example.org/EX_0002");

        parse(dummy, expression);
    }
}
