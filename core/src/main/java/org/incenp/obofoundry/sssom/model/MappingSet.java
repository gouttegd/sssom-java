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
 * Represents a <code>mapping set</code> object.
 * <p>
 * Automatically generated from the SSSOM LinkML schema.
 */
public class MappingSet  {
    @JsonProperty("sssom_version")
    @Versionable(addedIn = Version.SSSOM_1_1)
    private Version sssomVersion;

    @JsonProperty("curie_map")
    private Map<String,String> curieMap;

    private List<Mapping> mappings;

    @JsonProperty("mapping_set_id")
    @URI
    private String mappingSetId;

    @JsonProperty("mapping_set_version")
    @SlotURI("http://www.w3.org/2002/07/owl#versionInfo")
    private String mappingSetVersion;

    @JsonProperty("mapping_set_source")
    @SlotURI("http://www.w3.org/ns/prov#wasDerivedFrom")
    @URI
    private List<String> mappingSetSource;

    @JsonProperty("mapping_set_title")
    @SlotURI("http://purl.org/dc/terms/title")
    private String mappingSetTitle;

    @JsonProperty("mapping_set_description")
    @SlotURI("http://purl.org/dc/terms/description")
    private String mappingSetDescription;

    @JsonProperty("mapping_set_confidence")
    @Versionable(addedIn = Version.SSSOM_1_1)
    private Double mappingSetConfidence;

    @JsonProperty("creator_id")
    @EntityReference
    @SlotURI("http://purl.org/dc/terms/creator")
    private List<String> creatorId;

    @JsonProperty("creator_label")
    private List<String> creatorLabel;

    @SlotURI("http://purl.org/dc/terms/license")
    @URI
    private String license;

    @JsonProperty("subject_type")
    @Propagatable
    private EntityType subjectType;

    @JsonProperty("subject_source")
    @EntityReference
    @Propagatable
    private String subjectSource;

    @JsonProperty("subject_source_version")
    @Propagatable
    private String subjectSourceVersion;

    @JsonProperty("object_type")
    @Propagatable
    private EntityType objectType;

    @JsonProperty("object_source")
    @EntityReference
    @Propagatable
    private String objectSource;

    @JsonProperty("object_source_version")
    @Propagatable
    private String objectSourceVersion;

    @JsonProperty("predicate_type")
    @Propagatable
    @Versionable(addedIn = Version.SSSOM_1_1)
    private EntityType predicateType;

    @JsonProperty("mapping_provider")
    @Propagatable
    @URI
    private String mappingProvider;

    @JsonProperty("cardinality_scope")
    @Propagatable
    @Versionable(addedIn = Version.SSSOM_1_1)
    private List<String> cardinalityScope;

    @JsonProperty("mapping_tool")
    @Propagatable
    private String mappingTool;

    @JsonProperty("mapping_tool_id")
    @EntityReference
    @Propagatable
    @Versionable(addedIn = Version.SSSOM_1_1)
    private String mappingToolId;

    @JsonProperty("mapping_tool_version")
    @Propagatable
    private String mappingToolVersion;

    @JsonProperty("mapping_date")
    @Propagatable
    @SlotURI("http://purl.org/dc/terms/created")
    private LocalDate mappingDate;

    @JsonProperty("publication_date")
    @SlotURI("http://purl.org/dc/terms/issued")
    private LocalDate publicationDate;

    @JsonProperty("subject_match_field")
    @EntityReference
    @Propagatable
    private List<String> subjectMatchField;

    @JsonProperty("object_match_field")
    @EntityReference
    @Propagatable
    private List<String> objectMatchField;

    @JsonProperty("subject_preprocessing")
    @EntityReference
    @Propagatable
    private List<String> subjectPreprocessing;

    @JsonProperty("object_preprocessing")
    @EntityReference
    @Propagatable
    private List<String> objectPreprocessing;

    @JsonProperty("similarity_measure")
    @Propagatable
    @Versionable(addedIn = Version.SSSOM_1_1)
    private String similarityMeasure;

    @JsonProperty("curation_rule")
    @EntityReference
    @Propagatable
    @Versionable(addedIn = Version.SSSOM_1_1)
    private List<String> curationRule;

    @JsonProperty("curation_rule_text")
    @Propagatable
    @Versionable(addedIn = Version.SSSOM_1_1)
    private List<String> curationRuleText;

    @JsonProperty("see_also")
    @SlotURI("http://www.w3.org/2000/01/rdf-schema#seeAlso")
    @URI
    private List<String> seeAlso;

    @JsonProperty("issue_tracker")
    @URI
    private String issueTracker;

    private String other;

    @SlotURI("http://www.w3.org/2000/01/rdf-schema#comment")
    private String comment;

    @JsonProperty("extension_definitions")
    private List<ExtensionDefinition> extensionDefinitions;

    private Map<String,ExtensionValue> extensions;

    /**
     * Creates a new empty instance.
     */
    public MappingSet() {
    }

