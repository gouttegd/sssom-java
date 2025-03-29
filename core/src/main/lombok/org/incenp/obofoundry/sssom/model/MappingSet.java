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
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper=false)
public class MappingSet  {
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
    private EntityType predicateType;

    @JsonProperty("mapping_provider")
    @Propagatable
    @URI
    private String mappingProvider;

    @JsonProperty("mapping_tool")
    @Propagatable
    private String mappingTool;

    @JsonProperty("mapping_tool_version")
    @Propagatable
    private String mappingToolVersion;

    @JsonProperty("mapping_date")
    @Propagatable
    @SlotURI("http://purl.org/pav/authoredOn")
    private LocalDate mappingDate;

    @JsonProperty("publication_date")
    @SlotURI("http://purl.org/dc/terms/created")
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
    private String similarityMeasure;

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
}
