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
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom;

import java.io.IOException;
import java.util.Map;

import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Version;

/**
 * Base class to read a mapping set from any of the supported serialisation
 * formats.
 * <p>
 * This class should be derived into specialised classes that implement a
 * precise serialisation format.
 */
public abstract class SSSOMReader {

    protected ExtraMetadataPolicy extraPolicy = ExtraMetadataPolicy.NONE;
    protected PropagationPolicy propagationPolicy = PropagationPolicy.NeverReplace;
    protected Version assumedVersion = Version.SSSOM_1_0;
    private boolean withValidation = true;

    /**
     * Sets the policy to deal with non-standard metadata in the input file.
     * 
     * @param policy The policy instructing the parser about what to do when
     *               encountering non-standard metadata. The default policy is
     *               {@link ExtraMetadataPolicy#NONE}.
     */
    public void setExtraMetadataPolicy(ExtraMetadataPolicy policy) {
        extraPolicy = policy;
    }

    /**
     * Enables or disables the propagation of "propagatable slots".
     * 
     * @param enabled {@code False} to disable propagation; it is enabled by
     *                default.
     */
    public void setPropagationEnabled(boolean enabled) {
        propagationPolicy = enabled ? PropagationPolicy.NeverReplace : PropagationPolicy.Disabled;
    }

    /**
     * Sets the version of the SSSOM specification that the reader should assume the
     * set is compliant with, if the set does not have a {@code sssom_version} slot.
     * The default is to assume that the set is compliant with version 1.0, as
     * mandated by the specification.
     * <p>
     * This parameter has no effect if the set does have a {@code sssom_version}
     * slot: in that case, the set will be expected to be compliant with the version
     * indicated in that slot.
     * 
     * @param version The SSSOM version a set is assumed to be compliant with.
     */
    public void setAssumedVersion(Version version) {
        assumedVersion = version;
    }

    /**
     * Enables or disables post-parsing validation of mappings.
     * 
     * @param enabled {@code False} to disable validation; it is enabled by default.
     */
    public void setValidationEnabled(boolean enabled) {
        withValidation = enabled;
    }

    /**
     * Declares all prefix names in the specified map. Prefix names declared here
     * complement the declarations from the file’s own prefix map, allowing the
     * reader to parse a file with an incomplete prefix map.
     * <p>
     * It is up to concrete implementations to actually do something with that map.
     * Some of them may ignore it.
     * 
     * @param map The prefix map to use.
     */
    public void fillPrefixMap(Map<String, String> map) {
    }

    /**
     * Reads a mapping set from the source file.
     * 
     * @return A complete SSSOM mapping set.
     * @throws SSSOMFormatException If encountering invalid SSSOM data.
     * @throws IOException          If any kind of non-SSSOM-related I/O error
     *                              occurs.
     */
    public abstract MappingSet read() throws SSSOMFormatException, IOException;

    /**
     * Validates a mapping set. This method performs all post-parsing checks to
     * ensure the mapping set is valid according to the SSSOM specification.
     * 
     * @param mappingSet The set to validate. It should be called by the
     *                   {@code read} method in concrete derived classes.
     * @throws SSSOMFormatException If the mapping set is invalid.
     */
    protected void validate(MappingSet mappingSet) throws SSSOMFormatException {
        if ( withValidation ) {
            Validator validator = new Validator();
            validator.check(mappingSet);
        }
    }
}
