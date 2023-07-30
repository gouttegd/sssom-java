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
public class MappingSetReference  {
    @JsonProperty("mapping_set_id")
    private String mappingSetId;

    @JsonProperty("mirror_from")
    private String mirrorFrom;

    @JsonProperty("registry_confidence")
    private Double registryConfidence;

    @JsonProperty("mapping_set_group")
    private String mappingSetGroup;

    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;

    @JsonProperty("local_name")
    private String localName;
}
