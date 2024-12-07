package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.time.LocalDate;
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
}
