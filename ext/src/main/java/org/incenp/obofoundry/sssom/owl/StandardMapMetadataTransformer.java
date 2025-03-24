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

package org.incenp.obofoundry.sssom.owl;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.Slot;
import org.incenp.obofoundry.sssom.transform.IMetadataTransformer;
import org.semanticweb.owlapi.model.IRI;

/**
 * A class to transform mapping metadata slots into into their mapped IRIs
 * according to the SSSOM specification.
 * <p>
 * This class differs from the {@link DirectMetadataTransformer} class in that
 * it takes into account the value of the <code>slot_uri</code> field for the
 * slot in the SSSOM schema, if the slot has such a field. For example, when
 * applied to the <code>creator_id</code> slot, this class will return the IRI
 * for <code>dcterms:creator</code>, instead of <code>sssom:creator_id</code>.
 */
public class StandardMapMetadataTransformer implements IMetadataTransformer<Mapping, IRI> {

    @Override
    public IRI transform(Slot<Mapping> slot) {
        return IRI.create(slot.getURI());
    }

}