    /**
     * Creates a new instance from the specified values.
     */
    protected MappingSet(final Version sssomVersion,
            final Map<String,String> curieMap,
            final List<Mapping> mappings,
            final String mappingSetId,
            final String mappingSetVersion,
            final List<String> mappingSetSource,
            final String mappingSetTitle,
            final String mappingSetDescription,
            final Double mappingSetConfidence,
            final List<String> creatorId,
            final List<String> creatorLabel,
            final String license,
            final EntityType subjectType,
            final String subjectSource,
            final String subjectSourceVersion,
            final EntityType objectType,
            final String objectSource,
            final String objectSourceVersion,
            final EntityType predicateType,
            final String mappingProvider,
            final List<String> cardinalityScope,
            final String mappingTool,
            final String mappingToolId,
            final String mappingToolVersion,
            final LocalDate mappingDate,
            final LocalDate publicationDate,
            final List<String> subjectMatchField,
            final List<String> objectMatchField,
            final List<String> subjectPreprocessing,
            final List<String> objectPreprocessing,
            final String similarityMeasure,
            final List<String> curationRule,
            final List<String> curationRuleText,
            final List<String> seeAlso,
            final String issueTracker,
            final String other,
            final String comment,
            final List<ExtensionDefinition> extensionDefinitions,
            final Map<String,ExtensionValue> extensions) {
        this.sssomVersion = sssomVersion;
        this.curieMap = curieMap;
        this.mappings = mappings;
        this.mappingSetId = mappingSetId;
        this.mappingSetVersion = mappingSetVersion;
        this.mappingSetSource = mappingSetSource;
        this.mappingSetTitle = mappingSetTitle;
        this.mappingSetDescription = mappingSetDescription;
        this.mappingSetConfidence = mappingSetConfidence;
        this.creatorId = creatorId;
        this.creatorLabel = creatorLabel;
        this.license = license;
        this.subjectType = subjectType;
        this.subjectSource = subjectSource;
        this.subjectSourceVersion = subjectSourceVersion;
        this.objectType = objectType;
        this.objectSource = objectSource;
        this.objectSourceVersion = objectSourceVersion;
        this.predicateType = predicateType;
        this.mappingProvider = mappingProvider;
        this.cardinalityScope = cardinalityScope;
        this.mappingTool = mappingTool;
        this.mappingToolId = mappingToolId;
        this.mappingToolVersion = mappingToolVersion;
        this.mappingDate = mappingDate;
        this.publicationDate = publicationDate;
        this.subjectMatchField = subjectMatchField;
        this.objectMatchField = objectMatchField;
        this.subjectPreprocessing = subjectPreprocessing;
        this.objectPreprocessing = objectPreprocessing;
        this.similarityMeasure = similarityMeasure;
        this.curationRule = curationRule;
        this.curationRuleText = curationRuleText;
        this.seeAlso = seeAlso;
        this.issueTracker = issueTracker;
        this.other = other;
        this.comment = comment;
        this.extensionDefinitions = extensionDefinitions;
        this.extensions = extensions;
    }

    /**
     * Gets the value of the <code>sssom_version</code> slot.
     */
    public Version getSssomVersion() {
        return this.sssomVersion;
    }

    /**
     * Sets the value of the <code>sssom_version</code> slot.
     */
    public void setSssomVersion(final Version value) {
        this.sssomVersion = value;
    }

    /**
     * Gets the value of the <code>curie_map</code> slot.
     */
    public Map<String,String> getCurieMap() {
        return this.curieMap;
    }

    /**
     * Gets the prefix map, optionally initializing the map if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty map if it happens to be {@code null}.
     * @return The prefix map.
     */
    public Map<String,String> getCurieMap(boolean set) {
        if ( curieMap == null && set ) {
            curieMap = new HashMap<>();
        }
        return curieMap;
    }

    /**
     * Sets the value of the <code>curie_map</code> slot.
     */
    public void setCurieMap(final Map<String,String> value) {
        this.curieMap = value;
    }

    /**
     * Gets the value of the <code>mappings</code> slot.
     */
    public List<Mapping> getMappings() {
        return this.mappings;
    }

    /**
     * Gets the list of <code>mappings</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of mappings values.
     */
    public List<Mapping> getMappings(boolean set) {
        if ( mappings == null && set ) {
            mappings = new ArrayList<>();
        }
        return mappings;
    }

