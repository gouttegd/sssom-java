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

import java.util.Collection;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.slots.SlotHelper;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.IMetadataTransformer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * A helper class intended to host some methods used throughout the SSSOM/T-OWL
 * code.
 */
public class SSSOMTHelper {

    /**
     * The name of the parameter used to specify the annotations to generate on a
     * mapping-derived axiom.
     */
    public final static String ANNOTS_KEYWORD = "annots";

    /**
     * The name of the parameter used to specify how SSSOM fields are translated to
     * OWL annotation property IRIs.
     */
    public final static String ANNOTS_URIS_KEYWORD = "annots_uris";

    /**
     * Given an axiom generator, maybe wraps it into another generator that would
     * produce the same axiom but annotated with metadata from the mapping from
     * which the axiom is derived.
     * 
     * @param application      The SSSOM/T-OWL application.
     * @param innerTransformer The original axiom generator.
     * @param keyedArguments   The named parameters given to a SSSOM/T-OWL function.
     *                         This method will look for the <code>annots</code> and
     *                         <code>annots_uris</code> keys to determine whether
     *                         generated axioms should be annotated and how.
     * @return The wrapped axiom generator, or the original generator if axiom
     *         annotations were not requested in the given parameters.
     */
    public static IMappingTransformer<OWLAxiom> maybeCreateAnnotatedTransformer(SSSOMTOwlApplication application,
            IMappingTransformer<OWLAxiom> innerTransformer, Map<String, String> keyedArguments) {
        String annotSpec = keyedArguments.get(ANNOTS_KEYWORD);
        if ( annotSpec == null ) {
            return innerTransformer;
        }

        Collection<String> slots = SlotHelper.getMappingSlotList(annotSpec.replaceAll("( |\r|\n|\t)", ""));
        IMetadataTransformer<Mapping, IRI> mapper;
        switch ( keyedArguments.getOrDefault(ANNOTS_URIS_KEYWORD, "standard_map") ) {
        case "direct":
            mapper = new DirectMetadataTransformer<Mapping>();
            break;

        default:
            mapper = new StandardMapMetadataTransformer<Mapping>();
            break;
        }

        return new AnnotatedAxiomGenerator(application.getOntology(), innerTransformer, mapper, slots);
    }
}
