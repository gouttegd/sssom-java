/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents the SSSOM/T filter function "is_duplicate".
 * <p>
 * That function may be be used to select mappings for which a mapping-derived
 * value is the same as another mapping. It expects one argument, which is the
 * mapping-derived value to check against.
 * <p>
 * For example, to select mappings that have the same subject ID as a previous
 * mapping:
 * 
 * <pre>
 * is_duplicate(%{subject_id}) -&gt; ...;
 * </pre>
 * 
 * <p>
 * This could be used in conjunction with the <code>stop()</code> preprocessor
 * to drop mappings that are considered "duplicates". For example, to drop all
 * mappings that have the same subject/predicate/object triple except the first
 * one:
 * 
 * <pre>
 * is_duplicate("%{subject_id}%{predicate_id}%{object_id}") -&gt; stop();
 * </pre>
 * 
 * <p>
 * Or to drop all mappings that are entirely identical, except the first one
 * (using the <code>hash</code> special substitution):
 * 
 * <pre>
 * is_duplicate(%{hash}) -&gt; stop();
 * </pre>
 */
public class SSSOMTDuplicateFunction implements ISSSOMTFunction<IMappingFilter>, IMappingFilter {

    private HashSet<String> keySet;
    private IMappingTransformer<String> keyGen;
    private MappingFormatter formatter;

    /**
     * Creates a new instance.
     * 
     * @param <T>         The type of objects produced by the application.
     * @param application The application this function is a part of.
     */
    public <T> SSSOMTDuplicateFunction(SSSOMTransformApplication<T> application) {
        formatter = application.getFormatter();
    }

    private SSSOMTDuplicateFunction(IMappingTransformer<String> key) {
        this.keyGen = key;
        keySet = new HashSet<>();
    }

    @Override
    public String getName() {
        return "is_duplicate";
    }

    @Override
    public String getSignature() {
        return "S";
    }

    @Override
    public IMappingFilter call(List<String> arguments, Map<String, String> keyedArguments) throws SSSOMTransformError {
        return new SSSOMTDuplicateFunction(formatter.getTransformer(arguments.get(0)));
    }

    @Override
    public boolean filter(Mapping mapping) {
        String key = keyGen.transform(mapping);
        if ( keySet.contains(key) ) {
            return true;
        }
        keySet.add(key);
        return false;
    }
}
