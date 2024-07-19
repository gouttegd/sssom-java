package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.time.LocalDate;
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
public class LiteralMapping  {
    private String literal;

    @JsonProperty("literal_datatype")
    private String literalDatatype;

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

    @JsonProperty("literal_source")
    @EntityReference
    private String literalSource;

    @JsonProperty("literal_source_version")
    private String literalSourceVersion;

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

    @Setter(AccessLevel.NONE)
    private Double confidence;

    @JsonProperty("object_match_field")
    @EntityReference
    private List<String> objectMatchField;

    @JsonProperty("match_string")
    private List<String> matchString;

    @JsonProperty("literal_preprocessing")
    @EntityReference
    private List<String> literalPreprocessing;

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
}
