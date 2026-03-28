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
 * Represents a <code>mapping registry</code> object.
 * <p>
 * Automatically generated from the SSSOM LinkML schema.
 */
public class MappingRegistry  {
    @JsonProperty("mapping_registry_id")
    @EntityReference
    private String mappingRegistryId;

    @JsonProperty("mapping_registry_title")
    private String mappingRegistryTitle;

    @JsonProperty("mapping_registry_description")
    private String mappingRegistryDescription;

    @URI
    private List<String> imports;

    @JsonProperty("mapping_set_references")
    private List<MappingSetReference> mappingSetReferences;

    @URI
    private String documentation;

    @URI
    private String homepage;

    @JsonProperty("issue_tracker")
    @URI
    private String issueTracker;

    private Map<String,ExtensionValue> extensions;

    /**
     * Creates a new empty instance.
     */
    public MappingRegistry() {
    }

    /**
     * Creates a new instance from the specified values.
     */
    protected MappingRegistry(final String mappingRegistryId,
            final String mappingRegistryTitle,
            final String mappingRegistryDescription,
            final List<String> imports,
            final List<MappingSetReference> mappingSetReferences,
            final String documentation,
            final String homepage,
            final String issueTracker,
            final Map<String,ExtensionValue> extensions) {
        this.mappingRegistryId = mappingRegistryId;
        this.mappingRegistryTitle = mappingRegistryTitle;
        this.mappingRegistryDescription = mappingRegistryDescription;
        this.imports = imports;
        this.mappingSetReferences = mappingSetReferences;
        this.documentation = documentation;
        this.homepage = homepage;
        this.issueTracker = issueTracker;
        this.extensions = extensions;
    }

    /**
     * Gets the value of the <code>mapping_registry_id</code> slot.
     */
    public String getMappingRegistryId() {
        return this.mappingRegistryId;
    }

    /**
     * Sets the value of the <code>mapping_registry_id</code> slot.
     */
    public void setMappingRegistryId(final String value) {
        this.mappingRegistryId = value;
    }

    /**
     * Gets the value of the <code>mapping_registry_title</code> slot.
     */
    public String getMappingRegistryTitle() {
        return this.mappingRegistryTitle;
    }

    /**
     * Sets the value of the <code>mapping_registry_title</code> slot.
     */
    public void setMappingRegistryTitle(final String value) {
        this.mappingRegistryTitle = value;
    }

    /**
     * Gets the value of the <code>mapping_registry_description</code> slot.
     */
    public String getMappingRegistryDescription() {
        return this.mappingRegistryDescription;
    }

    /**
     * Sets the value of the <code>mapping_registry_description</code> slot.
     */
    public void setMappingRegistryDescription(final String value) {
        this.mappingRegistryDescription = value;
    }

    /**
     * Gets the value of the <code>imports</code> slot.
     */
    public List<String> getImports() {
        return this.imports;
    }

    /**
     * Gets the list of <code>imports</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of imports values.
     */
    public List<String> getImports(boolean set) {
        if ( imports == null && set ) {
            imports = new ArrayList<>();
        }
        return imports;
    }

    /**
     * Sets the value of the <code>imports</code> slot.
     */
    public void setImports(final List<String> value) {
        this.imports = value;
    }

    /**
     * Gets the value of the <code>mapping_set_references</code> slot.
     */
    public List<MappingSetReference> getMappingSetReferences() {
        return this.mappingSetReferences;
    }

    /**
     * Gets the list of <code>mapping_set_references</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of mapping_set_references values.
     */
    public List<MappingSetReference> getMappingSetReferences(boolean set) {
        if ( mappingSetReferences == null && set ) {
            mappingSetReferences = new ArrayList<>();
        }
        return mappingSetReferences;
    }

    /**
     * Sets the value of the <code>mapping_set_references</code> slot.
     */
    public void setMappingSetReferences(final List<MappingSetReference> value) {
        this.mappingSetReferences = value;
    }

    /**
     * Gets the value of the <code>documentation</code> slot.
     */
    public String getDocumentation() {
        return this.documentation;
    }

    /**
     * Sets the value of the <code>documentation</code> slot.
     */
    public void setDocumentation(final String value) {
        this.documentation = value;
    }

    /**
     * Gets the value of the <code>homepage</code> slot.
     */
    public String getHomepage() {
        return this.homepage;
    }

    /**
     * Sets the value of the <code>homepage</code> slot.
     */
    public void setHomepage(final String value) {
        this.homepage = value;
    }

    /**
     * Gets the value of the <code>issue_tracker</code> slot.
     */
    public String getIssueTracker() {
        return this.issueTracker;
    }

