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

package org.incenp.obofoundry.sssom.transform;

import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.HashType;
import org.incenp.obofoundry.sssom.MappingHasher;
import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * Represents the SSSOM/T standard preprocessor function "invert".
 * <p>
 * This function simply returns an inverted copy of the original mapping. It
 * takes one optional argument, which is the predicate to use in the inverted
 * mapping. Placeholders are supported in this argument.
 * <p>
 * If called without any argument, the function will invert the mapping if it
 * knows the correct inverse predicate; if it does not, the mapping will be
 * dropped.
 * <p>
 * The function accepts two flags:
 * <ul>
 * <li><code>/fill_justification</code>: if set to <code>yes</code>, the
 * <code>mapping_justification</code> slot of the inverted mapping is set to
 * <code>semapv:MappingInversion</code>;
 * <li><code>/fill_derived_from</code>: if set to <code>yes</code>, the
 * <a href="https://ts4nfdi.github.io/mapping-sameness-identifier/">Mapping
 * Sameness Identifier</a> of the original mapping is appended to the
 * <code>derived_from</code> slot of the inverted mapping.
 * </ul>
 */
public class SSSOMTInvertFunction
        implements ISSSOMTFunction<IMappingTransformer<Mapping>>, IMappingTransformer<Mapping> {

    private IMappingTransformer<String> predicate;
    private MappingFormatter formatter;
    private boolean fillJustification;
    private MappingHasher derivedFromHasher;

    public <T> SSSOMTInvertFunction(SSSOMTransformApplication<T> application) {
        formatter = application.getFormatter();
    }

    private SSSOMTInvertFunction(IMappingTransformer<String> predicate, boolean fillJustification,
            boolean fillDerivedFrom) {
        this.predicate = predicate;
        this.fillJustification = fillJustification;
        if ( fillDerivedFrom ) {
            derivedFromHasher = new MappingHasher(HashType.MAPPING_SAMENESS_ID);
        }
    }

    @Override
    public String getName() {
        return "invert";
    }

    @Override
    public String getSignature() {
        return "S?";
    }

    @Override
    public IMappingTransformer<Mapping> call(List<String> arguments, Map<String, String> keyedArguments)
            throws SSSOMTransformError {
        return new SSSOMTInvertFunction(arguments.size() == 1 ? formatter.getTransformer(arguments.get(0)) : null,
                getBoolean(keyedArguments.get("fill_justification")),
                getBoolean(keyedArguments.get("fill_derived_from")));
    }

    @Override
    public Mapping transform(Mapping mapping) {
        Mapping inverted = predicate != null ? mapping.invert(predicate.transform(mapping)) : mapping.invert();
        if ( inverted != null ) {
            if ( fillJustification ) {
                inverted.setMappingJustification("https://w3id.org/semapv/vocab/MappingInversion");
            }
            if ( derivedFromHasher != null ) {
                inverted.getDerivedFrom(true).add(derivedFromHasher.hash(mapping));
            }
        }
        return inverted;
    }

    private boolean getBoolean(String value) {
        if ( value == null ) {
            return false;
        } else if ( value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") ) {
            return true;
        } else {
            return false;
        }
    }
}
