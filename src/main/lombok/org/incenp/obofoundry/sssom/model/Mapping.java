package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
    private LocalDateTime mappingDate;

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

    @JsonProperty("semantic_similarity_score")
    private Double semanticSimilarityScore;

    @JsonProperty("semantic_similarity_measure")
    private String semanticSimilarityMeasure;

    @JsonProperty("see_also")
    private List<String> seeAlso;

    private String other;

    private String comment;
}
