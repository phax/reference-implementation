package eu.efti.eftigate.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.eftigate.entity.MetadataResults;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
        generator = PropertyGenerator.class,
        property = "id")
public class RequestDto {
    private long id;
    private RequestStatusEnum status;
    private String edeliveryMessageId;
    private Integer retry;
    private byte[] reponseData;
    private LocalDateTime nextRetryDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String gateUrlDest;
    private ControlDto control;
    private ErrorDto error;
    private MetadataResults metadataResults;


    public RequestDto(final ControlDto controlDto) {
        this.status = RequestStatusEnum.RECEIVED;
        this.retry = 0;
        this.gateUrlDest = controlDto.getEftiGateUrl();
        this.control = controlDto;
    }
}
