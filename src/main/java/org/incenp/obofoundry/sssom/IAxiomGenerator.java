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

import org.incenp.obofoundry.sssom.model.Mapping;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Translate a single SSSOM mapping into an OWL axiom.
 */
public interface IAxiomGenerator {

    /**
     * Generate an axiom that is the OWL translation of the specified mapping.
     * 
     * @param mapping The mapping to translate.
     * @return The generated axiom.
     */
    public OWLAxiom generateAxiom(Mapping mapping);
}
