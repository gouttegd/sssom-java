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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a <code>mapping</code> object.
 * <p>
 * Automatically generated from the SSSOM LinkML schema.
 */
public class Mapping  {
    @JsonProperty("record_id")
    @EntityReference
    @Versionable(addedIn = Version.SSSOM_1_1)
    private String recordId;

    @JsonProperty("subject_id")
    @EntityReference
    @SlotURI("http://www.w3.org/2002/07/owl#annotatedSource")
    private String subjectId;

    @JsonProperty("subject_label")
    private String subjectLabel;

    @JsonProperty("subject_category")
    private String subjectCategory;

    @JsonProperty("predicate_id")
    @EntityReference
    @SlotURI("http://www.w3.org/2002/07/owl#annotatedProperty")
    private String predicateId;

    @JsonProperty("predicate_label")
    private String predicateLabel;

    @JsonProperty("predicate_modifier")
    private PredicateModifier predicateModifier;

    @JsonProperty("object_id")
    @EntityReference
    @SlotURI("http://www.w3.org/2002/07/owl#annotatedTarget")
    private String objectId;

    @JsonProperty("object_label")
    private String objectLabel;

    @JsonProperty("object_category")
    private String objectCategory;

    @JsonProperty("mapping_justification")
    @EntityReference
    private String mappingJustification;

    @JsonProperty("author_id")
    @EntityReference
    @SlotURI("http://purl.org/pav/authoredBy")
    private List<String> authorId;

    @JsonProperty("author_label")
    private List<String> authorLabel;

    @JsonProperty("reviewer_id")
    @EntityReference
    private List<String> reviewerId;

    @JsonProperty("reviewer_label")
    private List<String> reviewerLabel;

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
    private EntityType subjectType;

    @JsonProperty("subject_source")
    @EntityReference
    private String subjectSource;

    @JsonProperty("subject_source_version")
    private String subjectSourceVersion;

    @JsonProperty("object_type")
    private EntityType objectType;

    @JsonProperty("object_source")
    @EntityReference
    private String objectSource;

    @JsonProperty("object_source_version")
    private String objectSourceVersion;

    @JsonProperty("predicate_type")
    @Versionable(addedIn = Version.SSSOM_1_1)
    private EntityType predicateType;

    @JsonProperty("mapping_provider")
    @URI
    private String mappingProvider;

    @JsonProperty("mapping_source")
    @EntityReference
    private String mappingSource;

    @JsonProperty("mapping_cardinality")
    private MappingCardinality mappingCardinality;

    @JsonProperty("cardinality_scope")
    @Versionable(addedIn = Version.SSSOM_1_1)
    private List<String> cardinalityScope;

    @JsonProperty("mapping_tool")
    private String mappingTool;

    @JsonProperty("mapping_tool_id")
    @EntityReference
    @Versionable(addedIn = Version.SSSOM_1_1)
    private String mappingToolId;

    @JsonProperty("mapping_tool_version")
    private String mappingToolVersion;

    @JsonProperty("mapping_date")
    @SlotURI("http://purl.org/dc/terms/created")
    private LocalDate mappingDate;

    @JsonProperty("publication_date")
    @SlotURI("http://purl.org/dc/terms/issued")
    private LocalDate publicationDate;

    @JsonProperty("review_date")
    @Versionable(addedIn = Version.SSSOM_1_1)
    private LocalDate reviewDate;

    private Double confidence;

    @JsonProperty("reviewer_confidence")
    @Versionable(addedIn = Version.SSSOM_1_1)
    private Double reviewerConfidence;

    @JsonProperty("curation_rule")
    @EntityReference
    private List<String> curationRule;

    @JsonProperty("curation_rule_text")
    private List<String> curationRuleText;

    @JsonProperty("subject_match_field")
    @EntityReference
    private List<String> subjectMatchField;

    @JsonProperty("object_match_field")
    @EntityReference
    private List<String> objectMatchField;

    @JsonProperty("match_string")
    private List<String> matchString;

    @JsonProperty("subject_preprocessing")
    @EntityReference
    private List<String> subjectPreprocessing;

    @JsonProperty("object_preprocessing")
    @EntityReference
    private List<String> objectPreprocessing;

    @JsonProperty("similarity_score")
    private Double similarityScore;

    @JsonProperty("similarity_measure")
    private String similarityMeasure;

    @JsonProperty("see_also")
    @SlotURI("http://www.w3.org/2000/01/rdf-schema#seeAlso")
    @URI
    private List<String> seeAlso;

    @JsonProperty("issue_tracker_item")
    @EntityReference
    private String issueTrackerItem;

    private String other;

    @SlotURI("http://www.w3.org/2000/01/rdf-schema#comment")
    private String comment;

    private Map<String,ExtensionValue> extensions;

    /**
     * Creates a new empty instance.
     */
    public Mapping() {
    }

    /**
     * Creates a new instance from the specified values.
     */
    protected Mapping(final String recordId,
            final String subjectId,
            final String subjectLabel,
            final String subjectCategory,
            final String predicateId,
            final String predicateLabel,
            final PredicateModifier predicateModifier,
            final String objectId,
            final String objectLabel,
            final String objectCategory,
            final String mappingJustification,
            final List<String> authorId,
            final List<String> authorLabel,
            final List<String> reviewerId,
            final List<String> reviewerLabel,
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
            final String mappingSource,
            final MappingCardinality mappingCardinality,
            final List<String> cardinalityScope,
            final String mappingTool,
            final String mappingToolId,
            final String mappingToolVersion,
            final LocalDate mappingDate,
            final LocalDate publicationDate,
            final LocalDate reviewDate,
            final Double confidence,
            final Double reviewerConfidence,
            final List<String> curationRule,
            final List<String> curationRuleText,
            final List<String> subjectMatchField,
            final List<String> objectMatchField,
            final List<String> matchString,
            final List<String> subjectPreprocessing,
            final List<String> objectPreprocessing,
            final Double similarityScore,
            final String similarityMeasure,
            final List<String> seeAlso,
            final String issueTrackerItem,
            final String other,
            final String comment,
            final Map<String,ExtensionValue> extensions) {
        this.recordId = recordId;
        this.subjectId = subjectId;
        this.subjectLabel = subjectLabel;
        this.subjectCategory = subjectCategory;
        this.predicateId = predicateId;
        this.predicateLabel = predicateLabel;
        this.predicateModifier = predicateModifier;
        this.objectId = objectId;
        this.objectLabel = objectLabel;
        this.objectCategory = objectCategory;
        this.mappingJustification = mappingJustification;
        this.authorId = authorId;
        this.authorLabel = authorLabel;
        this.reviewerId = reviewerId;
        this.reviewerLabel = reviewerLabel;
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
        this.mappingSource = mappingSource;
        this.mappingCardinality = mappingCardinality;
        this.cardinalityScope = cardinalityScope;
        this.mappingTool = mappingTool;
        this.mappingToolId = mappingToolId;
        this.mappingToolVersion = mappingToolVersion;
        this.mappingDate = mappingDate;
        this.publicationDate = publicationDate;
        this.reviewDate = reviewDate;
        this.confidence = confidence;
        this.reviewerConfidence = reviewerConfidence;
        this.curationRule = curationRule;
        this.curationRuleText = curationRuleText;
        this.subjectMatchField = subjectMatchField;
        this.objectMatchField = objectMatchField;
        this.matchString = matchString;
        this.subjectPreprocessing = subjectPreprocessing;
        this.objectPreprocessing = objectPreprocessing;
        this.similarityScore = similarityScore;
        this.similarityMeasure = similarityMeasure;
        this.seeAlso = seeAlso;
        this.issueTrackerItem = issueTrackerItem;
        this.other = other;
        this.comment = comment;
        this.extensions = extensions;
    }

