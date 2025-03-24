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

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * Represents the SSSOM/T-OWL generator function "annotate".
 * <p>
 * That function creates a OWL annotation assertion axiom. It takes three
 * arguments:
 * <ul>
 * <li>the name of the entity to annotate;
 * <li>the name of the annotation property;
 * <li>the annotation value.
 * </ul>
 * 
 * <p>
 * All three arguments can contain placeholders so that their value can be
 * derived from the mapping the function is applied to. For example, to annotate
 * the entity that is the object of the current mapping:
 * 
 * <pre>
 * annotate(%{object_id}, MY:PROPERTY, "annotation value");
 * </pre>
 * 
 * <p>
 * Additionally, the function can accept three parameters:
 * <ul>
 * <li><code>/type=T</code>, where <em>T</em> is the type of the annotation
 * value; it defaults to <code>xsd:string</code>; any valid OWL2 datatype can be
 * specified, as well as the special value <code>iri</code> to indicate that the
 * annotation value should be treated as a IRI;
 * <li><code>/annots=A</code>, where <code>A</code> is a list of SSSOM metadata
 * fields to annotate the generated axiom with.
 * <li><code>/annots_uris=B</code>, where <code>B</code> dictates how metadata
 * fields are rendered into annotation properties (allowed values:
 * <code>direct</code>, <code>standard_map</code>; the default is
 * <code>direct</code>).
 * </ul>
 */
public class SSSOMTAnnotateFunction
        implements ISSSOMTFunction<IMappingTransformer<OWLAxiom>>, IMappingTransformer<OWLAxiom> {

    protected SSSOMTOwlApplication app;
    private OWLDataFactory factory;
    private IMappingTransformer<String> subject;
    private IMappingTransformer<String> property;
    private IMappingTransformer<String> value;
    private String type;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application.
     */
    public SSSOMTAnnotateFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    protected SSSOMTAnnotateFunction(SSSOMTOwlApplication application, String subject, String property, String value,
            String type) {
        app = application;
        factory = application.getOntology().getOWLOntologyManager().getOWLDataFactory();
        this.subject = application.getFormatter().getTransformer(subject);
        this.property = application.getFormatter().getTransformer(property);
        this.value = application.getFormatter().getTransformer(value);
        this.type = type;
    }

    @Override
    public String getName() {
        return "annotate";
    }

    @Override
    public String getSignature() {
        return "SSSS?";
    }

    @Override
    public IMappingTransformer<OWLAxiom> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        IMappingTransformer<OWLAxiom> t = new SSSOMTAnnotateFunction(app, arguments.get(0), arguments.get(1),
                arguments.get(2), keyedArguments.get("type"));
        return SSSOMTHelper.maybeCreateAnnotatedTransformer(app, t, keyedArguments);
    }

    @Override
    public OWLAxiom transform(Mapping mapping) {
        IRI subjectIRI = IRI.create(subject.transform(mapping));
        IRI propertyIRI = IRI.create(property.transform(mapping));
        OWLAnnotationValue val = null;

        if ( type == null ) {
            val = factory.getOWLLiteral(value.transform(mapping));
        } else if ( type.equalsIgnoreCase("iri") ) {
            val = IRI.create(value.transform(mapping));
        } else {
            val = factory.getOWLLiteral(value.transform(mapping), factory.getOWLDatatype(IRI.create(type)));
        }

        return factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(propertyIRI), subjectIRI, val);
    }
}
