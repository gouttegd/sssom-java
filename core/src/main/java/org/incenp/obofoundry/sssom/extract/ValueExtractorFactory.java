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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.extract;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;

/**
 * Builds {@link IValueExtractor} instances from a string representation.
 */
public class ValueExtractorFactory {

    /**
     * Parses an “extractor expression” into a {@link IValueExtractor}.
     * <p>
     * This is the public interface for the whole “extractor expression” feature.
     * 
     * @param expression The expression representing the piece of content to extract
     *                   from a mapping set.
     * @return An object that can extract the desired piece of content.
     * @throws ExtractorSyntaxException If the provided expression is invalid.
     */
    public IValueExtractor parse(String expression) throws ExtractorSyntaxException {
        if (expression.startsWith("set.") && expression.length() > 4) {
            return parseSetExtractor(expression.substring(4));
        } else if (expression.startsWith("mapping.") && expression.length() > 8) {
            return parseMappingExtractor(0, expression.substring(8));
        } else if (expression.startsWith("mapping(") && expression.length() > 8) {
            String[] items = expression.split("\\.", 2);
            if (items.length == 2) {
                int mappingNo = getNumber(parseParenValue(items[0], 8));
                return parseMappingExtractor(mappingNo, items[1]);
            }
        }

        throw new ExtractorSyntaxException();
    }

    /*
     * Parses a "set." expression.
     * 
     * Accepts "slot(SLOTNAME[, N])" and "extension(PROPERTY)" expressions.
     */
    private IValueExtractor parseSetExtractor(String expression) throws ExtractorSyntaxException {
        if ( expression.startsWith("slot(") && expression.length() > 5 ) {
            String[] params = parseParenValue(expression, 5).split(", ?", 2);
            int itemNo = params.length == 2 ? getNumber(params[1]) : 0;
            Slot<MappingSet> slot = SlotHelper.getMappingSetHelper().getSlotByName(params[0]);
            if ( slot == null ) {
                throw new ExtractorSyntaxException("Invalid slot name: %s", params[0]);
            }
            return new MappingSetSlotExtractor(slot, itemNo);
        } else if ( expression.startsWith("extension(") && expression.length() > 10 ) {
            String property = parseParenValue(expression, 10);
            return new MappingSetExtensionExtractor(property);
        }

        throw new ExtractorSyntaxException();
    }

    /*
     * Parses a "mapping(N)." expression.
     * 
     * Accepts:
     * 
     * - "slot(SLOTNAME[, N])"
     * 
     * - "extension(PROPERTY)"
     * 
     * - "special(sexpr)"
     * 
     * - "special(hash)"
     */
    private IValueExtractor parseMappingExtractor(int mappingNo, String expression)
            throws ExtractorSyntaxException {
        if ( expression.startsWith("slot(") && expression.length() > 5 ) {
            String[] params = parseParenValue(expression, 5).split(", ?", 2);
            int itemNo = params.length == 2 ? getNumber(params[1]) : 0;
            Slot<Mapping> slot = SlotHelper.getMappingHelper().getSlotByName(params[0]);
            if ( slot == null ) {
                throw new ExtractorSyntaxException("Invalid slot name: %s", params[0]);
            }
            return new MappingSlotExtractor(mappingNo, slot, itemNo);
        } else if ( expression.startsWith("extension(") && expression.length() > 10 ) {
            String property = parseParenValue(expression, 10);
            return new MappingExtensionExtractor(mappingNo, property);
        } else if ( expression.equals("special(sexpr)") ) {
            return new SExpressionExtractor(mappingNo);
        } else if ( expression.equals("special(hash)") ) {
            return new HashExtractor(mappingNo);
        }

        throw new ExtractorSyntaxException();
    }

    /*
     * Parses a value enclosed in parentheses.
     * 
     * Given an expression and the index of the opening paren, checks for the
     * presence of a closing paren and returns what is between them.
     */
    private String parseParenValue(String expression, int offset) throws ExtractorSyntaxException {
        int len = expression.length();
        if ( expression.charAt(len - 1) == ')' ) {
            return expression.substring(offset, len - 1);
        }

        throw new ExtractorSyntaxException("Invalid parenthesised expression: %s", expression);
    }

    /*
     * Parses an integer value. If strictly positive, assume the value is 1-based,
     * so shift by -1 to make it 0-based.
     */
    private int getNumber(String expression) throws ExtractorSyntaxException {
        try {
            int n = Integer.parseInt(expression);
            return n > 0 ? n - 1 : n;
        } catch ( NumberFormatException e ) {
            throw new ExtractorSyntaxException("Invalid index: %s", expression);
        }
    }
}
