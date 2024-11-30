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

package org.incenp.obofoundry.sssom.uriexpr;

import java.util.HashMap;
import java.util.Map;

/**
 * A basic registry of URI Expression schemas.
 * <p>
 * For now, the sole purpose of this registry is to associate a “schema” (the
 * first half of a URI Expression, before the JSON-URL part) and a format with
 * an expansion template. More features may be added in the future.
 * <p>
 * This is only used by the SSSOM/T-OWL (ROBOT) application, so the only format
 * we need in the OWL Manchester Syntax format, but again, this may change in
 * the future.
 */
public class UriExpressionRegistry {

    private Map<String, String> templates = new HashMap<String, String>();

    /**
     * Registers a new template.
     * <p>
     * The template should be a string containing placeholders of the form
     * <code>{placeholder}</code>, where <em>placeholder</em> is the name of a slot
     * in the URI Expression.
     * 
     * @param schema   The name of the URI Expression schema.
     * @param format   An identifier for the format of the template.
     * @param template The template string.
     */
    public void registerTemplate(String schema, String format, String template) {
        templates.put(String.format("%s\f%s", schema, format), template);
    }

    /**
     * Gets a template in a given format for a URI Expression.
     * 
     * @param expression The URI Expression we want a template for. This method will
     *                   lookup the template associated with the expression’s
     *                   schema.
     * @param format     The requested format of the template.
     * @return The template, or {@code null} if there is no template for the
     *         expression’s schema in the requested format.
     */
    public String getTemplate(UriExpression expression, String format) {
        return templates.get(String.format("%s\f%s", expression.getSchema(), format));
    }

    /**
     * Applies a template to a URI Expression.
     * 
     * @param expression The expression whose values should be inserted into the
     *                   template. The template to apply will be automatically
     *                   lookup from the expression’s schema.
     * @param format     The requested format of the template.
     * @return A formatted string with the expression-derived values.
     */
    public String applyTemplate(UriExpression expression, String format) {
        String template = getTemplate(expression, format);

        if ( template != null ) {
            for ( String slotName : expression.getComponentNames() ) {
                String pattern = String.format("{%s}", slotName);
                template = template.replace(pattern, expression.getComponent(slotName));
            }
        }

        return template;
    }
}
