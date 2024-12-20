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

package org.incenp.obofoundry.sssom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.slots.SlotVisitorBase;

/**
 * Comparator for mapping objects. This class allows to sort mappings in the
 * order recommended by the SSSOM specification.
 * <p>
 * The recommended order is obtained by sorting mappings on each slot, in the
 * order they are listed in the specification. That is, mappings are compared
 * first on their subject IDs, then on their subject labels, then on their
 * subject categories, then on their predicate IDs, etc.
 */
public class DefaultMappingComparator implements Comparator<Mapping> {

    private StringifyVisitor visitor = new StringifyVisitor();

    @Override
    public int compare(Mapping o1, Mapping o2) {
        // We create a rough string representation of the mappings that we can then just
        // compare directly.
        String s1 = stringifyMapping(o1);
        String s2 = stringifyMapping(o2);

        return s1.compareTo(s2);
    }

    private String stringifyMapping(Mapping m) {
        visitor.strings.clear();
        SlotHelper.getMappingHelper().visitSlots(m, visitor, true);
        return String.join(",", visitor.strings);
    }

    private class StringifyVisitor extends SlotVisitorBase<Mapping> {

        List<String> strings = new ArrayList<String>();

        @Override
        public void visit(Slot<Mapping> slot, Mapping mapping, Object value) {
            strings.add(value != null ? value.toString() : "");
        }
    }
}
