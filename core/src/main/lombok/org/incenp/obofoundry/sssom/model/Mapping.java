package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
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
    @JsonProperty("subject_id")
    @EntityReference
    private String subjectId;

    @JsonProperty("subject_label")
    private String subjectLabel;

    @JsonProperty("subject_category")
    private String subjectCategory;

    @JsonProperty("predicate_id")
    @EntityReference
    private String predicateId;

    @JsonProperty("predicate_label")
    private String predicateLabel;

    @JsonProperty("predicate_modifier")
    private PredicateModifier predicateModifier;

    @JsonProperty("object_id")
    @EntityReference
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
    private List<String> creatorId;

    @JsonProperty("creator_label")
    private List<String> creatorLabel;

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

    @JsonProperty("mapping_provider")
    private String mappingProvider;

    @JsonProperty("mapping_source")
    @EntityReference
    private String mappingSource;

    @JsonProperty("mapping_cardinality")
    private MappingCardinality mappingCardinality;

    @JsonProperty("mapping_tool")
    private String mappingTool;

    @JsonProperty("mapping_tool_version")
    private String mappingToolVersion;

    @JsonProperty("mapping_date")
    private LocalDate mappingDate;

    @JsonProperty("publication_date")
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
    private List<String> seeAlso;

    @JsonProperty("issue_tracker_item")
    @EntityReference
    private String issueTrackerItem;

    private String other;

    private String comment;

    public void setConfidence(Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for confidence");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for confidence");
        }
        confidence = value;
    }

    public void setSimilarityScore(Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for similarity_score");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for similarity_score");
        }
        similarityScore = value;
    }

    private Map<String,ExtensionValue> extensions;

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

    public boolean isLiteral() {
        return subjectType == EntityType.RDFS_LITERAL || objectType == EntityType.RDFS_LITERAL;
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
