package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper=false)
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

    @JsonProperty("mapping_tool")
    private String mappingTool;

    @JsonProperty("mapping_tool_id")
    @EntityReference
    @Versionable(addedIn = Version.SSSOM_1_1)
    private String mappingToolId;

    @JsonProperty("mapping_tool_version")
    private String mappingToolVersion;

    @JsonProperty("mapping_date")
    @SlotURI("http://purl.org/pav/authoredOn")
    private LocalDate mappingDate;

    @JsonProperty("publication_date")
    @SlotURI("http://purl.org/dc/terms/created")
    private LocalDate publicationDate;

    @Setter(AccessLevel.NONE)
    private Double confidence;

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
    @Setter(AccessLevel.NONE)
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

    /**
     * Sets the confidence field to a new value.
     *
     * @param value The new confidence value to set.
     * @throws IllegalArgumentException If the value is outside of the valid
     *                                  range.
     */
    public void setConfidence(Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for confidence");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for confidence");
        }
        confidence = value;
    }

    /**
     * Sets the similarity_score field to a new value.
     *
     * @param value The new similarity_score value to set.
     * @throws IllegalArgumentException If the value is outside of the valid
     *                                  range.
     */
    public void setSimilarityScore(Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for similarity_score");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for similarity_score");
        }
        similarityScore = value;
    }

    /**
     * Gets the list of author_id values, optionally
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
     * Gets the list of author_label values, optionally
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
     * Gets the list of reviewer_id values, optionally
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
     * Gets the list of reviewer_label values, optionally
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
     * Gets the list of creator_id values, optionally
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
     * Gets the list of creator_label values, optionally
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
     * Gets the list of curation_rule values, optionally
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
     * Gets the list of curation_rule_text values, optionally
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
     * Gets the list of subject_match_field values, optionally
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
     * Gets the list of object_match_field values, optionally
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
     * Gets the list of match_string values, optionally
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
     * Gets the list of subject_preprocessing values, optionally
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
     * Gets the list of object_preprocessing values, optionally
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
     * Gets the list of see_also values, optionally
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

    private Map<String,ExtensionValue> extensions;

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
     * Creates a canonical S-expression representing this mapping.
     *
     * @return A String uniquely representing this mapping, as a canonical S-expression.
     */
    public String toSExpr() {
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
            for ( String v : authorId ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( authorLabel != null ) {
            sb.append("(12:author_label(");
            for ( String v : authorLabel ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( reviewerId != null ) {
            sb.append("(11:reviewer_id(");
            for ( String v : reviewerId ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( reviewerLabel != null ) {
            sb.append("(14:reviewer_label(");
            for ( String v : reviewerLabel ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( creatorId != null ) {
            sb.append("(10:creator_id(");
            for ( String v : creatorId ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( creatorLabel != null ) {
            sb.append("(13:creator_label(");
            for ( String v : creatorLabel ) {
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
        if ( mappingCardinality != null ) {
            String v = String.valueOf(mappingCardinality);
            sb.append(String.format("(19:mapping_cardinality%d:%s)", v.length(), v));
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
        if ( confidence != null ) {
            String v = String.valueOf(confidence);
            sb.append(String.format("(10:confidence%d:%s)", v.length(), v));
        }
        if ( curationRule != null ) {
            sb.append("(13:curation_rule(");
            for ( String v : curationRule ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( curationRuleText != null ) {
            sb.append("(18:curation_rule_text(");
            for ( String v : curationRuleText ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( subjectMatchField != null ) {
            sb.append("(19:subject_match_field(");
            for ( String v : subjectMatchField ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( objectMatchField != null ) {
            sb.append("(18:object_match_field(");
            for ( String v : objectMatchField ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( matchString != null ) {
            sb.append("(12:match_string(");
            for ( String v : matchString ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( subjectPreprocessing != null ) {
            sb.append("(21:subject_preprocessing(");
            for ( String v : subjectPreprocessing ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( objectPreprocessing != null ) {
            sb.append("(20:object_preprocessing(");
            for ( String v : objectPreprocessing ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
        }
        if ( similarityScore != null ) {
            String v = String.valueOf(similarityScore);
            sb.append(String.format("(16:similarity_score%d:%s)", v.length(), v));
        }
        if ( similarityMeasure != null ) {
            String v = String.valueOf(similarityMeasure);
            sb.append(String.format("(18:similarity_measure%d:%s)", v.length(), v));
        }
        if ( seeAlso != null ) {
            sb.append("(8:see_also(");
            for ( String v : seeAlso ) {
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
}
