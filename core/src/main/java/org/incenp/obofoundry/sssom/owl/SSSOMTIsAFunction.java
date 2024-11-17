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
 */
public class SSSOMTIsAFunction implements ISSSOMTFunction<IMappingFilter>, IMappingFilter {

    private SSSOMTOwlApplication app;
    private IMappingTransformer<String> entity;
    private IMappingTransformer<String> parent;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM-T/OWL application object.
     */
    public SSSOMTIsAFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    private SSSOMTIsAFunction(SSSOMTOwlApplication application, String entity, String parent) {
        app = application;
        this.entity = app.getFormatter().getTransformer(entity);
        this.parent = app.getFormatter().getTransformer(parent);
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
        return new SSSOMTIsAFunction(app, arguments.get(0), arguments.get(1));
    }

    @Override
    public boolean filter(Mapping mapping) {
        return app.getSubClassesOf(parent.transform(mapping)).contains(entity.transform(mapping));
    }

}
