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

package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a <code>mapping set reference</code> object.
 * <p>
 * Automatically generated from the SSSOM LinkML schema.
 */
public class MappingSetReference  {
    @JsonProperty("mapping_set_id")
    @URI
    private String mappingSetId;

    @JsonProperty("mirror_from")
    @URI
    private String mirrorFrom;

    @JsonProperty("registry_confidence")
    private Double registryConfidence;

    @JsonProperty("mapping_set_group")
    private String mappingSetGroup;

    @JsonProperty("last_updated")
    private LocalDate lastUpdated;

    @JsonProperty("local_name")
    private String localName;

    private Map<String,ExtensionValue> extensions;

    /**
     * Creates a new empty instance.
     */
    public MappingSetReference() {
    }

    /**
     * Creates a new instance from the specified values.
     */
    protected MappingSetReference(final String mappingSetId,
            final String mirrorFrom,
            final Double registryConfidence,
            final String mappingSetGroup,
            final LocalDate lastUpdated,
            final String localName,
            final Map<String,ExtensionValue> extensions) {
        this.mappingSetId = mappingSetId;
        this.mirrorFrom = mirrorFrom;
        this.registryConfidence = registryConfidence;
        this.mappingSetGroup = mappingSetGroup;
        this.lastUpdated = lastUpdated;
        this.localName = localName;
        this.extensions = extensions;
    }

    /**
     * Gets the value of the <code>mapping_set_id</code> slot.
     */
    public String getMappingSetId() {
        return this.mappingSetId;
    }

    /**
     * Sets the value of the <code>mapping_set_id</code> slot.
     */
    public void setMappingSetId(final String value) {
        this.mappingSetId = value;
    }

    /**
     * Gets the value of the <code>mirror_from</code> slot.
     */
    public String getMirrorFrom() {
        return this.mirrorFrom;
    }

    /**
     * Sets the value of the <code>mirror_from</code> slot.
     */
    public void setMirrorFrom(final String value) {
        this.mirrorFrom = value;
    }

    /**
     * Gets the value of the <code>registry_confidence</code> slot.
     */
    public Double getRegistryConfidence() {
        return this.registryConfidence;
    }

    /**
     * Sets the value of the <code>registry_confidence</code> slot.
     */
    public void setRegistryConfidence(final Double value) {
        if ( value != null && value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for registry_confidence");
        }
        if ( value != null && value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for registry_confidence");
        }
        this.registryConfidence = value;
    }

    /**
     * Gets the value of the <code>mapping_set_group</code> slot.
     */
    public String getMappingSetGroup() {
        return this.mappingSetGroup;
    }

    /**
     * Sets the value of the <code>mapping_set_group</code> slot.
     */
    public void setMappingSetGroup(final String value) {
        this.mappingSetGroup = value;
    }

    /**
     * Gets the value of the <code>last_updated</code> slot.
     */
    public LocalDate getLastUpdated() {
        return this.lastUpdated;
    }

    /**
     * Sets the value of the <code>last_updated</code> slot.
     */
    public void setLastUpdated(final LocalDate value) {
        this.lastUpdated = value;
    }

    /**
     * Gets the value of the <code>local_name</code> slot.
     */
    public String getLocalName() {
        return this.localName;
    }

    /**
     * Sets the value of the <code>local_name</code> slot.
     */
    public void setLocalName(final String value) {
        this.localName = value;
    }

    /**
     * Gets the map of extension values.
     */
    public Map<String,ExtensionValue> getExtensions() {
        return extensions;
    }

    /**
     * Gets the map of extension values, optionally initializing the map if
     * needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty map if it happens to be {@code null}.
     * @return The map of extension values.
     */
    public Map<String,ExtensionValue> getExtensions(boolean set) {
        if ( extensions == null && set ) {
            extensions = new HashMap<>();
        }
        return extensions;
    }