    /**
     * Gets the value of the <code>record_id</code> slot.
     */
    public String getRecordId() {
        return this.recordId;
    }

    /**
     * Sets the value of the <code>record_id</code> slot.
     */
    public void setRecordId(final String value) {
        this.recordId = value;
    }

    /**
     * Gets the value of the <code>subject_id</code> slot.
     */
    public String getSubjectId() {
        return this.subjectId;
    }

    /**
     * Sets the value of the <code>subject_id</code> slot.
     */
    public void setSubjectId(final String value) {
        this.subjectId = value;
    }

    /**
     * Gets the value of the <code>subject_label</code> slot.
     */
    public String getSubjectLabel() {
        return this.subjectLabel;
    }

    /**
     * Sets the value of the <code>subject_label</code> slot.
     */
    public void setSubjectLabel(final String value) {
        this.subjectLabel = value;
    }

    /**
     * Gets the value of the <code>subject_category</code> slot.
     */
    public String getSubjectCategory() {
        return this.subjectCategory;
    }

    /**
     * Sets the value of the <code>subject_category</code> slot.
     */
    public void setSubjectCategory(final String value) {
        this.subjectCategory = value;
    }

    /**
     * Gets the value of the <code>predicate_id</code> slot.
     */
    public String getPredicateId() {
        return this.predicateId;
    }

    /**
     * Sets the value of the <code>predicate_id</code> slot.
     */
    public void setPredicateId(final String value) {
        this.predicateId = value;
    }

    /**
     * Gets the value of the <code>predicate_label</code> slot.
     */
    public String getPredicateLabel() {
        return this.predicateLabel;
    }

    /**
     * Sets the value of the <code>predicate_label</code> slot.
     */
    public void setPredicateLabel(final String value) {
        this.predicateLabel = value;
    }

    /**
     * Gets the value of the <code>predicate_modifier</code> slot.
     */
    public PredicateModifier getPredicateModifier() {
        return this.predicateModifier;
    }

    /**
     * Sets the value of the <code>predicate_modifier</code> slot.
     */
    public void setPredicateModifier(final PredicateModifier value) {
        this.predicateModifier = value;
    }

