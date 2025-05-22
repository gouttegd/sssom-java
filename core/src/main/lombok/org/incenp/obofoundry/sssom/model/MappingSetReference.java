package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.util.ArrayList;
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
public class MappingSetReference  {
    @JsonProperty("mapping_set_id")
    @URI
    private String mappingSetId;

    @JsonProperty("mirror_from")
    @URI
    private String mirrorFrom;

    @JsonProperty("registry_confidence")
    @Setter(AccessLevel.NONE)
    private Double registryConfidence;

    @JsonProperty("mapping_set_group")
    private String mappingSetGroup;

    @JsonProperty("last_updated")
    private LocalDate lastUpdated;

    @JsonProperty("local_name")
    private String localName;

    /**
     * Sets the registry_confidence field to a new value.
     *
     * @param value The new registry_confidence value to set.
     * @throws IllegalArgumentException If the value is outside of the valid
     *                                  range.
     */
    public void setRegistryConfidence(Double value) {
        if ( value > 1.0 ) {
            throw new IllegalArgumentException("Invalid value for registry_confidence");
        }
        if ( value < 0.0 ) {
            throw new IllegalArgumentException("Invalid value for registry_confidence");
        }
        registryConfidence = value;
    }
}