    /**
     * Sets the value of the <code>issue_tracker</code> slot.
     */
    public void setIssueTracker(final String value) {
        this.issueTracker = value;
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
        sb.append("MappingRegistry(");
        if ( this.mappingRegistryId != null ) {
            sb.append("mapping_registry_id=");
            sb.append(this.mappingRegistryId);
            sb.append(",");
        }
        if ( this.mappingRegistryTitle != null ) {
            sb.append("mapping_registry_title=");
            sb.append(this.mappingRegistryTitle);
            sb.append(",");
        }
        if ( this.mappingRegistryDescription != null ) {
            sb.append("mapping_registry_description=");
            sb.append(this.mappingRegistryDescription);
            sb.append(",");
        }
        if ( this.imports != null ) {
            sb.append("imports=");
            sb.append(this.imports);
            sb.append(",");
        }
        if ( this.mappingSetReferences != null ) {
            sb.append("mapping_set_references=");
            sb.append(this.mappingSetReferences);
            sb.append(",");
        }
        if ( this.documentation != null ) {
            sb.append("documentation=");
            sb.append(this.documentation);
            sb.append(",");
        }
        if ( this.homepage != null ) {
            sb.append("homepage=");
            sb.append(this.homepage);
            sb.append(",");
        }
        if ( this.issueTracker != null ) {
            sb.append("issue_tracker=");
            sb.append(this.issueTracker);
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
        if ( !(o instanceof MappingRegistry) ) return false;
        final MappingRegistry other = (MappingRegistry) o;
        if ( !other.canEqual((Object) this)) return false;
        if ( this.mappingRegistryId == null ? other.mappingRegistryId != null : !this.mappingRegistryId.equals(other.mappingRegistryId)) return false;
        if ( this.mappingRegistryTitle == null ? other.mappingRegistryTitle != null : !this.mappingRegistryTitle.equals(other.mappingRegistryTitle)) return false;
        if ( this.mappingRegistryDescription == null ? other.mappingRegistryDescription != null : !this.mappingRegistryDescription.equals(other.mappingRegistryDescription)) return false;
        if ( this.imports == null ? other.imports != null : !this.imports.equals(other.imports)) return false;
        if ( this.mappingSetReferences == null ? other.mappingSetReferences != null : !this.mappingSetReferences.equals(other.mappingSetReferences)) return false;
        if ( this.documentation == null ? other.documentation != null : !this.documentation.equals(other.documentation)) return false;
        if ( this.homepage == null ? other.homepage != null : !this.homepage.equals(other.homepage)) return false;
        if ( this.issueTracker == null ? other.issueTracker != null : !this.issueTracker.equals(other.issueTracker)) return false;
        if ( this.extensions == null ? other.extensions != null : !this.extensions.equals(other.extensions)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MappingRegistry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.mappingRegistryId == null ? 43 : this.mappingRegistryId.hashCode());
        result = result * PRIME + (this.mappingRegistryTitle == null ? 43 : this.mappingRegistryTitle.hashCode());
        result = result * PRIME + (this.mappingRegistryDescription == null ? 43 : this.mappingRegistryDescription.hashCode());
        result = result * PRIME + (this.imports == null ? 43 : this.imports.hashCode());
        result = result * PRIME + (this.mappingSetReferences == null ? 43 : this.mappingSetReferences.hashCode());
        result = result * PRIME + (this.documentation == null ? 43 : this.documentation.hashCode());
        result = result * PRIME + (this.homepage == null ? 43 : this.homepage.hashCode());
        result = result * PRIME + (this.issueTracker == null ? 43 : this.issueTracker.hashCode());
        result = result * PRIME + (this.extensions == null ? 43 : this.extensions.hashCode());
        return result;
    }

    public static class MappingRegistryBuilder {
        private String mappingRegistryId;
        private String mappingRegistryTitle;
        private String mappingRegistryDescription;
        private List<String> imports;
        private List<MappingSetReference> mappingSetReferences;
        private String documentation;
        private String homepage;
        private String issueTracker;
        private Map<String,ExtensionValue> extensions;

        MappingRegistryBuilder() {
        }

        public MappingRegistry.MappingRegistryBuilder mappingRegistryId(final String mappingRegistryId) {
            this.mappingRegistryId = mappingRegistryId;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder mappingRegistryTitle(final String mappingRegistryTitle) {
            this.mappingRegistryTitle = mappingRegistryTitle;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder mappingRegistryDescription(final String mappingRegistryDescription) {
            this.mappingRegistryDescription = mappingRegistryDescription;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder imports(final List<String> imports) {
            this.imports = imports;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder mappingSetReferences(final List<MappingSetReference> mappingSetReferences) {
            this.mappingSetReferences = mappingSetReferences;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder documentation(final String documentation) {
            this.documentation = documentation;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder homepage(final String homepage) {
            this.homepage = homepage;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder issueTracker(final String issueTracker) {
            this.issueTracker = issueTracker;
            return this;
        }

        public MappingRegistry.MappingRegistryBuilder extensions(final Map<String,ExtensionValue> extensions) {
            this.extensions = extensions;
            return this;
        }

        public MappingRegistry build() {
            return new MappingRegistry(this.mappingRegistryId,
                this.mappingRegistryTitle,
                this.mappingRegistryDescription,
                this.imports,
                this.mappingSetReferences,
                this.documentation,
                this.homepage,
                this.issueTracker,
                this.extensions);
        }

        public String toString() {
            return "MappingRegistry.MappingRegistryBuilder(mappingRegistryId=" + this.mappingRegistryId
                + ", mappingRegistryTitle=" + this.mappingRegistryTitle
                + ", mappingRegistryDescription=" + this.mappingRegistryDescription
                + ", imports=" + this.imports
                + ", mappingSetReferences=" + this.mappingSetReferences
                + ", documentation=" + this.documentation
                + ", homepage=" + this.homepage
                + ", issueTracker=" + this.issueTracker
                + ", extensions=" + this.extensions + ")";
        }
    }

    public static MappingRegistry.MappingRegistryBuilder builder() {
        return new MappingRegistry.MappingRegistryBuilder();
    }

    public MappingRegistry.MappingRegistryBuilder toBuilder() {
        return new MappingRegistry.MappingRegistryBuilder()
            .mappingRegistryId(this.mappingRegistryId)
            .mappingRegistryTitle(this.mappingRegistryTitle)
            .mappingRegistryDescription(this.mappingRegistryDescription)
            .imports(this.imports)
            .mappingSetReferences(this.mappingSetReferences)
            .documentation(this.documentation)
            .homepage(this.homepage)
            .issueTracker(this.issueTracker)
            .extensions(this.extensions);
    }
}
