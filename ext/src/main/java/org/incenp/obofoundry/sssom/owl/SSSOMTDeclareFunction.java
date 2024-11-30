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

import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;

/**
 * Represents the SSSOM/T-OWL "declare" function.
 * <p>
 * This function may be used to make sure that some entities are known to the
 * SSSOM/T-OWL application, before they can be used in {@code create_axiom}.
 * <p>
 * It expects an arbitrary number of arguments, each argument being the IRI of
 * an entity to declare. By default, entities are declared as OWL classes. To
 * declare entities of another type, use the <code>/type=</code> parameter.
 * <p>
 * For example, to declare an object property:
 * 
 * <pre>
 * declare(BFO:0000066, /type="object_property");
 * </pre>
 * 
 * <p>
 * All types of entities can be declared (<code>class</code>, which is the
 * default, <code>object_property</code>, <code>data_property</code>,
 * <code>individual</code>, <code>datatype</code>, and
 * <code>annotation_property</code>. Only <code>class</code> and
 * <code>object_property</code> are expected to be useful, though.
 */
public class SSSOMTDeclareFunction implements ISSSOMTFunction<Void> {

    private SSSOMTOwlApplication app;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application object.
     */
    public SSSOMTDeclareFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    @Override
    public String getName() {
        return "declare";
    }

    @Override
    public String getSignature() {
        return "S+";
    }

    @Override
    public Void call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        String type = keyedArguments.getOrDefault("type", "class");
        switch ( type ) {
        case "class":
            arguments.forEach((c) -> app.getEntityChecker().addClass(c));
            break;

        case "object_property":
            arguments.forEach((c) -> app.getEntityChecker().addObjectProperty(c));
            break;

        case "data_property":
            arguments.forEach((c) -> app.getEntityChecker().addDataproperty(c));
            break;

        case "individual":
            arguments.forEach((c) -> app.getEntityChecker().addIndividual(type));
            break;

        case "datatype":
            arguments.forEach((c) -> app.getEntityChecker().addDatatype(c));
            break;

        case "annotation_property":
            arguments.forEach((c) -> app.getEntityChecker().addAnnotationProperty(c));
        }
        return null;
    }
}
