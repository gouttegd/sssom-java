/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.cli;

import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * Converts the string value of the <code>--annotate</code> into a
 * AnnotationOption object.
 */
public class AnnotationOptionConverter implements ITypeConverter<AnnotationOption> {

    @Override
    public AnnotationOption convert(String value) throws TypeConversionException {
        boolean append = true;
        int sep = value.indexOf("+=", 0);
        if ( sep == -1 ) {
            sep = value.indexOf('=');
            append = false;
        }
        if ( sep == -1 ) {
            throw new TypeConversionException(String.format("Missing separator in '%s'", value));
        }

        String slotName = value.substring(0, sep);
        sep += append ? 2 : 1;
        String slotValue = sep == value.length() ? null : value.substring(sep);

        if ( append && slotValue == null ) {
            throw new TypeConversionException("Invalid append flag (+) with empty value");
        }

        Slot<MappingSet> slot = SlotHelper.getMappingSetHelper().getSlotByName(slotName);
        if ( slot == null ) {
            throw new TypeConversionException(String.format("Invalid slot name '%s'", slotName));
        }
        return new AnnotationOption(slot, slotValue, append);
    }

}
