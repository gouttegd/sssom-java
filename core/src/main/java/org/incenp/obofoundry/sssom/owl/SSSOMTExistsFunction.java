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
 * Represents the SSSOM/T-OWL filter function "exists".
 * <p>
 * That function may be used to select mappings depending on the existence of a
 * given entity in the helper ontology of the SSSOM/T-OWL application. It
 * expects one argument which is the name of the entity whose existence should
 * be checked.
 * <p>
 * For example, to select mappings whose subject exists in the ontology:
 * 
 * <pre>
 * exists(%{subject_id}) -&gt; ...;
 * </pre>
 */
public class SSSOMTExistsFunction implements ISSSOMTFunction<IMappingFilter>, IMappingFilter {

    private SSSOMTOwlApplication app;
    private IMappingTransformer<String> entity;

    /**
     * Creates a new instance.
     * 
     * @param application The SSSOM/T-OWL application object.
     */
    public SSSOMTExistsFunction(SSSOMTOwlApplication application) {
        app = application;
    }

    private SSSOMTExistsFunction(SSSOMTOwlApplication application, IMappingTransformer<String> entity) {
        app = application;
        this.entity = entity;
    }

    @Override
    public String getName() {
        return "exists";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public IMappingFilter call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        return new SSSOMTExistsFunction(app, app.getFormatter().getTransformer(arguments.get(0)));
    }

    @Override
    public boolean filter(Mapping mapping) {
        return app.classExists(entity.transform(mapping));
    }

}
