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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.ISSSOMTFunction;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.semanticweb.owlapi.model.IRI;

/**
 * Represents the SSSOM/T-OWL filter function "is_a".
 * <p>
 * That function may be used to select mappings depending on whether a given
 * entity is a descendant of another entity, according to the class hierarchy
 * available in the helper ontology of the SSSOM/T-OWL application. It expects
 * two arguments:
 * <ul>
 * <li>the entity whose ascendency should be checked;
 * <li>the root of the hierarchy to check.
 * </ul>
 * <p>
 * For example, to select mappings whose subject is a descendant of
 * UBERON:0000105:
 * 
 * <pre>
 * is_a(%{subject_id}, UBERON:0000105) -&gt; ...;
 * </pre>
 * <p>
 * The function works both with classes and with object and data properties. If
 * the second argument is declared in the ontology as an object or data
 * property, the function will test whether the first argument is a subproperty;
 * otherwise, it will assume the second argument is a class, and will test
 * whether the first argument is a subclass. Use the {@code /type="class"} or
 * {@code /type="property"} optional argument to force treating the second
 * argument as a class or as a property, respectively.
 * <p>
 * The function can also be used to check whether an entity is a class, an
 * object property, a data property, or an annotation property, by setting the
 * second argument, respectively, to {@code owl:Thing} (or {@code owl:Class}),
 * {@code owl:topObjectProperty} (or {@code owl:ObjectProperty}),
 * {@code owl:topDataProperty} (or {@code owl:DataProperty}), or
 * {@code owl:AnnotationProperty}.
 */
public class SSSOMTIsAFunction implements ISSSOMTFunction<IMappingFilter>, IMappingFilter {

    private SSSOMTOwlApplication app;
    private IMappingTransformer<String> entity;
    private IMappingTransformer<String> parent;
    private OwlEntityType type;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM-T/OWL application object.
     */
    public SSSOMTIsAFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    private SSSOMTIsAFunction(SSSOMTOwlApplication application, String entity, String parent, OwlEntityType type) {
        app = application;
        this.entity = app.getFormatter().getTransformer(entity);
        this.parent = app.getFormatter().getTransformer(parent);
        this.type = type;
    }

    @Override
    public String getName() {
        return "is_a";
    }

    @Override
    public String getSignature() {
        return "SS";
    }

    @Override
    public IMappingFilter call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        OwlEntityType entityType = OwlEntityType.fromString(keyedArguments.get("type"));
        return new SSSOMTIsAFunction(app, arguments.get(0), arguments.get(1), entityType);
    }

    @Override
    public boolean filter(Mapping mapping) {
        String entityID = entity.transform(mapping);
        String parentID = parent.transform(mapping);

        if ( parentID.startsWith("http://www.w3.org/2002/07/owl#") ) {
            String type = parentID.substring(30);
            IRI entityIRI = IRI.create(entityID);
            if ( type.equals("Class") || type.equals("Thing") ) {
                return app.getOntology().containsClassInSignature(entityIRI);
            } else if ( type.equals("ObjectProperty") || type.equals("topObjectProperty") ) {
                return app.getOntology().containsObjectPropertyInSignature(entityIRI);
            } else if ( type.equals("AnnotationProperty") ) {
                return app.getOntology().containsAnnotationPropertyInSignature(entityIRI);
            } else if ( type.equals("DataProperty") || type.equals("topDataProperty") ) {
                return app.getOntology().containsDataPropertyInSignature(entityIRI);
            }
        }

        if ( type == OwlEntityType.ANY ) {
            IRI parentIRI = IRI.create(parentID);
            if ( app.getOntology().containsObjectPropertyInSignature(parentIRI)
                    || app.getOntology().containsDataPropertyInSignature(parentIRI) ) {
                type = OwlEntityType.PROPERTY;
            } else {
                type = OwlEntityType.CLASS;
            }
        }

        if ( type == OwlEntityType.PROPERTY ) {
            return app.getSubPropertiesOf(parentID).contains(entityID);
        } else {
            return app.getSubClassesOf(parentID).contains(entityID);
        }
    }

    private enum OwlEntityType {
        CLASS("class"),
        PROPERTY("property"),
        ANY("any");

        OwlEntityType(String name) {
        }

        static OwlEntityType fromString(String s) {
            if ( s == null ) {
                return OwlEntityType.ANY;
            }
            switch ( s ) {
            case "class":
                return OwlEntityType.CLASS;
            case "property":
                return OwlEntityType.PROPERTY;
            default:
                return OwlEntityType.ANY;
            }
        }
    }
}
