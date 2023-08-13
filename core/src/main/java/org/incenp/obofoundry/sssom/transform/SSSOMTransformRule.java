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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a rule in the SSSOM/Transform language.
 * <p>
 * This class is basically an intermediate between the text representation of a
 * SSSOM/Transform rule and its corresponding effective implementation as a
 * {@link MappingProcessingRule} object. The <em>action</em> part is still in
 * text form, and must be further processed according to the specific needs of
 * the application to produce the final, applicable rule.
 * 
 * @see <a href="https://incenp.org/dvlpt/sssom-java/sssom-transform.html">The
 *      SSSOM/Transform language</a>
 */
public class SSSOMTransformRule {
    private IMappingFilter filter;
    private Set<String> tags = new HashSet<String>();
    private String actionName;
    private List<String> arguments = new ArrayList<String>();

    /**
     * Creates a new instance.
     * 
     * @param filter The filter part of the rule.
     * @param name   The name of the instruction in the action part of the rule.
     */
    public SSSOMTransformRule(IMappingFilter filter, String name) {
        this.filter = filter;
        actionName = name;
    }

    /**
     * Gets the rule's filter.
     * 
     * @return The filter part of the rule.
     */
    public IMappingFilter getFilter() {
        return filter;
    }

    /**
     * Gets the rule's tags.
     * 
     * @return The set of tags associated with the rule.
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Gets the name of the rule's instruction.
     * <p>
     * For example, if the rule is
     * 
     * <pre>
     * predicate==skos:exactMatch -> stop();
     * </pre>
     * <p>
     * the name is {@literal stop}.
     * 
     * @return The name of the instruction in the action part of the rule.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Gets the arguments passed to the rule's instruction.
     * 
     * @return The list of arguments for the rule's instruction; may be empty.
     */
    public List<String> getArguments() {
        return arguments;
    }
}
