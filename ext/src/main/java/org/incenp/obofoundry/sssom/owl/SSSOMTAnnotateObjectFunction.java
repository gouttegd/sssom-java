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

import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Represents the SSSOM/T-OWL generator function "annotate_object".
 * <p>
 * That function creates a OWL annotation assertion axiom on the object of the
 * mapping. It takes two arguments:
 * <ul>
 * <li>the name of the annotation property;
 * <li>the annotation value.
 * </ul>
 * 
 * @deprecated Maintained for backwards compatibility only; use
 *             <code>annotate(%{object_id}, ...);</code> instead.
 */
public class SSSOMTAnnotateObjectFunction extends SSSOMTAnnotateFunction {

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application object.
     */
    public SSSOMTAnnotateObjectFunction(SSSOMTOwlApplication application) {
        super(application);
    }

    @Override
    public String getName() {
        return "annotate_object";
    }

    @Override
    public String getSignature() {
        return "SSS?";
    }

    @Override
    public IMappingTransformer<OWLAxiom> call(List<String> arguments, Map<String, String> keyedArguments) {
        IMappingTransformer<OWLAxiom> t = new SSSOMTAnnotateFunction(app, "%{object_id}", arguments.get(0),
                arguments.get(1), null);
        return SSSOMTHelper.maybeCreateAnnotatedTransformer(app, t, keyedArguments, arguments, 2);
    }
}