    /**
     * Gets the value of the <code>object_id</code> slot.
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * Sets the value of the <code>object_id</code> slot.
     */
    public void setObjectId(final String value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the <code>object_label</code> slot.
     */
    public String getObjectLabel() {
        return this.objectLabel;
    }

    /**
     * Sets the value of the <code>object_label</code> slot.
     */
    public void setObjectLabel(final String value) {
        this.objectLabel = value;
    }

    /**
     * Gets the value of the <code>object_category</code> slot.
     */
    public String getObjectCategory() {
        return this.objectCategory;
    }

    /**
     * Sets the value of the <code>object_category</code> slot.
     */
    public void setObjectCategory(final String value) {
        this.objectCategory = value;
    }

    /**
     * Gets the value of the <code>mapping_justification</code> slot.
     */
    public String getMappingJustification() {
        return this.mappingJustification;
    }

    /**
     * Sets the value of the <code>mapping_justification</code> slot.
     */
    public void setMappingJustification(final String value) {
        this.mappingJustification = value;
    }

    /**
     * Gets the value of the <code>author_id</code> slot.
     */
    public List<String> getAuthorId() {
        return this.authorId;
    }

    /**
     * Gets the list of <code>author_id</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of author_id values.
     */
    public List<String> getAuthorId(boolean set) {
        if ( authorId == null && set ) {
            authorId = new ArrayList<>();
        }
        return authorId;
    }

    /**
     * Sets the value of the <code>author_id</code> slot.
     */
    public void setAuthorId(final List<String> value) {
        this.authorId = value;
    }

    /**
     * Gets the value of the <code>author_label</code> slot.
     */
    public List<String> getAuthorLabel() {
        return this.authorLabel;
    }

    /**
     * Gets the list of <code>author_label</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of author_label values.
     */
    public List<String> getAuthorLabel(boolean set) {
        if ( authorLabel == null && set ) {
            authorLabel = new ArrayList<>();
        }
        return authorLabel;
    }

    /**
     * Sets the value of the <code>author_label</code> slot.
     */
    public void setAuthorLabel(final List<String> value) {
        this.authorLabel = value;
    }

    /**
     * Gets the value of the <code>reviewer_id</code> slot.
     */
    public List<String> getReviewerId() {
        return this.reviewerId;
    }

    /**
     * Gets the list of <code>reviewer_id</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of reviewer_id values.
     */
    public List<String> getReviewerId(boolean set) {
        if ( reviewerId == null && set ) {
            reviewerId = new ArrayList<>();
        }
        return reviewerId;
    }

    /**
     * Sets the value of the <code>reviewer_id</code> slot.
     */
    public void setReviewerId(final List<String> value) {
        this.reviewerId = value;
    }

    /**
     * Gets the value of the <code>reviewer_label</code> slot.
     */
    public List<String> getReviewerLabel() {
        return this.reviewerLabel;
    }

    /**
     * Gets the list of <code>reviewer_label</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of reviewer_label values.
     */
    public List<String> getReviewerLabel(boolean set) {
        if ( reviewerLabel == null && set ) {
            reviewerLabel = new ArrayList<>();
        }
        return reviewerLabel;
    }

    /**
     * Sets the value of the <code>reviewer_label</code> slot.
     */
    public void setReviewerLabel(final List<String> value) {
        this.reviewerLabel = value;
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
     * Gets the value of the <code>mapping_source</code> slot.
     */
    public String getMappingSource() {
        return this.mappingSource;
    }

    /**
     * Sets the value of the <code>mapping_source</code> slot.
     */
    public void setMappingSource(final String value) {
        this.mappingSource = value;
    }

    /**
     * Gets the value of the <code>mapping_cardinality</code> slot.
     */
    public MappingCardinality getMappingCardinality() {
        return this.mappingCardinality;
    }

    /**
     * Sets the value of the <code>mapping_cardinality</code> slot.
     */
    public void setMappingCardinality(final MappingCardinality value) {
        this.mappingCardinality = value;
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
     * Gets the value of the <code>review_date</code> slot.
     */
    public LocalDate getReviewDate() {
        return this.reviewDate;
    }

    /**
     * Sets the value of the <code>review_date</code> slot.
     */
    public void setReviewDate(final LocalDate value) {
        this.reviewDate = value;
    }

    /**
     * Gets the value of the <code>confidence</code> slot.
     */
    public Double getConfidence() {
        return this.confidence;
    }

    /**
     * Sets the value of the <code>confidence</code> slot.
     */
    public void setConfidence(final Double value) {
        if ( value != null && value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for confidence");
        }
        if ( value != null && value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for confidence");
        }
        this.confidence = value;
    }

    /**
     * Gets the value of the <code>reviewer_confidence</code> slot.
     */
    public Double getReviewerConfidence() {
        return this.reviewerConfidence;
    }

    /**
     * Sets the value of the <code>reviewer_confidence</code> slot.
     */
    public void setReviewerConfidence(final Double value) {
        if ( value != null && value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for reviewer_confidence");
        }
        if ( value != null && value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for reviewer_confidence");
        }
        this.reviewerConfidence = value;
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
     * Gets the value of the <code>match_string</code> slot.
     */
    public List<String> getMatchString() {
        return this.matchString;
    }

    /**
     * Gets the list of <code>match_string</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of match_string values.
     */
    public List<String> getMatchString(boolean set) {
        if ( matchString == null && set ) {
            matchString = new ArrayList<>();
        }
        return matchString;
    }

    /**
     * Sets the value of the <code>match_string</code> slot.
     */
    public void setMatchString(final List<String> value) {
        this.matchString = value;
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
     * Gets the value of the <code>similarity_score</code> slot.
     */
    public Double getSimilarityScore() {
        return this.similarityScore;
    }

    /**
     * Sets the value of the <code>similarity_score</code> slot.
     */
    public void setSimilarityScore(final Double value) {
        if ( value != null && value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for similarity_score");
        }
        if ( value != null && value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for similarity_score");
        }
        this.similarityScore = value;
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
     * Gets the value of the <code>issue_tracker_item</code> slot.
     */
    public String getIssueTrackerItem() {
        return this.issueTrackerItem;
    }

    /**
     * Sets the value of the <code>issue_tracker_item</code> slot.
     */
    public void setIssueTrackerItem(final String value) {
        this.issueTrackerItem = value;
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

    /**
     * Indicates whether this mapping represents a "missing" mapping.
     * <p>
     * A missing mapping is a mapping where the <em>subject_id</em>  or the
     * <em>object_id</em> (or both) is the special value
     * <code>sssom:NoTermFound</code>, and indicates that an entity in one
     * domain could not be mapped to any entity in another domain.
     *
     * @return {@code True} if the mapping is a missing mapping,
     *         {@code false} otherwise.
     */
    public boolean isUnmapped() {
        return Constants.NoTermFound.equals(subjectId) || Constants.NoTermFound.equals(objectId);
    }

    /**
     * Indicates whether the object of this mapping is not mapped to any
     * entity on the subject side.
     *
     * @return {@code True} if the subject of this mapping is
     *         <code>sssom:NoTermFound</code>, {@code false} otherwise.
     */
    public boolean hasUnmappedSubject() {
        return Constants.NoTermFound.equals(subjectId);
    }

    /**
     * Indicates whether the subject of this mapping is not mapped to any
     * entity on the object side.
     *
     * @return {@code True} if the object of this mapping is
     *         <code>sssom:NoTermFound</code>, {@code false} otherwise.
     */
    public boolean hasUnmappedObject() {
        return Constants.NoTermFound.equals(objectId);
    }

    /**
     * Indicates whether this mapping represents a "literal" mapping.
     * <p>
     * A literal mapping is a mapping where either the subject or the object
     * (or both) is a literal, as indicated by the {@code subject_type} or
     * {@code object_type} slot being set to {@link EntityType#RDFS_LITERAL}.
     *
     * @return {@code True} if the mapping is a literal mapping,
     *         {@code false} otherwise.
     */
    public boolean isLiteral() {
        return subjectType == EntityType.RDFS_LITERAL || objectType == EntityType.RDFS_LITERAL;
    }

    /**
     * Creates an inverted version of this mapping with an explicit predicate.
     *
     * @param predicate The predicate to use for the new mapping.
     * @return A new mapping that is the inverse of this one, or {@code null}
     *         if the specified predicate is itself {@code null}.
     */
    public Mapping invert(String predicate) {
        if ( predicate == null ) {
            return null;
        }

        Mapping inverted = toBuilder()
                .predicateId(predicate)
                .subjectId(objectId)
                .objectId(subjectId)
                .subjectLabel(objectLabel)
                .objectLabel(subjectLabel)
                .subjectCategory(objectCategory)
                .objectCategory(subjectCategory)
                .subjectType(objectType)
                .objectType(subjectType)
                .subjectSource(objectSource)
                .objectSource(subjectSource)
                .subjectSourceVersion(objectSourceVersion)
                .objectSourceVersion(subjectSourceVersion)
                .subjectMatchField(objectMatchField)
                .objectMatchField(subjectMatchField)
                .subjectPreprocessing(objectPreprocessing)
                .objectPreprocessing(subjectPreprocessing).build();

        if ( mappingCardinality != null ) {
            inverted.mappingCardinality = mappingCardinality.getInverse();
        }

        return inverted;
    }

    /**
     * Creates an inverted version of this mapping if possible.
     * <p>
     * Inversion is possible if the predicate is one of the known "common"
     * predicates and is invertible. To invert a mapping with an arbitrary
     * predicate, use {@link #invert(String)}.
     *
     * @return A new mapping that is the inverse of this one, or {@code null}
     *         if inversion is not possible.
     */
    public Mapping invert() {
        CommonPredicate predicate = CommonPredicate.fromString(predicateId);
        if ( predicate == null || !predicate.isInvertible() ) {
            return null;
        }

        return invert(predicate.getInverse());
    }

    /**
     * Creates a canonical S-expression representing this mapping.
     *
     * @return A String uniquely representing this mapping, as a canonical S-expression.
     */
    public String toSExpr() {
        DecimalFormat floatFormatter = new DecimalFormat("#.###");
        floatFormatter.setRoundingMode(RoundingMode.HALF_UP);

        StringBuilder sb = new StringBuilder();
        sb.append("(7:mapping(");
        if ( subjectId != null ) {
            String v = String.valueOf(subjectId);
            sb.append(String.format("(10:subject_id%d:%s)", v.length(), v));
        }
        if ( subjectLabel != null ) {
            String v = String.valueOf(subjectLabel);
            sb.append(String.format("(13:subject_label%d:%s)", v.length(), v));
        }
        if ( subjectCategory != null ) {
            String v = String.valueOf(subjectCategory);
            sb.append(String.format("(16:subject_category%d:%s)", v.length(), v));
        }
        if ( predicateId != null ) {
            String v = String.valueOf(predicateId);
            sb.append(String.format("(12:predicate_id%d:%s)", v.length(), v));
        }
        if ( predicateLabel != null ) {
            String v = String.valueOf(predicateLabel);
            sb.append(String.format("(15:predicate_label%d:%s)", v.length(), v));
        }
        if ( predicateModifier != null ) {
            String v = String.valueOf(predicateModifier);
            sb.append(String.format("(18:predicate_modifier%d:%s)", v.length(), v));
        }
        if ( objectId != null ) {
            String v = String.valueOf(objectId);
            sb.append(String.format("(9:object_id%d:%s)", v.length(), v));
        }
        if ( objectLabel != null ) {
            String v = String.valueOf(objectLabel);
            sb.append(String.format("(12:object_label%d:%s)", v.length(), v));
        }
        if ( objectCategory != null ) {
            String v = String.valueOf(objectCategory);
            sb.append(String.format("(15:object_category%d:%s)", v.length(), v));
        }
        if ( mappingJustification != null ) {
            String v = String.valueOf(mappingJustification);
            sb.append(String.format("(21:mapping_justification%d:%s)", v.length(), v));
        }
        if ( authorId != null ) {
            sb.append("(9:author_id(");
            List<String> tmp = null;
            if ( authorId.size() > 1 ) {
                tmp = new ArrayList<>(authorId);
                Collections.sort(tmp);
            } else {
                tmp = authorId;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( authorLabel != null ) {
            sb.append("(12:author_label(");
            List<String> tmp = null;
            if ( authorLabel.size() > 1 ) {
                tmp = new ArrayList<>(authorLabel);
                Collections.sort(tmp);
            } else {
                tmp = authorLabel;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( reviewerId != null ) {
            sb.append("(11:reviewer_id(");
            List<String> tmp = null;
            if ( reviewerId.size() > 1 ) {
                tmp = new ArrayList<>(reviewerId);
                Collections.sort(tmp);
            } else {
                tmp = reviewerId;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( reviewerLabel != null ) {
            sb.append("(14:reviewer_label(");
            List<String> tmp = null;
            if ( reviewerLabel.size() > 1 ) {
                tmp = new ArrayList<>(reviewerLabel);
                Collections.sort(tmp);
            } else {
                tmp = reviewerLabel;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( creatorId != null ) {
            sb.append("(10:creator_id(");
            List<String> tmp = null;
            if ( creatorId.size() > 1 ) {
                tmp = new ArrayList<>(creatorId);
                Collections.sort(tmp);
            } else {
                tmp = creatorId;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( creatorLabel != null ) {
            sb.append("(13:creator_label(");
            List<String> tmp = null;
            if ( creatorLabel.size() > 1 ) {
                tmp = new ArrayList<>(creatorLabel);
                Collections.sort(tmp);
            } else {
                tmp = creatorLabel;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( license != null ) {
            String v = String.valueOf(license);
            sb.append(String.format("(7:license%d:%s)", v.length(), v));
        }
        if ( subjectType != null ) {
            String v = String.valueOf(subjectType);
            sb.append(String.format("(12:subject_type%d:%s)", v.length(), v));
        }
        if ( subjectSource != null ) {
            String v = String.valueOf(subjectSource);
            sb.append(String.format("(14:subject_source%d:%s)", v.length(), v));
        }
        if ( subjectSourceVersion != null ) {
            String v = String.valueOf(subjectSourceVersion);
            sb.append(String.format("(22:subject_source_version%d:%s)", v.length(), v));
        }
        if ( objectType != null ) {
            String v = String.valueOf(objectType);
            sb.append(String.format("(11:object_type%d:%s)", v.length(), v));
        }
        if ( objectSource != null ) {
            String v = String.valueOf(objectSource);
            sb.append(String.format("(13:object_source%d:%s)", v.length(), v));
        }
        if ( objectSourceVersion != null ) {
            String v = String.valueOf(objectSourceVersion);
            sb.append(String.format("(21:object_source_version%d:%s)", v.length(), v));
        }
        if ( predicateType != null ) {
            String v = String.valueOf(predicateType);
            sb.append(String.format("(14:predicate_type%d:%s)", v.length(), v));
        }
        if ( mappingProvider != null ) {
            String v = String.valueOf(mappingProvider);
            sb.append(String.format("(16:mapping_provider%d:%s)", v.length(), v));
        }
        if ( mappingSource != null ) {
            String v = String.valueOf(mappingSource);
            sb.append(String.format("(14:mapping_source%d:%s)", v.length(), v));
        }
        if ( cardinalityScope != null ) {
            sb.append("(17:cardinality_scope(");
            List<String> tmp = null;
            if ( cardinalityScope.size() > 1 ) {
                tmp = new ArrayList<>(cardinalityScope);
                Collections.sort(tmp);
            } else {
                tmp = cardinalityScope;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( mappingTool != null ) {
            String v = String.valueOf(mappingTool);
            sb.append(String.format("(12:mapping_tool%d:%s)", v.length(), v));
        }
        if ( mappingToolId != null ) {
            String v = String.valueOf(mappingToolId);
            sb.append(String.format("(15:mapping_tool_id%d:%s)", v.length(), v));
        }
        if ( mappingToolVersion != null ) {
            String v = String.valueOf(mappingToolVersion);
            sb.append(String.format("(20:mapping_tool_version%d:%s)", v.length(), v));
        }
        if ( mappingDate != null ) {
            String v = String.valueOf(mappingDate);
            sb.append(String.format("(12:mapping_date%d:%s)", v.length(), v));
        }
        if ( publicationDate != null ) {
            String v = String.valueOf(publicationDate);
            sb.append(String.format("(16:publication_date%d:%s)", v.length(), v));
        }
        if ( reviewDate != null ) {
            String v = String.valueOf(reviewDate);
            sb.append(String.format("(11:review_date%d:%s)", v.length(), v));
        }
        if ( confidence != null ) {
            String v = floatFormatter.format(confidence);
            sb.append(String.format("(10:confidence%d:%s)", v.length(), v));
        }
        if ( reviewerConfidence != null ) {
            String v = floatFormatter.format(reviewerConfidence);
            sb.append(String.format("(19:reviewer_confidence%d:%s)", v.length(), v));
        }
        if ( curationRule != null ) {
            sb.append("(13:curation_rule(");
            List<String> tmp = null;
            if ( curationRule.size() > 1 ) {
                tmp = new ArrayList<>(curationRule);
                Collections.sort(tmp);
            } else {
                tmp = curationRule;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( curationRuleText != null ) {
            sb.append("(18:curation_rule_text(");
            List<String> tmp = null;
            if ( curationRuleText.size() > 1 ) {
                tmp = new ArrayList<>(curationRuleText);
                Collections.sort(tmp);
            } else {
                tmp = curationRuleText;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( subjectMatchField != null ) {
            sb.append("(19:subject_match_field(");
            List<String> tmp = null;
            if ( subjectMatchField.size() > 1 ) {
                tmp = new ArrayList<>(subjectMatchField);
                Collections.sort(tmp);
            } else {
                tmp = subjectMatchField;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( objectMatchField != null ) {
            sb.append("(18:object_match_field(");
            List<String> tmp = null;
            if ( objectMatchField.size() > 1 ) {
                tmp = new ArrayList<>(objectMatchField);
                Collections.sort(tmp);
            } else {
                tmp = objectMatchField;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( matchString != null ) {
            sb.append("(12:match_string(");
            List<String> tmp = null;
            if ( matchString.size() > 1 ) {
                tmp = new ArrayList<>(matchString);
                Collections.sort(tmp);
            } else {
                tmp = matchString;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( subjectPreprocessing != null ) {
            sb.append("(21:subject_preprocessing(");
            List<String> tmp = null;
            if ( subjectPreprocessing.size() > 1 ) {
                tmp = new ArrayList<>(subjectPreprocessing);
                Collections.sort(tmp);
            } else {
                tmp = subjectPreprocessing;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( objectPreprocessing != null ) {
            sb.append("(20:object_preprocessing(");
            List<String> tmp = null;
            if ( objectPreprocessing.size() > 1 ) {
                tmp = new ArrayList<>(objectPreprocessing);
                Collections.sort(tmp);
            } else {
                tmp = objectPreprocessing;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( similarityScore != null ) {
            String v = floatFormatter.format(similarityScore);
            sb.append(String.format("(16:similarity_score%d:%s)", v.length(), v));
        }
        if ( similarityMeasure != null ) {
            String v = String.valueOf(similarityMeasure);
            sb.append(String.format("(18:similarity_measure%d:%s)", v.length(), v));
        }
        if ( seeAlso != null ) {
            sb.append("(8:see_also(");
            List<String> tmp = null;
            if ( seeAlso.size() > 1 ) {
                tmp = new ArrayList<>(seeAlso);
                Collections.sort(tmp);
            } else {
                tmp = seeAlso;
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( issueTrackerItem != null ) {
            String v = String.valueOf(issueTrackerItem);
            sb.append(String.format("(18:issue_tracker_item%d:%s)", v.length(), v));
        }
        if ( other != null ) {
            String v = String.valueOf(other);
            sb.append(String.format("(5:other%d:%s)", v.length(), v));
        }
        if ( comment != null ) {
            String v = String.valueOf(comment);
            sb.append(String.format("(7:comment%d:%s)", v.length(), v));
        }
        if ( extensions != null ) {
            sb.append("(10:extensions(");
            ArrayList<Map.Entry<String, ExtensionValue>> entries = new ArrayList<>(extensions.entrySet());
            entries.sort((a, b) -> a.getKey().compareTo(b.getKey()));
            for ( Map.Entry<String, ExtensionValue> entry : entries ) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                sb.append(String.format("(%d:%s%d:%s)", key.length(), key, value.length(), value));
            }
            sb.append("))");
        }
        sb.append("))");
        return sb.toString();
    }

    /**
     * @deprecated Use {@code #getSimilarityScore()} instead.
     */
    @Deprecated
    public Double getSemanticSimilarityScore() {
        return getSimilarityScore();
    }

    /**
     * @deprecated Use {@code #setSimilarityScore(Double)} instead.
     */
    @Deprecated
    public void setSemanticSimilarityScore(Double value) {
        setSimilarityScore(value);
    }

    /**
     * @deprecated Use {@code #getSimilarityMeasure()} instead.
     */
    @Deprecated
    public String getSemanticSimilarityMeasure() {
        return getSimilarityMeasure();
    }

    /**
     * @deprecated Use {@code #setSimilarityMeasure(String)} instead.
     */
    @Deprecated
    public void setSemanticSimilarityMeasure(String value) {
        setSimilarityMeasure(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mapping(");
        if ( this.recordId != null ) {
            sb.append("record_id=");
            sb.append(this.recordId);
            sb.append(",");
        }
        if ( this.subjectId != null ) {
            sb.append("subject_id=");
            sb.append(this.subjectId);
            sb.append(",");
        }
        if ( this.subjectLabel != null ) {
            sb.append("subject_label=");
            sb.append(this.subjectLabel);
            sb.append(",");
        }
        if ( this.subjectCategory != null ) {
            sb.append("subject_category=");
            sb.append(this.subjectCategory);
            sb.append(",");
        }
        if ( this.predicateId != null ) {
            sb.append("predicate_id=");
            sb.append(this.predicateId);
            sb.append(",");
        }
        if ( this.predicateLabel != null ) {
            sb.append("predicate_label=");
            sb.append(this.predicateLabel);
            sb.append(",");
        }
        if ( this.predicateModifier != null ) {
            sb.append("predicate_modifier=");
            sb.append(this.predicateModifier);
            sb.append(",");
        }
        if ( this.objectId != null ) {
            sb.append("object_id=");
            sb.append(this.objectId);
            sb.append(",");
        }
        if ( this.objectLabel != null ) {
            sb.append("object_label=");
            sb.append(this.objectLabel);
            sb.append(",");
        }
        if ( this.objectCategory != null ) {
            sb.append("object_category=");
            sb.append(this.objectCategory);
            sb.append(",");
        }
        if ( this.mappingJustification != null ) {
            sb.append("mapping_justification=");
            sb.append(this.mappingJustification);
            sb.append(",");
        }
        if ( this.authorId != null ) {
            sb.append("author_id=");
            sb.append(this.authorId);
            sb.append(",");
        }
        if ( this.authorLabel != null ) {
            sb.append("author_label=");
            sb.append(this.authorLabel);
            sb.append(",");
        }
        if ( this.reviewerId != null ) {
            sb.append("reviewer_id=");
            sb.append(this.reviewerId);
            sb.append(",");
        }
        if ( this.reviewerLabel != null ) {
            sb.append("reviewer_label=");
            sb.append(this.reviewerLabel);
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
        if ( this.mappingSource != null ) {
            sb.append("mapping_source=");
            sb.append(this.mappingSource);
            sb.append(",");
        }
        if ( this.mappingCardinality != null ) {
            sb.append("mapping_cardinality=");
            sb.append(this.mappingCardinality);
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
        if ( this.reviewDate != null ) {
            sb.append("review_date=");
            sb.append(this.reviewDate);
            sb.append(",");
        }
        if ( this.confidence != null ) {
            sb.append("confidence=");
            sb.append(this.confidence);
            sb.append(",");
        }
        if ( this.reviewerConfidence != null ) {
            sb.append("reviewer_confidence=");
            sb.append(this.reviewerConfidence);
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
        if ( this.matchString != null ) {
            sb.append("match_string=");
            sb.append(this.matchString);
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
        if ( this.similarityScore != null ) {
            sb.append("similarity_score=");
            sb.append(this.similarityScore);
            sb.append(",");
        }
        if ( this.similarityMeasure != null ) {
            sb.append("similarity_measure=");
            sb.append(this.similarityMeasure);
            sb.append(",");
        }
        if ( this.seeAlso != null ) {
            sb.append("see_also=");
            sb.append(this.seeAlso);
            sb.append(",");
        }
        if ( this.issueTrackerItem != null ) {
            sb.append("issue_tracker_item=");
            sb.append(this.issueTrackerItem);
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
        if ( !(o instanceof Mapping) ) return false;
        final Mapping other = (Mapping) o;
        if ( !other.canEqual((Object) this)) return false;
        if ( this.recordId == null ? other.recordId != null : !this.recordId.equals(other.recordId)) return false;
        if ( this.subjectId == null ? other.subjectId != null : !this.subjectId.equals(other.subjectId)) return false;
        if ( this.subjectLabel == null ? other.subjectLabel != null : !this.subjectLabel.equals(other.subjectLabel)) return false;
        if ( this.subjectCategory == null ? other.subjectCategory != null : !this.subjectCategory.equals(other.subjectCategory)) return false;
        if ( this.predicateId == null ? other.predicateId != null : !this.predicateId.equals(other.predicateId)) return false;
        if ( this.predicateLabel == null ? other.predicateLabel != null : !this.predicateLabel.equals(other.predicateLabel)) return false;
        if ( this.predicateModifier == null ? other.predicateModifier != null : !this.predicateModifier.equals(other.predicateModifier)) return false;
        if ( this.objectId == null ? other.objectId != null : !this.objectId.equals(other.objectId)) return false;
        if ( this.objectLabel == null ? other.objectLabel != null : !this.objectLabel.equals(other.objectLabel)) return false;
        if ( this.objectCategory == null ? other.objectCategory != null : !this.objectCategory.equals(other.objectCategory)) return false;
        if ( this.mappingJustification == null ? other.mappingJustification != null : !this.mappingJustification.equals(other.mappingJustification)) return false;
        if ( this.authorId == null ? other.authorId != null : !this.authorId.equals(other.authorId)) return false;
        if ( this.authorLabel == null ? other.authorLabel != null : !this.authorLabel.equals(other.authorLabel)) return false;
        if ( this.reviewerId == null ? other.reviewerId != null : !this.reviewerId.equals(other.reviewerId)) return false;
        if ( this.reviewerLabel == null ? other.reviewerLabel != null : !this.reviewerLabel.equals(other.reviewerLabel)) return false;
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
        if ( this.mappingSource == null ? other.mappingSource != null : !this.mappingSource.equals(other.mappingSource)) return false;
        if ( this.mappingCardinality == null ? other.mappingCardinality != null : !this.mappingCardinality.equals(other.mappingCardinality)) return false;
        if ( this.cardinalityScope == null ? other.cardinalityScope != null : !this.cardinalityScope.equals(other.cardinalityScope)) return false;
        if ( this.mappingTool == null ? other.mappingTool != null : !this.mappingTool.equals(other.mappingTool)) return false;
        if ( this.mappingToolId == null ? other.mappingToolId != null : !this.mappingToolId.equals(other.mappingToolId)) return false;
        if ( this.mappingToolVersion == null ? other.mappingToolVersion != null : !this.mappingToolVersion.equals(other.mappingToolVersion)) return false;
        if ( this.mappingDate == null ? other.mappingDate != null : !this.mappingDate.equals(other.mappingDate)) return false;
        if ( this.publicationDate == null ? other.publicationDate != null : !this.publicationDate.equals(other.publicationDate)) return false;
        if ( this.reviewDate == null ? other.reviewDate != null : !this.reviewDate.equals(other.reviewDate)) return false;
        if ( this.confidence == null ? other.confidence != null : !this.confidence.equals(other.confidence)) return false;
        if ( this.reviewerConfidence == null ? other.reviewerConfidence != null : !this.reviewerConfidence.equals(other.reviewerConfidence)) return false;
        if ( this.curationRule == null ? other.curationRule != null : !this.curationRule.equals(other.curationRule)) return false;
        if ( this.curationRuleText == null ? other.curationRuleText != null : !this.curationRuleText.equals(other.curationRuleText)) return false;
        if ( this.subjectMatchField == null ? other.subjectMatchField != null : !this.subjectMatchField.equals(other.subjectMatchField)) return false;
        if ( this.objectMatchField == null ? other.objectMatchField != null : !this.objectMatchField.equals(other.objectMatchField)) return false;
        if ( this.matchString == null ? other.matchString != null : !this.matchString.equals(other.matchString)) return false;
        if ( this.subjectPreprocessing == null ? other.subjectPreprocessing != null : !this.subjectPreprocessing.equals(other.subjectPreprocessing)) return false;
        if ( this.objectPreprocessing == null ? other.objectPreprocessing != null : !this.objectPreprocessing.equals(other.objectPreprocessing)) return false;
        if ( this.similarityScore == null ? other.similarityScore != null : !this.similarityScore.equals(other.similarityScore)) return false;
        if ( this.similarityMeasure == null ? other.similarityMeasure != null : !this.similarityMeasure.equals(other.similarityMeasure)) return false;
        if ( this.seeAlso == null ? other.seeAlso != null : !this.seeAlso.equals(other.seeAlso)) return false;
        if ( this.issueTrackerItem == null ? other.issueTrackerItem != null : !this.issueTrackerItem.equals(other.issueTrackerItem)) return false;
        if ( this.other == null ? other.other != null : !this.other.equals(other.other)) return false;
        if ( this.comment == null ? other.comment != null : !this.comment.equals(other.comment)) return false;
        if ( this.extensions == null ? other.extensions != null : !this.extensions.equals(other.extensions)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Mapping;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.recordId == null ? 43 : this.recordId.hashCode());
        result = result * PRIME + (this.subjectId == null ? 43 : this.subjectId.hashCode());
        result = result * PRIME + (this.subjectLabel == null ? 43 : this.subjectLabel.hashCode());
        result = result * PRIME + (this.subjectCategory == null ? 43 : this.subjectCategory.hashCode());
        result = result * PRIME + (this.predicateId == null ? 43 : this.predicateId.hashCode());
        result = result * PRIME + (this.predicateLabel == null ? 43 : this.predicateLabel.hashCode());
        result = result * PRIME + (this.predicateModifier == null ? 43 : this.predicateModifier.hashCode());
        result = result * PRIME + (this.objectId == null ? 43 : this.objectId.hashCode());
        result = result * PRIME + (this.objectLabel == null ? 43 : this.objectLabel.hashCode());
        result = result * PRIME + (this.objectCategory == null ? 43 : this.objectCategory.hashCode());
        result = result * PRIME + (this.mappingJustification == null ? 43 : this.mappingJustification.hashCode());
        result = result * PRIME + (this.authorId == null ? 43 : this.authorId.hashCode());
        result = result * PRIME + (this.authorLabel == null ? 43 : this.authorLabel.hashCode());
        result = result * PRIME + (this.reviewerId == null ? 43 : this.reviewerId.hashCode());
        result = result * PRIME + (this.reviewerLabel == null ? 43 : this.reviewerLabel.hashCode());
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
        result = result * PRIME + (this.mappingSource == null ? 43 : this.mappingSource.hashCode());
        result = result * PRIME + (this.mappingCardinality == null ? 43 : this.mappingCardinality.hashCode());
        result = result * PRIME + (this.cardinalityScope == null ? 43 : this.cardinalityScope.hashCode());
        result = result * PRIME + (this.mappingTool == null ? 43 : this.mappingTool.hashCode());
        result = result * PRIME + (this.mappingToolId == null ? 43 : this.mappingToolId.hashCode());
        result = result * PRIME + (this.mappingToolVersion == null ? 43 : this.mappingToolVersion.hashCode());
        result = result * PRIME + (this.mappingDate == null ? 43 : this.mappingDate.hashCode());
        result = result * PRIME + (this.publicationDate == null ? 43 : this.publicationDate.hashCode());
        result = result * PRIME + (this.reviewDate == null ? 43 : this.reviewDate.hashCode());
        result = result * PRIME + (this.confidence == null ? 43 : this.confidence.hashCode());
        result = result * PRIME + (this.reviewerConfidence == null ? 43 : this.reviewerConfidence.hashCode());
        result = result * PRIME + (this.curationRule == null ? 43 : this.curationRule.hashCode());
        result = result * PRIME + (this.curationRuleText == null ? 43 : this.curationRuleText.hashCode());
        result = result * PRIME + (this.subjectMatchField == null ? 43 : this.subjectMatchField.hashCode());
        result = result * PRIME + (this.objectMatchField == null ? 43 : this.objectMatchField.hashCode());
        result = result * PRIME + (this.matchString == null ? 43 : this.matchString.hashCode());
        result = result * PRIME + (this.subjectPreprocessing == null ? 43 : this.subjectPreprocessing.hashCode());
        result = result * PRIME + (this.objectPreprocessing == null ? 43 : this.objectPreprocessing.hashCode());
        result = result * PRIME + (this.similarityScore == null ? 43 : this.similarityScore.hashCode());
        result = result * PRIME + (this.similarityMeasure == null ? 43 : this.similarityMeasure.hashCode());
        result = result * PRIME + (this.seeAlso == null ? 43 : this.seeAlso.hashCode());
        result = result * PRIME + (this.issueTrackerItem == null ? 43 : this.issueTrackerItem.hashCode());
        result = result * PRIME + (this.other == null ? 43 : this.other.hashCode());
        result = result * PRIME + (this.comment == null ? 43 : this.comment.hashCode());
        result = result * PRIME + (this.extensions == null ? 43 : this.extensions.hashCode());
        return result;
    }

    public static class MappingBuilder {
        private String recordId;
        private String subjectId;
        private String subjectLabel;
        private String subjectCategory;
        private String predicateId;
        private String predicateLabel;
        private PredicateModifier predicateModifier;
        private String objectId;
        private String objectLabel;
        private String objectCategory;
        private String mappingJustification;
        private List<String> authorId;
        private List<String> authorLabel;
        private List<String> reviewerId;
        private List<String> reviewerLabel;
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
        private String mappingSource;
        private MappingCardinality mappingCardinality;
        private List<String> cardinalityScope;
        private String mappingTool;
        private String mappingToolId;
        private String mappingToolVersion;
        private LocalDate mappingDate;
        private LocalDate publicationDate;
        private LocalDate reviewDate;
        private Double confidence;
        private Double reviewerConfidence;
        private List<String> curationRule;
        private List<String> curationRuleText;
        private List<String> subjectMatchField;
        private List<String> objectMatchField;
        private List<String> matchString;
        private List<String> subjectPreprocessing;
        private List<String> objectPreprocessing;
        private Double similarityScore;
        private String similarityMeasure;
        private List<String> seeAlso;
        private String issueTrackerItem;
        private String other;
        private String comment;
        private Map<String,ExtensionValue> extensions;

        MappingBuilder() {
        }

        public Mapping.MappingBuilder recordId(final String recordId) {
            this.recordId = recordId;
            return this;
        }

        public Mapping.MappingBuilder subjectId(final String subjectId) {
            this.subjectId = subjectId;
            return this;
        }

        public Mapping.MappingBuilder subjectLabel(final String subjectLabel) {
            this.subjectLabel = subjectLabel;
            return this;
        }

        public Mapping.MappingBuilder subjectCategory(final String subjectCategory) {
            this.subjectCategory = subjectCategory;
            return this;
        }

        public Mapping.MappingBuilder predicateId(final String predicateId) {
            this.predicateId = predicateId;
            return this;
        }

        public Mapping.MappingBuilder predicateLabel(final String predicateLabel) {
            this.predicateLabel = predicateLabel;
            return this;
        }

        public Mapping.MappingBuilder predicateModifier(final PredicateModifier predicateModifier) {
            this.predicateModifier = predicateModifier;
            return this;
        }

        public Mapping.MappingBuilder objectId(final String objectId) {
            this.objectId = objectId;
            return this;
        }

        public Mapping.MappingBuilder objectLabel(final String objectLabel) {
            this.objectLabel = objectLabel;
            return this;
        }

        public Mapping.MappingBuilder objectCategory(final String objectCategory) {
            this.objectCategory = objectCategory;
            return this;
        }

        public Mapping.MappingBuilder mappingJustification(final String mappingJustification) {
            this.mappingJustification = mappingJustification;
            return this;
        }

        public Mapping.MappingBuilder authorId(final List<String> authorId) {
            this.authorId = authorId;
            return this;
        }

        public Mapping.MappingBuilder authorLabel(final List<String> authorLabel) {
            this.authorLabel = authorLabel;
            return this;
        }

        public Mapping.MappingBuilder reviewerId(final List<String> reviewerId) {
            this.reviewerId = reviewerId;
            return this;
        }

        public Mapping.MappingBuilder reviewerLabel(final List<String> reviewerLabel) {
            this.reviewerLabel = reviewerLabel;
            return this;
        }

        public Mapping.MappingBuilder creatorId(final List<String> creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public Mapping.MappingBuilder creatorLabel(final List<String> creatorLabel) {
            this.creatorLabel = creatorLabel;
            return this;
        }

        public Mapping.MappingBuilder license(final String license) {
            this.license = license;
            return this;
        }

        public Mapping.MappingBuilder subjectType(final EntityType subjectType) {
            this.subjectType = subjectType;
            return this;
        }

        public Mapping.MappingBuilder subjectSource(final String subjectSource) {
            this.subjectSource = subjectSource;
            return this;
        }

        public Mapping.MappingBuilder subjectSourceVersion(final String subjectSourceVersion) {
            this.subjectSourceVersion = subjectSourceVersion;
            return this;
        }

        public Mapping.MappingBuilder objectType(final EntityType objectType) {
            this.objectType = objectType;
            return this;
        }

        public Mapping.MappingBuilder objectSource(final String objectSource) {
            this.objectSource = objectSource;
            return this;
        }

        public Mapping.MappingBuilder objectSourceVersion(final String objectSourceVersion) {
            this.objectSourceVersion = objectSourceVersion;
            return this;
        }

        public Mapping.MappingBuilder predicateType(final EntityType predicateType) {
            this.predicateType = predicateType;
            return this;
        }

        public Mapping.MappingBuilder mappingProvider(final String mappingProvider) {
            this.mappingProvider = mappingProvider;
            return this;
        }

        public Mapping.MappingBuilder mappingSource(final String mappingSource) {
            this.mappingSource = mappingSource;
            return this;
        }

        public Mapping.MappingBuilder mappingCardinality(final MappingCardinality mappingCardinality) {
            this.mappingCardinality = mappingCardinality;
            return this;
        }

        public Mapping.MappingBuilder cardinalityScope(final List<String> cardinalityScope) {
            this.cardinalityScope = cardinalityScope;
            return this;
        }

        public Mapping.MappingBuilder mappingTool(final String mappingTool) {
            this.mappingTool = mappingTool;
            return this;
        }

        public Mapping.MappingBuilder mappingToolId(final String mappingToolId) {
            this.mappingToolId = mappingToolId;
            return this;
        }

        public Mapping.MappingBuilder mappingToolVersion(final String mappingToolVersion) {
            this.mappingToolVersion = mappingToolVersion;
            return this;
        }

        public Mapping.MappingBuilder mappingDate(final LocalDate mappingDate) {
            this.mappingDate = mappingDate;
            return this;
        }

        public Mapping.MappingBuilder publicationDate(final LocalDate publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public Mapping.MappingBuilder reviewDate(final LocalDate reviewDate) {
            this.reviewDate = reviewDate;
            return this;
        }

        public Mapping.MappingBuilder confidence(final Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Mapping.MappingBuilder reviewerConfidence(final Double reviewerConfidence) {
            this.reviewerConfidence = reviewerConfidence;
            return this;
        }

        public Mapping.MappingBuilder curationRule(final List<String> curationRule) {
            this.curationRule = curationRule;
            return this;
        }

        public Mapping.MappingBuilder curationRuleText(final List<String> curationRuleText) {
            this.curationRuleText = curationRuleText;
            return this;
        }

        public Mapping.MappingBuilder subjectMatchField(final List<String> subjectMatchField) {
            this.subjectMatchField = subjectMatchField;
            return this;
        }

        public Mapping.MappingBuilder objectMatchField(final List<String> objectMatchField) {
            this.objectMatchField = objectMatchField;
            return this;
        }

        public Mapping.MappingBuilder matchString(final List<String> matchString) {
            this.matchString = matchString;
            return this;
        }

        public Mapping.MappingBuilder subjectPreprocessing(final List<String> subjectPreprocessing) {
            this.subjectPreprocessing = subjectPreprocessing;
            return this;
        }

        public Mapping.MappingBuilder objectPreprocessing(final List<String> objectPreprocessing) {
            this.objectPreprocessing = objectPreprocessing;
            return this;
        }

        public Mapping.MappingBuilder similarityScore(final Double similarityScore) {
            this.similarityScore = similarityScore;
            return this;
        }

        public Mapping.MappingBuilder similarityMeasure(final String similarityMeasure) {
            this.similarityMeasure = similarityMeasure;
            return this;
        }

        public Mapping.MappingBuilder seeAlso(final List<String> seeAlso) {
            this.seeAlso = seeAlso;
            return this;
        }

        public Mapping.MappingBuilder issueTrackerItem(final String issueTrackerItem) {
            this.issueTrackerItem = issueTrackerItem;
            return this;
        }

        public Mapping.MappingBuilder other(final String other) {
            this.other = other;
            return this;
        }

        public Mapping.MappingBuilder comment(final String comment) {
            this.comment = comment;
            return this;
        }

        public Mapping.MappingBuilder extensions(final Map<String,ExtensionValue> extensions) {
            this.extensions = extensions;
            return this;
        }

        public Mapping build() {
            return new Mapping(this.recordId,
                this.subjectId,
                this.subjectLabel,
                this.subjectCategory,
                this.predicateId,
                this.predicateLabel,
                this.predicateModifier,
                this.objectId,
                this.objectLabel,
                this.objectCategory,
                this.mappingJustification,
                this.authorId,
                this.authorLabel,
                this.reviewerId,
                this.reviewerLabel,
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
                this.mappingSource,
                this.mappingCardinality,
                this.cardinalityScope,
                this.mappingTool,
                this.mappingToolId,
                this.mappingToolVersion,
                this.mappingDate,
                this.publicationDate,
                this.reviewDate,
                this.confidence,
                this.reviewerConfidence,
                this.curationRule,
                this.curationRuleText,
                this.subjectMatchField,
                this.objectMatchField,
                this.matchString,
                this.subjectPreprocessing,
                this.objectPreprocessing,
                this.similarityScore,
                this.similarityMeasure,
                this.seeAlso,
                this.issueTrackerItem,
                this.other,
                this.comment,
                this.extensions);
        }

        public String toString() {
            return "Mapping.MappingBuilder(recordId=" + this.recordId
                + ", subjectId=" + this.subjectId
                + ", subjectLabel=" + this.subjectLabel
                + ", subjectCategory=" + this.subjectCategory
                + ", predicateId=" + this.predicateId
                + ", predicateLabel=" + this.predicateLabel
                + ", predicateModifier=" + this.predicateModifier
                + ", objectId=" + this.objectId
                + ", objectLabel=" + this.objectLabel
                + ", objectCategory=" + this.objectCategory
                + ", mappingJustification=" + this.mappingJustification
                + ", authorId=" + this.authorId
                + ", authorLabel=" + this.authorLabel
                + ", reviewerId=" + this.reviewerId
                + ", reviewerLabel=" + this.reviewerLabel
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
                + ", mappingSource=" + this.mappingSource
                + ", mappingCardinality=" + this.mappingCardinality
                + ", cardinalityScope=" + this.cardinalityScope
                + ", mappingTool=" + this.mappingTool
                + ", mappingToolId=" + this.mappingToolId
                + ", mappingToolVersion=" + this.mappingToolVersion
                + ", mappingDate=" + this.mappingDate
                + ", publicationDate=" + this.publicationDate
                + ", reviewDate=" + this.reviewDate
                + ", confidence=" + this.confidence
                + ", reviewerConfidence=" + this.reviewerConfidence
                + ", curationRule=" + this.curationRule
                + ", curationRuleText=" + this.curationRuleText
                + ", subjectMatchField=" + this.subjectMatchField
                + ", objectMatchField=" + this.objectMatchField
                + ", matchString=" + this.matchString
                + ", subjectPreprocessing=" + this.subjectPreprocessing
                + ", objectPreprocessing=" + this.objectPreprocessing
                + ", similarityScore=" + this.similarityScore
                + ", similarityMeasure=" + this.similarityMeasure
                + ", seeAlso=" + this.seeAlso
                + ", issueTrackerItem=" + this.issueTrackerItem
                + ", other=" + this.other
                + ", comment=" + this.comment
                + ", extensions=" + this.extensions + ")";
        }
    }

    public static Mapping.MappingBuilder builder() {
        return new Mapping.MappingBuilder();
    }

    public Mapping.MappingBuilder toBuilder() {
        return new Mapping.MappingBuilder()
            .recordId(this.recordId)
            .subjectId(this.subjectId)
            .subjectLabel(this.subjectLabel)
            .subjectCategory(this.subjectCategory)
            .predicateId(this.predicateId)
            .predicateLabel(this.predicateLabel)
            .predicateModifier(this.predicateModifier)
            .objectId(this.objectId)
            .objectLabel(this.objectLabel)
            .objectCategory(this.objectCategory)
            .mappingJustification(this.mappingJustification)
            .authorId(this.authorId)
            .authorLabel(this.authorLabel)
            .reviewerId(this.reviewerId)
            .reviewerLabel(this.reviewerLabel)
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
            .mappingSource(this.mappingSource)
            .mappingCardinality(this.mappingCardinality)
            .cardinalityScope(this.cardinalityScope)
            .mappingTool(this.mappingTool)
            .mappingToolId(this.mappingToolId)
            .mappingToolVersion(this.mappingToolVersion)
            .mappingDate(this.mappingDate)
            .publicationDate(this.publicationDate)
            .reviewDate(this.reviewDate)
            .confidence(this.confidence)
            .reviewerConfidence(this.reviewerConfidence)
            .curationRule(this.curationRule)
            .curationRuleText(this.curationRuleText)
            .subjectMatchField(this.subjectMatchField)
            .objectMatchField(this.objectMatchField)
            .matchString(this.matchString)
            .subjectPreprocessing(this.subjectPreprocessing)
            .objectPreprocessing(this.objectPreprocessing)
            .similarityScore(this.similarityScore)
            .similarityMeasure(this.similarityMeasure)
            .seeAlso(this.seeAlso)
            .issueTrackerItem(this.issueTrackerItem)
            .other(this.other)
            .comment(this.comment)
            .extensions(this.extensions);
    }
}
