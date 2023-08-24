/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A helper class to track <em>variables</em> that can have a different value
 * depending on whether the subject (or the object) of a mapping belongs to a
 * given set.
 */
public class VariableManager {

    private Map<String, Variable> variables = new HashMap<String, Variable>();

    /**
     * Defines a new variable to track.
     * 
     * @param name  The name of the new variable.
     * @param value The variable's default value.
     */
    public void addVariable(String name, String value) {
        variables.put(name, new Variable(value));
    }

    /**
     * Sets the value of a variable when a mapping has a subject ID in a given set.
     * 
     * @param name     The name of the variable to set.
     * @param value    The value to assign to the variable.
     * @param subjects The set of IDs to compare a mapping's subject ID to.
     */
    public void setVariableValueForSubjects(String name, String value, Set<String> subjects) {
        Variable v = variables.get(name);
        if ( v == null ) {
            throw new IllegalArgumentException(String.format("Undefined variable: %s", name));
        }

        for ( String subject : subjects ) {
            v.setValueForSubject(value, subject);
        }
    }

    /**
     * Sets the value of a variable when a mapping has an object ID in a given set.
     * 
     * @param name    The name of the variable to set.
     * @param value   The value to assign to the variable.
     * @param objects The set of IDs to compare a mapping's object ID to.
     */
    public void setVariableValueForObjects(String name, String value, Set<String> objects) {
        Variable v = variables.get(name);
        if ( v == null ) {
            throw new IllegalArgumentException(String.format("Undefined variable: %s", name));
        }

        for ( String object : objects ) {
            v.setValueForObject(value, object);
        }
    }

    /**
     * Gets the value of a variable for a given mapping.
     * 
     * @param name    The name of the variable to lookup.
     * @param mapping The mapping for which to get the variable's value.
     * @return The value of the variable, which may be the value dependent on the
     *         mapping's subject, the value dependent on the mapping's object, or
     *         the default value.
     */
    public String expandVariable(String name, Mapping mapping) {
        Variable v = variables.get(name);
        if ( v == null ) {
            throw new IllegalArgumentException(String.format("Undefined variable: %s", name));
        }
        return v.getValueForMapping(mapping);
    }

    private class Variable {
        String defValue;
        Map<String, String> subjectSpecificValues = new HashMap<String, String>();
        Map<String, String> objectSpecificValues = new HashMap<String, String>();

        Variable(String defaultValue) {
            defValue = defaultValue;
        }

        void setValueForSubject(String value, String subject) {
            subjectSpecificValues.put(subject, value);
        }

        void setValueForObject(String value, String object) {
            objectSpecificValues.put(object, value);
        }

        String getValueForMapping(Mapping mapping) {
            String value = subjectSpecificValues.get(mapping.getSubjectId());
            if ( value == null ) {
                value = objectSpecificValues.get(mapping.getObjectId());
            }
            if ( value == null ) {
                value = defValue;
            }

            return value;
        }
    }

}
