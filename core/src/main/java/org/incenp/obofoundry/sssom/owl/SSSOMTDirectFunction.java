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

package org.incenp.obofoundry.sssom.owl;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Represents the SSSOM/T-OWL generator function "direct".
 * <p>
 * That function creates axioms that are the direct “OWL reification” of the
 * mappings, as per the serialisation rules set forth in the SSSOM
 * specification.
 * <p>
 * The function accepts an optional argument, which if present should be a list
 * of SSSOM metadata slots. The generated axiom will be annotated with the value
 * of those slots. If that argument is not present, it defaults to
 * <code>metadata,-mapping_cardinality</code>, indicating that all available
 * metadata slots should be turned into annotations, except
 * <code>mapping_cardinality</code>.
 * 
 * @see <a href="https://mapping-commons.github.io/sssom/spec-formats-owl/">The
 *      OWL/RDF serialisation format in the SSSOM specification</a>
 */
public class SSSOMTDirectFunction implements ISSSOMTFunction<IMappingTransformer<OWLAxiom>> {

    private SSSOMTOwlApplication app;

    public SSSOMTDirectFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    @Override
    public String getName() {
        return "direct";
    }

    @Override
    public String getSignature() {
        return "S?";
    }

    @Override
    public IMappingTransformer<OWLAxiom> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return app.createAnnotatedTransformer(new DirectAxiomGenerator(app.getOntology()),
                arguments.isEmpty() ? "metadata,-mapping_cardinality" : arguments.get(0));
    }

}