    /**
     * Sets the value of the <code>mappings</code> slot.
     */
    public void setMappings(final List<Mapping> value) {
        this.mappings = value;
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
     * Gets the value of the <code>mapping_set_version</code> slot.
     */
    public String getMappingSetVersion() {
        return this.mappingSetVersion;
    }

    /**
     * Sets the value of the <code>mapping_set_version</code> slot.
     */
    public void setMappingSetVersion(final String value) {
        this.mappingSetVersion = value;
    }

    /**
     * Gets the value of the <code>mapping_set_source</code> slot.
     */
    public List<String> getMappingSetSource() {
        return this.mappingSetSource;
    }

    /**
     * Gets the list of <code>mapping_set_source</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of mapping_set_source values.
     */
    public List<String> getMappingSetSource(boolean set) {
        if ( mappingSetSource == null && set ) {
            mappingSetSource = new ArrayList<>();
        }
        return mappingSetSource;
    }

    /**
     * Sets the value of the <code>mapping_set_source</code> slot.
     */
    public void setMappingSetSource(final List<String> value) {
        this.mappingSetSource = value;
    }

    /**
     * Gets the value of the <code>mapping_set_title</code> slot.
     */
    public String getMappingSetTitle() {
        return this.mappingSetTitle;
    }

    /**
     * Sets the value of the <code>mapping_set_title</code> slot.
     */
    public void setMappingSetTitle(final String value) {
        this.mappingSetTitle = value;
    }

    /**
     * Gets the value of the <code>mapping_set_description</code> slot.
     */
    public String getMappingSetDescription() {
        return this.mappingSetDescription;
    }

    /**
     * Sets the value of the <code>mapping_set_description</code> slot.
     */
    public void setMappingSetDescription(final String value) {
        this.mappingSetDescription = value;
    }

    /**
     * Gets the value of the <code>mapping_set_confidence</code> slot.
     */
    public Double getMappingSetConfidence() {
        return this.mappingSetConfidence;
    }

    /**
     * Sets the value of the <code>mapping_set_confidence</code> slot.
     */
    public void setMappingSetConfidence(final Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for mapping_set_confidence");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for mapping_set_confidence");
        }
        this.mappingSetConfidence = value;
    }

    /**
     * Gets the value of the <code>creator_id</code> slot.
     */
    public List<String> getCreatorId() {
        return this.creatorId;
    }

    /**
     * Gets the list of <code>creator_id</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of creator_id values.
     */
    public List<String> getCreatorId(boolean set) {
        if ( creatorId == null && set ) {
            creatorId = new ArrayList<>();
        }
        return creatorId;
    }

    /**
     * Sets the value of the <code>creator_id</code> slot.
     */
    public void setCreatorId(final List<String> value) {
        this.creatorId = value;
    }

    /**
     * Gets the value of the <code>creator_label</code> slot.
     */
    public List<String> getCreatorLabel() {
        return this.creatorLabel;
    }

    /**
     * Gets the list of <code>creator_label</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of creator_label values.
     */
    public List<String> getCreatorLabel(boolean set) {
        if ( creatorLabel == null && set ) {
            creatorLabel = new ArrayList<>();
        }
        return creatorLabel;
    }

    /**
     * Sets the value of the <code>creator_label</code> slot.
     */
    public void setCreatorLabel(final List<String> value) {
        this.creatorLabel = value;
    }

    /**
     * Gets the value of the <code>license</code> slot.
     */
    public String getLicense() {
        return this.license;
    }

    /**
     * Sets the value of the <code>license</code> slot.
     */
    public void setLicense(final String value) {
        this.license = value;
    }

    /**
     * Gets the value of the <code>subject_type</code> slot.
     */
    public EntityType getSubjectType() {
        return this.subjectType;
    }

    /**
     * Sets the value of the <code>subject_type</code> slot.
     */
    public void setSubjectType(final EntityType value) {
        this.subjectType = value;
    }

    /**
     * Gets the value of the <code>subject_source</code> slot.
     */
    public String getSubjectSource() {
        return this.subjectSource;
    }

    /**
     * Sets the value of the <code>subject_source</code> slot.
     */
    public void setSubjectSource(final String value) {
        this.subjectSource = value;
    }

    /**
     * Gets the value of the <code>subject_source_version</code> slot.
     */
    public String getSubjectSourceVersion() {
        return this.subjectSourceVersion;
    }

    /**
     * Sets the value of the <code>subject_source_version</code> slot.
     */
    public void setSubjectSourceVersion(final String value) {
        this.subjectSourceVersion = value;
    }

    /**
     * Gets the value of the <code>object_type</code> slot.
     */
    public EntityType getObjectType() {
        return this.objectType;
    }

    /**
     * Sets the value of the <code>object_type</code> slot.
     */
    public void setObjectType(final EntityType value) {
        this.objectType = value;
    }

    /**
     * Gets the value of the <code>object_source</code> slot.
     */
    public String getObjectSource() {
        return this.objectSource;
    }

    /**
     * Sets the value of the <code>object_source</code> slot.
     */
    public void setObjectSource(final String value) {
        this.objectSource = value;
    }

    /**
     * Gets the value of the <code>object_source_version</code> slot.
     */
    public String getObjectSourceVersion() {
        return this.objectSourceVersion;
    }

    /**
     * Sets the value of the <code>object_source_version</code> slot.
     */
    public void setObjectSourceVersion(final String value) {
        this.objectSourceVersion = value;
    }

    /**
     * Gets the value of the <code>predicate_type</code> slot.
     */
    public EntityType getPredicateType() {
        return this.predicateType;
    }

    /**
     * Sets the value of the <code>predicate_type</code> slot.
     */
    public void setPredicateType(final EntityType value) {
        this.predicateType = value;
    }

    /**
     * Gets the value of the <code>mapping_provider</code> slot.
     */
    public String getMappingProvider() {
        return this.mappingProvider;
    }

    /**
     * Sets the value of the <code>mapping_provider</code> slot.
     */
    public void setMappingProvider(final String value) {
        this.mappingProvider = value;
    }

    /**
     * Gets the value of the <code>cardinality_scope</code> slot.
     */
    public List<String> getCardinalityScope() {
        return this.cardinalityScope;
    }

    /**
     * Gets the list of <code>cardinality_scope</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of cardinality_scope values.
     */
    public List<String> getCardinalityScope(boolean set) {
        if ( cardinalityScope == null && set ) {
            cardinalityScope = new ArrayList<>();
        }
        return cardinalityScope;
    }

    /**
     * Sets the value of the <code>cardinality_scope</code> slot.
     */
    public void setCardinalityScope(final List<String> value) {
        this.cardinalityScope = value;
    }

    /**
     * Gets the value of the <code>mapping_tool</code> slot.
     */
    public String getMappingTool() {
        return this.mappingTool;
    }

    /**
     * Sets the value of the <code>mapping_tool</code> slot.
     */
    public void setMappingTool(final String value) {
        this.mappingTool = value;
    }

    /**
     * Gets the value of the <code>mapping_tool_id</code> slot.
     */
    public String getMappingToolId() {
        return this.mappingToolId;
    }

    /**
     * Sets the value of the <code>mapping_tool_id</code> slot.
     */
    public void setMappingToolId(final String value) {
        this.mappingToolId = value;
    }

    /**
     * Gets the value of the <code>mapping_tool_version</code> slot.
     */
    public String getMappingToolVersion() {
        return this.mappingToolVersion;
    }

    /**
     * Sets the value of the <code>mapping_tool_version</code> slot.
     */
    public void setMappingToolVersion(final String value) {
        this.mappingToolVersion = value;
    }

    /**
     * Gets the value of the <code>mapping_date</code> slot.
     */
    public LocalDate getMappingDate() {
        return this.mappingDate;
    }

    /**
     * Sets the value of the <code>mapping_date</code> slot.
     */
    public void setMappingDate(final LocalDate value) {
        this.mappingDate = value;
    }

    /**
     * Gets the value of the <code>publication_date</code> slot.
     */
    public LocalDate getPublicationDate() {
        return this.publicationDate;
    }

    /**
     * Sets the value of the <code>publication_date</code> slot.
     */
    public void setPublicationDate(final LocalDate value) {
        this.publicationDate = value;
    }

    /**
     * Gets the value of the <code>subject_match_field</code> slot.
     */
    public List<String> getSubjectMatchField() {
        return this.subjectMatchField;
    }

    /**
     * Gets the list of <code>subject_match_field</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of subject_match_field values.
     */
    public List<String> getSubjectMatchField(boolean set) {
        if ( subjectMatchField == null && set ) {
            subjectMatchField = new ArrayList<>();
        }
        return subjectMatchField;
    }

    /**
     * Sets the value of the <code>subject_match_field</code> slot.
     */
    public void setSubjectMatchField(final List<String> value) {
        this.subjectMatchField = value;
    }

    /**
     * Gets the value of the <code>object_match_field</code> slot.
     */
    public List<String> getObjectMatchField() {
        return this.objectMatchField;
    }

    /**
     * Gets the list of <code>object_match_field</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of object_match_field values.
     */
    public List<String> getObjectMatchField(boolean set) {
        if ( objectMatchField == null && set ) {
            objectMatchField = new ArrayList<>();
        }
        return objectMatchField;
    }

    /**
     * Sets the value of the <code>object_match_field</code> slot.
     */
    public void setObjectMatchField(final List<String> value) {
        this.objectMatchField = value;
    }

    /**
     * Gets the value of the <code>subject_preprocessing</code> slot.
     */
    public List<String> getSubjectPreprocessing() {
        return this.subjectPreprocessing;
    }

    /**
     * Gets the list of <code>subject_preprocessing</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of subject_preprocessing values.
     */
    public List<String> getSubjectPreprocessing(boolean set) {
        if ( subjectPreprocessing == null && set ) {
            subjectPreprocessing = new ArrayList<>();
        }
        return subjectPreprocessing;
    }

    /**
     * Sets the value of the <code>subject_preprocessing</code> slot.
     */
    public void setSubjectPreprocessing(final List<String> value) {
        this.subjectPreprocessing = value;
    }

    /**
     * Gets the value of the <code>object_preprocessing</code> slot.
     */
    public List<String> getObjectPreprocessing() {
        return this.objectPreprocessing;
    }

    /**
     * Gets the list of <code>object_preprocessing</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of object_preprocessing values.
     */
    public List<String> getObjectPreprocessing(boolean set) {
        if ( objectPreprocessing == null && set ) {
            objectPreprocessing = new ArrayList<>();
        }
        return objectPreprocessing;
    }

    /**
     * Sets the value of the <code>object_preprocessing</code> slot.
     */
    public void setObjectPreprocessing(final List<String> value) {
        this.objectPreprocessing = value;
    }

    /**
     * Gets the value of the <code>similarity_measure</code> slot.
     */
    public String getSimilarityMeasure() {
        return this.similarityMeasure;
    }

    /**
     * Sets the value of the <code>similarity_measure</code> slot.
     */
    public void setSimilarityMeasure(final String value) {
        this.similarityMeasure = value;
    }

    /**
     * Gets the value of the <code>curation_rule</code> slot.
     */
    public List<String> getCurationRule() {
        return this.curationRule;
    }

    /**
     * Gets the list of <code>curation_rule</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of curation_rule values.
     */
    public List<String> getCurationRule(boolean set) {
        if ( curationRule == null && set ) {
            curationRule = new ArrayList<>();
        }
        return curationRule;
    }

    /**
     * Sets the value of the <code>curation_rule</code> slot.
     */
    public void setCurationRule(final List<String> value) {
        this.curationRule = value;
    }

    /**
     * Gets the value of the <code>curation_rule_text</code> slot.
     */
    public List<String> getCurationRuleText() {
        return this.curationRuleText;
    }

    /**
     * Gets the list of <code>curation_rule_text</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of curation_rule_text values.
     */
    public List<String> getCurationRuleText(boolean set) {
        if ( curationRuleText == null && set ) {
            curationRuleText = new ArrayList<>();
        }
        return curationRuleText;
    }

    /**
     * Sets the value of the <code>curation_rule_text</code> slot.
     */
    public void setCurationRuleText(final List<String> value) {
        this.curationRuleText = value;
    }

    /**
     * Gets the value of the <code>see_also</code> slot.
     */
    public List<String> getSeeAlso() {
        return this.seeAlso;
    }

    /**
     * Gets the list of <code>see_also</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of see_also values.
     */
    public List<String> getSeeAlso(boolean set) {
        if ( seeAlso == null && set ) {
            seeAlso = new ArrayList<>();
        }
        return seeAlso;
    }

    /**
     * Sets the value of the <code>see_also</code> slot.
     */
    public void setSeeAlso(final List<String> value) {
        this.seeAlso = value;
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
     * Gets the value of the <code>other</code> slot.
     */
    public String getOther() {
        return this.other;
    }

    /**
     * Sets the value of the <code>other</code> slot.
     */
    public void setOther(final String value) {
        this.other = value;
    }

    /**
     * Gets the value of the <code>comment</code> slot.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Sets the value of the <code>comment</code> slot.
     */
    public void setComment(final String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the <code>extension_definitions</code> slot.
     */
    public List<ExtensionDefinition> getExtensionDefinitions() {
        return this.extensionDefinitions;
    }

    /**
     * Gets the list of <code>extension_definitions</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of extension_definitions values.
     */
    public List<ExtensionDefinition> getExtensionDefinitions(boolean set) {
        if ( extensionDefinitions == null && set ) {
            extensionDefinitions = new ArrayList<>();
        }
        return extensionDefinitions;
    }

    /**
     * Sets the value of the <code>extension_definitions</code> slot.
     */
    public void setExtensionDefinitions(final List<ExtensionDefinition> value) {
        this.extensionDefinitions = value;
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
        sb.append("MappingSet(");
        if ( this.sssomVersion != null ) {
            sb.append("sssom_version=");
            sb.append(this.sssomVersion);
            sb.append(",");
        }
        if ( this.curieMap != null ) {
            sb.append("curie_map=");
            sb.append(this.curieMap);
            sb.append(",");
        }
        if ( this.mappings != null ) {
            sb.append("mappings=");
            sb.append(this.mappings);
            sb.append(",");
        }
        if ( this.mappingSetId != null ) {
            sb.append("mapping_set_id=");
            sb.append(this.mappingSetId);
            sb.append(",");
        }
        if ( this.mappingSetVersion != null ) {
            sb.append("mapping_set_version=");
            sb.append(this.mappingSetVersion);
            sb.append(",");
        }
        if ( this.mappingSetSource != null ) {
            sb.append("mapping_set_source=");
            sb.append(this.mappingSetSource);
            sb.append(",");
        }
        if ( this.mappingSetTitle != null ) {
            sb.append("mapping_set_title=");
            sb.append(this.mappingSetTitle);
            sb.append(",");
        }
        if ( this.mappingSetDescription != null ) {
            sb.append("mapping_set_description=");
            sb.append(this.mappingSetDescription);
            sb.append(",");
        }
        if ( this.mappingSetConfidence != null ) {
            sb.append("mapping_set_confidence=");
            sb.append(this.mappingSetConfidence);
            sb.append(",");
        }
        if ( this.creatorId != null ) {
            sb.append("creator_id=");
            sb.append(this.creatorId);
            sb.append(",");
        }
        if ( this.creatorLabel != null ) {
            sb.append("creator_label=");
            sb.append(this.creatorLabel);
            sb.append(",");
        }
        if ( this.license != null ) {
            sb.append("license=");
            sb.append(this.license);
            sb.append(",");
        }
        if ( this.subjectType != null ) {
            sb.append("subject_type=");
            sb.append(this.subjectType);
            sb.append(",");
        }
        if ( this.subjectSource != null ) {
            sb.append("subject_source=");
            sb.append(this.subjectSource);
            sb.append(",");
        }
        if ( this.subjectSourceVersion != null ) {
            sb.append("subject_source_version=");
            sb.append(this.subjectSourceVersion);
            sb.append(",");
        }
        if ( this.objectType != null ) {
            sb.append("object_type=");
            sb.append(this.objectType);
            sb.append(",");
        }
        if ( this.objectSource != null ) {
            sb.append("object_source=");
            sb.append(this.objectSource);
            sb.append(",");
        }
        if ( this.objectSourceVersion != null ) {
            sb.append("object_source_version=");
            sb.append(this.objectSourceVersion);
            sb.append(",");
        }
        if ( this.predicateType != null ) {
            sb.append("predicate_type=");
            sb.append(this.predicateType);
            sb.append(",");
        }
        if ( this.mappingProvider != null ) {
            sb.append("mapping_provider=");
            sb.append(this.mappingProvider);
            sb.append(",");
        }
        if ( this.cardinalityScope != null ) {
            sb.append("cardinality_scope=");
            sb.append(this.cardinalityScope);
            sb.append(",");
        }
        if ( this.mappingTool != null ) {
            sb.append("mapping_tool=");
            sb.append(this.mappingTool);
            sb.append(",");
        }
        if ( this.mappingToolId != null ) {
            sb.append("mapping_tool_id=");
            sb.append(this.mappingToolId);
            sb.append(",");
        }
        if ( this.mappingToolVersion != null ) {
            sb.append("mapping_tool_version=");
            sb.append(this.mappingToolVersion);
            sb.append(",");
        }
        if ( this.mappingDate != null ) {
            sb.append("mapping_date=");
            sb.append(this.mappingDate);
            sb.append(",");
        }
        if ( this.publicationDate != null ) {
            sb.append("publication_date=");
            sb.append(this.publicationDate);
            sb.append(",");
        }
        if ( this.subjectMatchField != null ) {
            sb.append("subject_match_field=");
            sb.append(this.subjectMatchField);
            sb.append(",");
        }
        if ( this.objectMatchField != null ) {
            sb.append("object_match_field=");
            sb.append(this.objectMatchField);
            sb.append(",");
        }
        if ( this.subjectPreprocessing != null ) {
            sb.append("subject_preprocessing=");
            sb.append(this.subjectPreprocessing);
            sb.append(",");
        }
        if ( this.objectPreprocessing != null ) {
            sb.append("object_preprocessing=");
            sb.append(this.objectPreprocessing);
            sb.append(",");
        }
        if ( this.similarityMeasure != null ) {
            sb.append("similarity_measure=");
            sb.append(this.similarityMeasure);
            sb.append(",");
        }
        if ( this.curationRule != null ) {
            sb.append("curation_rule=");
            sb.append(this.curationRule);
            sb.append(",");
        }
        if ( this.curationRuleText != null ) {
            sb.append("curation_rule_text=");
            sb.append(this.curationRuleText);
            sb.append(",");
        }
        if ( this.seeAlso != null ) {
            sb.append("see_also=");
            sb.append(this.seeAlso);
            sb.append(",");
        }
        if ( this.issueTracker != null ) {
            sb.append("issue_tracker=");
            sb.append(this.issueTracker);
            sb.append(",");
        }
        if ( this.other != null ) {
            sb.append("other=");
            sb.append(this.other);
            sb.append(",");
        }
        if ( this.comment != null ) {
            sb.append("comment=");
            sb.append(this.comment);
            sb.append(",");
        }
        if ( this.extensionDefinitions != null ) {
            sb.append("extension_definitions=");
            sb.append(this.extensionDefinitions);
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
        if ( !(o instanceof MappingSet) ) return false;
        final MappingSet other = (MappingSet) o;
        if ( !other.canEqual((Object) this)) return false;
        if ( this.sssomVersion == null ? other.sssomVersion != null : !this.sssomVersion.equals(other.sssomVersion)) return false;
        if ( this.curieMap == null ? other.curieMap != null : !this.curieMap.equals(other.curieMap)) return false;
        if ( this.mappings == null ? other.mappings != null : !this.mappings.equals(other.mappings)) return false;
        if ( this.mappingSetId == null ? other.mappingSetId != null : !this.mappingSetId.equals(other.mappingSetId)) return false;
        if ( this.mappingSetVersion == null ? other.mappingSetVersion != null : !this.mappingSetVersion.equals(other.mappingSetVersion)) return false;
        if ( this.mappingSetSource == null ? other.mappingSetSource != null : !this.mappingSetSource.equals(other.mappingSetSource)) return false;
        if ( this.mappingSetTitle == null ? other.mappingSetTitle != null : !this.mappingSetTitle.equals(other.mappingSetTitle)) return false;
        if ( this.mappingSetDescription == null ? other.mappingSetDescription != null : !this.mappingSetDescription.equals(other.mappingSetDescription)) return false;
        if ( this.mappingSetConfidence == null ? other.mappingSetConfidence != null : !this.mappingSetConfidence.equals(other.mappingSetConfidence)) return false;
        if ( this.creatorId == null ? other.creatorId != null : !this.creatorId.equals(other.creatorId)) return false;
        if ( this.creatorLabel == null ? other.creatorLabel != null : !this.creatorLabel.equals(other.creatorLabel)) return false;
        if ( this.license == null ? other.license != null : !this.license.equals(other.license)) return false;
        if ( this.subjectType == null ? other.subjectType != null : !this.subjectType.equals(other.subjectType)) return false;
        if ( this.subjectSource == null ? other.subjectSource != null : !this.subjectSource.equals(other.subjectSource)) return false;
        if ( this.subjectSourceVersion == null ? other.subjectSourceVersion != null : !this.subjectSourceVersion.equals(other.subjectSourceVersion)) return false;
        if ( this.objectType == null ? other.objectType != null : !this.objectType.equals(other.objectType)) return false;
        if ( this.objectSource == null ? other.objectSource != null : !this.objectSource.equals(other.objectSource)) return false;
        if ( this.objectSourceVersion == null ? other.objectSourceVersion != null : !this.objectSourceVersion.equals(other.objectSourceVersion)) return false;
        if ( this.predicateType == null ? other.predicateType != null : !this.predicateType.equals(other.predicateType)) return false;
        if ( this.mappingProvider == null ? other.mappingProvider != null : !this.mappingProvider.equals(other.mappingProvider)) return false;
        if ( this.cardinalityScope == null ? other.cardinalityScope != null : !this.cardinalityScope.equals(other.cardinalityScope)) return false;
        if ( this.mappingTool == null ? other.mappingTool != null : !this.mappingTool.equals(other.mappingTool)) return false;
        if ( this.mappingToolId == null ? other.mappingToolId != null : !this.mappingToolId.equals(other.mappingToolId)) return false;
        if ( this.mappingToolVersion == null ? other.mappingToolVersion != null : !this.mappingToolVersion.equals(other.mappingToolVersion)) return false;
        if ( this.mappingDate == null ? other.mappingDate != null : !this.mappingDate.equals(other.mappingDate)) return false;
        if ( this.publicationDate == null ? other.publicationDate != null : !this.publicationDate.equals(other.publicationDate)) return false;
        if ( this.subjectMatchField == null ? other.subjectMatchField != null : !this.subjectMatchField.equals(other.subjectMatchField)) return false;
        if ( this.objectMatchField == null ? other.objectMatchField != null : !this.objectMatchField.equals(other.objectMatchField)) return false;
        if ( this.subjectPreprocessing == null ? other.subjectPreprocessing != null : !this.subjectPreprocessing.equals(other.subjectPreprocessing)) return false;
        if ( this.objectPreprocessing == null ? other.objectPreprocessing != null : !this.objectPreprocessing.equals(other.objectPreprocessing)) return false;
        if ( this.similarityMeasure == null ? other.similarityMeasure != null : !this.similarityMeasure.equals(other.similarityMeasure)) return false;
        if ( this.curationRule == null ? other.curationRule != null : !this.curationRule.equals(other.curationRule)) return false;
        if ( this.curationRuleText == null ? other.curationRuleText != null : !this.curationRuleText.equals(other.curationRuleText)) return false;
        if ( this.seeAlso == null ? other.seeAlso != null : !this.seeAlso.equals(other.seeAlso)) return false;
        if ( this.issueTracker == null ? other.issueTracker != null : !this.issueTracker.equals(other.issueTracker)) return false;
        if ( this.other == null ? other.other != null : !this.other.equals(other.other)) return false;
        if ( this.comment == null ? other.comment != null : !this.comment.equals(other.comment)) return false;
        if ( this.extensionDefinitions == null ? other.extensionDefinitions != null : !this.extensionDefinitions.equals(other.extensionDefinitions)) return false;
        if ( this.extensions == null ? other.extensions != null : !this.extensions.equals(other.extensions)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MappingSet;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.sssomVersion == null ? 43 : this.sssomVersion.hashCode());
        result = result * PRIME + (this.curieMap == null ? 43 : this.curieMap.hashCode());
        result = result * PRIME + (this.mappings == null ? 43 : this.mappings.hashCode());
        result = result * PRIME + (this.mappingSetId == null ? 43 : this.mappingSetId.hashCode());
        result = result * PRIME + (this.mappingSetVersion == null ? 43 : this.mappingSetVersion.hashCode());
        result = result * PRIME + (this.mappingSetSource == null ? 43 : this.mappingSetSource.hashCode());
        result = result * PRIME + (this.mappingSetTitle == null ? 43 : this.mappingSetTitle.hashCode());
        result = result * PRIME + (this.mappingSetDescription == null ? 43 : this.mappingSetDescription.hashCode());
        result = result * PRIME + (this.mappingSetConfidence == null ? 43 : this.mappingSetConfidence.hashCode());
        result = result * PRIME + (this.creatorId == null ? 43 : this.creatorId.hashCode());
        result = result * PRIME + (this.creatorLabel == null ? 43 : this.creatorLabel.hashCode());
        result = result * PRIME + (this.license == null ? 43 : this.license.hashCode());
        result = result * PRIME + (this.subjectType == null ? 43 : this.subjectType.hashCode());
        result = result * PRIME + (this.subjectSource == null ? 43 : this.subjectSource.hashCode());
        result = result * PRIME + (this.subjectSourceVersion == null ? 43 : this.subjectSourceVersion.hashCode());
        result = result * PRIME + (this.objectType == null ? 43 : this.objectType.hashCode());
        result = result * PRIME + (this.objectSource == null ? 43 : this.objectSource.hashCode());
        result = result * PRIME + (this.objectSourceVersion == null ? 43 : this.objectSourceVersion.hashCode());
        result = result * PRIME + (this.predicateType == null ? 43 : this.predicateType.hashCode());
        result = result * PRIME + (this.mappingProvider == null ? 43 : this.mappingProvider.hashCode());
        result = result * PRIME + (this.cardinalityScope == null ? 43 : this.cardinalityScope.hashCode());
        result = result * PRIME + (this.mappingTool == null ? 43 : this.mappingTool.hashCode());
        result = result * PRIME + (this.mappingToolId == null ? 43 : this.mappingToolId.hashCode());
        result = result * PRIME + (this.mappingToolVersion == null ? 43 : this.mappingToolVersion.hashCode());
        result = result * PRIME + (this.mappingDate == null ? 43 : this.mappingDate.hashCode());
        result = result * PRIME + (this.publicationDate == null ? 43 : this.publicationDate.hashCode());
        result = result * PRIME + (this.subjectMatchField == null ? 43 : this.subjectMatchField.hashCode());
        result = result * PRIME + (this.objectMatchField == null ? 43 : this.objectMatchField.hashCode());
        result = result * PRIME + (this.subjectPreprocessing == null ? 43 : this.subjectPreprocessing.hashCode());
        result = result * PRIME + (this.objectPreprocessing == null ? 43 : this.objectPreprocessing.hashCode());
        result = result * PRIME + (this.similarityMeasure == null ? 43 : this.similarityMeasure.hashCode());
        result = result * PRIME + (this.curationRule == null ? 43 : this.curationRule.hashCode());
        result = result * PRIME + (this.curationRuleText == null ? 43 : this.curationRuleText.hashCode());
        result = result * PRIME + (this.seeAlso == null ? 43 : this.seeAlso.hashCode());
        result = result * PRIME + (this.issueTracker == null ? 43 : this.issueTracker.hashCode());
        result = result * PRIME + (this.other == null ? 43 : this.other.hashCode());
        result = result * PRIME + (this.comment == null ? 43 : this.comment.hashCode());
        result = result * PRIME + (this.extensionDefinitions == null ? 43 : this.extensionDefinitions.hashCode());
        result = result * PRIME + (this.extensions == null ? 43 : this.extensions.hashCode());
        return result;
    }

    public static class MappingSetBuilder {
        private Version sssomVersion;
        private Map<String,String> curieMap;
        private List<Mapping> mappings;
        private String mappingSetId;
        private String mappingSetVersion;
        private List<String> mappingSetSource;
        private String mappingSetTitle;
        private String mappingSetDescription;
        private Double mappingSetConfidence;
        private List<String> creatorId;
        private List<String> creatorLabel;
        private String license;
        private EntityType subjectType;
        private String subjectSource;
        private String subjectSourceVersion;
        private EntityType objectType;
        private String objectSource;
        private String objectSourceVersion;
        private EntityType predicateType;
        private String mappingProvider;
        private List<String> cardinalityScope;
        private String mappingTool;
        private String mappingToolId;
        private String mappingToolVersion;
        private LocalDate mappingDate;
        private LocalDate publicationDate;
        private List<String> subjectMatchField;
        private List<String> objectMatchField;
        private List<String> subjectPreprocessing;
        private List<String> objectPreprocessing;
        private String similarityMeasure;
        private List<String> curationRule;
        private List<String> curationRuleText;
        private List<String> seeAlso;
        private String issueTracker;
        private String other;
        private String comment;
        private List<ExtensionDefinition> extensionDefinitions;
        private Map<String,ExtensionValue> extensions;

        MappingSetBuilder() {
        }

        public MappingSet.MappingSetBuilder sssomVersion(final Version sssomVersion) {
            this.sssomVersion = sssomVersion;
            return this;
        }

        public MappingSet.MappingSetBuilder curieMap(final Map<String,String> curieMap) {
            this.curieMap = curieMap;
            return this;
        }

        public MappingSet.MappingSetBuilder mappings(final List<Mapping> mappings) {
            this.mappings = mappings;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingSetId(final String mappingSetId) {
            this.mappingSetId = mappingSetId;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingSetVersion(final String mappingSetVersion) {
            this.mappingSetVersion = mappingSetVersion;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingSetSource(final List<String> mappingSetSource) {
            this.mappingSetSource = mappingSetSource;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingSetTitle(final String mappingSetTitle) {
            this.mappingSetTitle = mappingSetTitle;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingSetDescription(final String mappingSetDescription) {
            this.mappingSetDescription = mappingSetDescription;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingSetConfidence(final Double mappingSetConfidence) {
            this.mappingSetConfidence = mappingSetConfidence;
            return this;
        }

        public MappingSet.MappingSetBuilder creatorId(final List<String> creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public MappingSet.MappingSetBuilder creatorLabel(final List<String> creatorLabel) {
            this.creatorLabel = creatorLabel;
            return this;
        }

        public MappingSet.MappingSetBuilder license(final String license) {
            this.license = license;
            return this;
        }

        public MappingSet.MappingSetBuilder subjectType(final EntityType subjectType) {
            this.subjectType = subjectType;
            return this;
        }

        public MappingSet.MappingSetBuilder subjectSource(final String subjectSource) {
            this.subjectSource = subjectSource;
            return this;
        }

        public MappingSet.MappingSetBuilder subjectSourceVersion(final String subjectSourceVersion) {
            this.subjectSourceVersion = subjectSourceVersion;
            return this;
        }

        public MappingSet.MappingSetBuilder objectType(final EntityType objectType) {
            this.objectType = objectType;
            return this;
        }

        public MappingSet.MappingSetBuilder objectSource(final String objectSource) {
            this.objectSource = objectSource;
            return this;
        }

        public MappingSet.MappingSetBuilder objectSourceVersion(final String objectSourceVersion) {
            this.objectSourceVersion = objectSourceVersion;
            return this;
        }

        public MappingSet.MappingSetBuilder predicateType(final EntityType predicateType) {
            this.predicateType = predicateType;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingProvider(final String mappingProvider) {
            this.mappingProvider = mappingProvider;
            return this;
        }

        public MappingSet.MappingSetBuilder cardinalityScope(final List<String> cardinalityScope) {
            this.cardinalityScope = cardinalityScope;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingTool(final String mappingTool) {
            this.mappingTool = mappingTool;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingToolId(final String mappingToolId) {
            this.mappingToolId = mappingToolId;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingToolVersion(final String mappingToolVersion) {
            this.mappingToolVersion = mappingToolVersion;
            return this;
        }

        public MappingSet.MappingSetBuilder mappingDate(final LocalDate mappingDate) {
            this.mappingDate = mappingDate;
            return this;
        }

        public MappingSet.MappingSetBuilder publicationDate(final LocalDate publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public MappingSet.MappingSetBuilder subjectMatchField(final List<String> subjectMatchField) {
            this.subjectMatchField = subjectMatchField;
            return this;
        }

        public MappingSet.MappingSetBuilder objectMatchField(final List<String> objectMatchField) {
            this.objectMatchField = objectMatchField;
            return this;
        }

        public MappingSet.MappingSetBuilder subjectPreprocessing(final List<String> subjectPreprocessing) {
            this.subjectPreprocessing = subjectPreprocessing;
            return this;
        }

        public MappingSet.MappingSetBuilder objectPreprocessing(final List<String> objectPreprocessing) {
            this.objectPreprocessing = objectPreprocessing;
            return this;
        }

        public MappingSet.MappingSetBuilder similarityMeasure(final String similarityMeasure) {
            this.similarityMeasure = similarityMeasure;
            return this;
        }

        public MappingSet.MappingSetBuilder curationRule(final List<String> curationRule) {
            this.curationRule = curationRule;
            return this;
        }

        public MappingSet.MappingSetBuilder curationRuleText(final List<String> curationRuleText) {
            this.curationRuleText = curationRuleText;
            return this;
        }

        public MappingSet.MappingSetBuilder seeAlso(final List<String> seeAlso) {
            this.seeAlso = seeAlso;
            return this;
        }

        public MappingSet.MappingSetBuilder issueTracker(final String issueTracker) {
            this.issueTracker = issueTracker;
            return this;
        }

        public MappingSet.MappingSetBuilder other(final String other) {
            this.other = other;
            return this;
        }

        public MappingSet.MappingSetBuilder comment(final String comment) {
            this.comment = comment;
            return this;
        }

        public MappingSet.MappingSetBuilder extensionDefinitions(final List<ExtensionDefinition> extensionDefinitions) {
            this.extensionDefinitions = extensionDefinitions;
            return this;
        }

        public MappingSet.MappingSetBuilder extensions(final Map<String,ExtensionValue> extensions) {
            this.extensions = extensions;
            return this;
        }

        public MappingSet build() {
            return new MappingSet(this.sssomVersion,
                this.curieMap,
                this.mappings,
                this.mappingSetId,
                this.mappingSetVersion,
                this.mappingSetSource,
                this.mappingSetTitle,
                this.mappingSetDescription,
                this.mappingSetConfidence,
                this.creatorId,
                this.creatorLabel,
                this.license,
                this.subjectType,
                this.subjectSource,
                this.subjectSourceVersion,
                this.objectType,
                this.objectSource,
                this.objectSourceVersion,
                this.predicateType,
                this.mappingProvider,
                this.cardinalityScope,
                this.mappingTool,
                this.mappingToolId,
                this.mappingToolVersion,
                this.mappingDate,
                this.publicationDate,
                this.subjectMatchField,
                this.objectMatchField,
                this.subjectPreprocessing,
                this.objectPreprocessing,
                this.similarityMeasure,
                this.curationRule,
                this.curationRuleText,
                this.seeAlso,
                this.issueTracker,
                this.other,
                this.comment,
                this.extensionDefinitions,
                this.extensions);
        }

        public String toString() {
            return "MappingSet.MappingSetBuilder(sssomVersion=" + this.sssomVersion
                + ", curieMap=" + this.curieMap
                + ", mappings=" + this.mappings
                + ", mappingSetId=" + this.mappingSetId
                + ", mappingSetVersion=" + this.mappingSetVersion
                + ", mappingSetSource=" + this.mappingSetSource
                + ", mappingSetTitle=" + this.mappingSetTitle
                + ", mappingSetDescription=" + this.mappingSetDescription
                + ", mappingSetConfidence=" + this.mappingSetConfidence
                + ", creatorId=" + this.creatorId
                + ", creatorLabel=" + this.creatorLabel
                + ", license=" + this.license
                + ", subjectType=" + this.subjectType
                + ", subjectSource=" + this.subjectSource
                + ", subjectSourceVersion=" + this.subjectSourceVersion
                + ", objectType=" + this.objectType
                + ", objectSource=" + this.objectSource
                + ", objectSourceVersion=" + this.objectSourceVersion
                + ", predicateType=" + this.predicateType
                + ", mappingProvider=" + this.mappingProvider
                + ", cardinalityScope=" + this.cardinalityScope
                + ", mappingTool=" + this.mappingTool
                + ", mappingToolId=" + this.mappingToolId
                + ", mappingToolVersion=" + this.mappingToolVersion
                + ", mappingDate=" + this.mappingDate
                + ", publicationDate=" + this.publicationDate
                + ", subjectMatchField=" + this.subjectMatchField
                + ", objectMatchField=" + this.objectMatchField
                + ", subjectPreprocessing=" + this.subjectPreprocessing
                + ", objectPreprocessing=" + this.objectPreprocessing
                + ", similarityMeasure=" + this.similarityMeasure
                + ", curationRule=" + this.curationRule
                + ", curationRuleText=" + this.curationRuleText
                + ", seeAlso=" + this.seeAlso
                + ", issueTracker=" + this.issueTracker
                + ", other=" + this.other
                + ", comment=" + this.comment
                + ", extensionDefinitions=" + this.extensionDefinitions
                + ", extensions=" + this.extensions + ")";
        }
    }

    public static MappingSet.MappingSetBuilder builder() {
        return new MappingSet.MappingSetBuilder();
    }

    public MappingSet.MappingSetBuilder toBuilder() {
        return new MappingSet.MappingSetBuilder()
            .sssomVersion(this.sssomVersion)
            .curieMap(this.curieMap)
            .mappings(this.mappings)
            .mappingSetId(this.mappingSetId)
            .mappingSetVersion(this.mappingSetVersion)
            .mappingSetSource(this.mappingSetSource)
            .mappingSetTitle(this.mappingSetTitle)
            .mappingSetDescription(this.mappingSetDescription)
            .mappingSetConfidence(this.mappingSetConfidence)
            .creatorId(this.creatorId)
            .creatorLabel(this.creatorLabel)
            .license(this.license)
            .subjectType(this.subjectType)
            .subjectSource(this.subjectSource)
            .subjectSourceVersion(this.subjectSourceVersion)
            .objectType(this.objectType)
            .objectSource(this.objectSource)
            .objectSourceVersion(this.objectSourceVersion)
            .predicateType(this.predicateType)
            .mappingProvider(this.mappingProvider)
            .cardinalityScope(this.cardinalityScope)
            .mappingTool(this.mappingTool)
            .mappingToolId(this.mappingToolId)
            .mappingToolVersion(this.mappingToolVersion)
            .mappingDate(this.mappingDate)
            .publicationDate(this.publicationDate)
            .subjectMatchField(this.subjectMatchField)
            .objectMatchField(this.objectMatchField)
            .subjectPreprocessing(this.subjectPreprocessing)
            .objectPreprocessing(this.objectPreprocessing)
            .similarityMeasure(this.similarityMeasure)
            .curationRule(this.curationRule)
            .curationRuleText(this.curationRuleText)
            .seeAlso(this.seeAlso)
            .issueTracker(this.issueTracker)
            .other(this.other)
            .comment(this.comment)
            .extensionDefinitions(this.extensionDefinitions)
            .extensions(this.extensions);
    }
}
