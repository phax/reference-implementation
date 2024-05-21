package eu.efti.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.efti.commons.enums.CountryIndicator;
import eu.efti.commons.enums.StatusEnum;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "eFTIGate",
        "requestUuid",
        "status",
        "errorCode",
        "errorDescription",
        "metadata"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "body")
public class MetadataResponseDto {
    private CountryIndicator eFTIGate;
    private String requestUuid;
    private StatusEnum status;
    private String errorCode;
    private String errorDescription;
    private List<MetadataResultDto> metadata;
}
