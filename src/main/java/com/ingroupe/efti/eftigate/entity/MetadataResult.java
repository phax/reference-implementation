package com.ingroupe.efti.eftigate.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.ingroupe.efti.metadataregistry.entity.TransportVehicle;
import com.ingroupe.efti.metadataregistry.utils.OffsetDateTimeDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataResult implements Serializable {
    private long id;
    @JsonProperty("eFTIGateUrl")
    private String eFTIGateUrl;
    @JsonProperty("eFTIDataUuid")
    private String eFTIDataUuid;
    @JsonProperty("eFTIPlatformUrl")
    private String eFTIPlatformUrl;
    private boolean isDangerousGoods;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime journeyStart;
    private String countryStart;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime journeyEnd;
    private String countryEnd;
    private String metadataUUID;
    @NotEmpty(message = "TRANSPORT_VEHICLES_MISSING")
    @Valid
    private List<TransportVehicle> transportVehicles;
    private boolean isDisabled;
}