    /**
     * Sets the map of extension values.
     */
    public void setExtensions(final Map<String,ExtensionValue> value) {
        this.extensions = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MappingSetReference(");
        if ( this.mappingSetId != null ) {
            sb.append("mapping_set_id=");
            sb.append(this.mappingSetId);
            sb.append(",");
        }
        if ( this.mirrorFrom != null ) {
            sb.append("mirror_from=");
            sb.append(this.mirrorFrom);
            sb.append(",");
        }
        if ( this.registryConfidence != null ) {
            sb.append("registry_confidence=");
            sb.append(this.registryConfidence);
            sb.append(",");
        }
        if ( this.mappingSetGroup != null ) {
            sb.append("mapping_set_group=");
            sb.append(this.mappingSetGroup);
            sb.append(",");
        }
        if ( this.lastUpdated != null ) {
            sb.append("last_updated=");
            sb.append(this.lastUpdated);
            sb.append(",");
        }
        if ( this.localName != null ) {
            sb.append("local_name=");
            sb.append(this.localName);
            sb.append(",");
        }
        if ( extensions != null && !extensions.isEmpty() ) {
            sb.append("extensions={");
            for ( Map.Entry<String, ExtensionValue> entry : extensions.entrySet() ) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append(",");
            }
            sb.append("}");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if ( o == this ) return true;
        if ( !(o instanceof MappingSetReference) ) return false;
        final MappingSetReference other = (MappingSetReference) o;
        if ( !other.canEqual((Object) this)) return false;
        if ( this.mappingSetId == null ? other.mappingSetId != null : !this.mappingSetId.equals(other.mappingSetId)) return false;
        if ( this.mirrorFrom == null ? other.mirrorFrom != null : !this.mirrorFrom.equals(other.mirrorFrom)) return false;
        if ( this.registryConfidence == null ? other.registryConfidence != null : !this.registryConfidence.equals(other.registryConfidence)) return false;
        if ( this.mappingSetGroup == null ? other.mappingSetGroup != null : !this.mappingSetGroup.equals(other.mappingSetGroup)) return false;
        if ( this.lastUpdated == null ? other.lastUpdated != null : !this.lastUpdated.equals(other.lastUpdated)) return false;
        if ( this.localName == null ? other.localName != null : !this.localName.equals(other.localName)) return false;
        if ( this.extensions == null ? other.extensions != null : !this.extensions.equals(other.extensions)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MappingSetReference;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.mappingSetId == null ? 43 : this.mappingSetId.hashCode());
        result = result * PRIME + (this.mirrorFrom == null ? 43 : this.mirrorFrom.hashCode());
        result = result * PRIME + (this.registryConfidence == null ? 43 : this.registryConfidence.hashCode());
        result = result * PRIME + (this.mappingSetGroup == null ? 43 : this.mappingSetGroup.hashCode());
        result = result * PRIME + (this.lastUpdated == null ? 43 : this.lastUpdated.hashCode());
        result = result * PRIME + (this.localName == null ? 43 : this.localName.hashCode());
        result = result * PRIME + (this.extensions == null ? 43 : this.extensions.hashCode());
        return result;
    }

    public static class MappingSetReferenceBuilder {
        private String mappingSetId;
        private String mirrorFrom;
        private Double registryConfidence;
        private String mappingSetGroup;
        private LocalDate lastUpdated;
        private String localName;
        private Map<String,ExtensionValue> extensions;

        MappingSetReferenceBuilder() {
        }

        public MappingSetReference.MappingSetReferenceBuilder mappingSetId(final String mappingSetId) {
            this.mappingSetId = mappingSetId;
            return this;
        }

        public MappingSetReference.MappingSetReferenceBuilder mirrorFrom(final String mirrorFrom) {
            this.mirrorFrom = mirrorFrom;
            return this;
        }

        public MappingSetReference.MappingSetReferenceBuilder registryConfidence(final Double registryConfidence) {
            this.registryConfidence = registryConfidence;
            return this;
        }

        public MappingSetReference.MappingSetReferenceBuilder mappingSetGroup(final String mappingSetGroup) {
            this.mappingSetGroup = mappingSetGroup;
            return this;
        }

        public MappingSetReference.MappingSetReferenceBuilder lastUpdated(final LocalDate lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public MappingSetReference.MappingSetReferenceBuilder localName(final String localName) {
            this.localName = localName;
            return this;
        }

        public MappingSetReference.MappingSetReferenceBuilder extensions(final Map<String,ExtensionValue> extensions) {
            this.extensions = extensions;
            return this;
        }

        public MappingSetReference build() {
            return new MappingSetReference(this.mappingSetId,
                this.mirrorFrom,
                this.registryConfidence,
                this.mappingSetGroup,
                this.lastUpdated,
                this.localName,
                this.extensions);
        }

        public String toString() {
            return "MappingSetReference.MappingSetReferenceBuilder(mappingSetId=" + this.mappingSetId
                + ", mirrorFrom=" + this.mirrorFrom
                + ", registryConfidence=" + this.registryConfidence
                + ", mappingSetGroup=" + this.mappingSetGroup
                + ", lastUpdated=" + this.lastUpdated
                + ", localName=" + this.localName
                + ", extensions=" + this.extensions + ")";
        }
    }

    public static MappingSetReference.MappingSetReferenceBuilder builder() {
        return new MappingSetReference.MappingSetReferenceBuilder();
    }

    public MappingSetReference.MappingSetReferenceBuilder toBuilder() {
        return new MappingSetReference.MappingSetReferenceBuilder()
            .mappingSetId(this.mappingSetId)
            .mirrorFrom(this.mirrorFrom)
            .registryConfidence(this.registryConfidence)
            .mappingSetGroup(this.mappingSetGroup)
            .lastUpdated(this.lastUpdated)
            .localName(this.localName)
            .extensions(this.extensions);
    }
}
