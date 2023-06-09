package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper=false)
public class MappingSet  {
    private List<Mapping> mappings;

    @JsonProperty("mapping_set_id")
    private String mappingSetId;

    @JsonProperty("mapping_set_version")
    private String mappingSetVersion;

    @JsonProperty("mapping_set_source")
    private List<String> mappingSetSource;

    @JsonProperty("mapping_set_title")
    private String mappingSetTitle;

    @JsonProperty("mapping_set_description")
    private String mappingSetDescription;

    @JsonProperty("creator_id")
    private List<String> creatorId;

    @JsonProperty("creator_label")
    private List<String> creatorLabel;

    private String license;

    @JsonProperty("subject_type")
    private String subjectType;

    @JsonProperty("subject_source")
    private String subjectSource;

    @JsonProperty("subject_source_version")
    private String subjectSourceVersion;

    @JsonProperty("object_type")
    private String objectType;

    @JsonProperty("object_source")
    private String objectSource;

    @JsonProperty("object_source_version")
    private String objectSourceVersion;

    @JsonProperty("mapping_provider")
    private String mappingProvider;

    @JsonProperty("mapping_tool")
    private String mappingTool;

    @JsonProperty("mapping_date")
    private String mappingDate;

    @JsonProperty("subject_match_field")
    private List<String> subjectMatchField;

    @JsonProperty("object_match_field")
    private List<String> objectMatchField;

    @JsonProperty("subject_preprocessing")
    private List<String> subjectPreprocessing;

    @JsonProperty("object_preprocessing")
    private List<String> objectPreprocessing;

    @JsonProperty("see_also")
    private List<String> seeAlso;

    private String other;

    private String comment;

    @JsonProperty("curie_map")
    private Map<String,String> curieMap;
}
