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
    @Setter(AccessLevel.NONE)
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
    @SlotURI("http://purl.org/pav/authoredOn")
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

    /**
     * Sets the mapping_set_confidence field to a new value.
     *
     * @param value The new mapping_set_confidence value to set.
     * @throws IllegalArgumentException If the value is outside of the valid
     *                                  range.
     */
    public void setMappingSetConfidence(Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for mapping_set_confidence");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for mapping_set_confidence");
        }
        mappingSetConfidence = value;
    }

    /**
     * Gets the list of mapping_set_source values, optionally
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
     * Gets the list of cardinality_scope values, optionally
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
     * Gets the list of mappings, optionally initializing the underlying field
     * to an empty list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of mappings.
     */
    public List<Mapping> getMappings(boolean set) {
        if ( mappings == null && set ) {
            mappings = new ArrayList<>();
        }
        return mappings;
    }

    /**
     * Gets the list of extension definitions, optionally initializing the
     * underlying field to an empty list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of extension definitions.
     */
    public List<ExtensionDefinition> getExtensionDefinitions(boolean set) {
        if ( extensionDefinitions == null && set ) {
            extensionDefinitions = new ArrayList<>();
        }
        return extensionDefinitions;
    }
}
