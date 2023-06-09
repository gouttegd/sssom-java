package org.incenp.obofoundry.sssom.model;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    private String lastUpdated;

    @JsonProperty("local_name")
    private String localName;
}
