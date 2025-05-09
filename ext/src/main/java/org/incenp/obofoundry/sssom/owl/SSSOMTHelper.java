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
import java.util.List;
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
        switch ( keyedArguments.getOrDefault(ANNOTS_URIS_KEYWORD, "direct") ) {
        case "standard_map":
            mapper = new StandardMapMetadataTransformer();
            break;

        default:
            mapper = new DirectMetadataTransformer();
            break;
        }

        return new AnnotatedAxiomGenerator(application.getOntology(), innerTransformer, mapper, slots);
    }

    /**
     * Given an axiom generator, maybe wraps it into another generator that would
     * produce the same axiom but annotated with metadata from the mapping from
     * which the axiom is derived.
     * <p>
     * This method is similar to
     * {@link #maybeCreateAnnotatedTransformer(SSSOMTOwlApplication, IMappingTransformer, Map)},
     * but if the parameters map does not contain a <code>annots</code> key, it will
     * look for an optional position argument instead. This is for backwards
     * compatibility only, new SSSOM/T-OWL function should not use this method.
     * 
     * @param application      The SSSOM/T-OWL application.
     * @param innerTransformer The original axiom generator.
     * @param keyedArguments   The named parameters given to a SSSOM/T-OWL function.
     * @param arguments        The positional arguments given to a SSSOM/T-OWL
     *                         function.
     * @param nArgs            The number of non-optional arguments expected by the
     *                         function.
     * @return The wrapped axiom generator, or the original generator if axiom
     *         annotations were not requested in the given parameters or positional
     *         arguments.
     */
    public static IMappingTransformer<OWLAxiom> maybeCreateAnnotatedTransformer(SSSOMTOwlApplication application,
            IMappingTransformer<OWLAxiom> innerTransformer, Map<String, String> keyedArguments, List<String> arguments,
            int nArgs) {
        if ( !keyedArguments.containsKey(ANNOTS_KEYWORD) && arguments.size() == nArgs + 1 ) {
            keyedArguments.put(ANNOTS_KEYWORD, arguments.get(nArgs));
        }
        return maybeCreateAnnotatedTransformer(application, innerTransformer, keyedArguments);
    }
}
