package com.ingroupe.efti.eftilogger.service;

import com.ingroupe.efti.commons.dto.MetadataDto;
import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.commons.utils.SerializeUtils;
import com.ingroupe.efti.eftilogger.LogMarkerEnum;
import com.ingroupe.efti.eftilogger.dto.LogRegistryDto;
import com.ingroupe.efti.eftilogger.model.ComponentType;
import lombok.RequiredArgsConstructor;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuditRegistryLogService implements LogService<LogRegistryDto> {

    private static final LogMarkerEnum MARKER = LogMarkerEnum.REGISTRY;
    private final SerializeUtils serializeUtils;

    public void log(final MetadataDto metadataDto,
                    final String currentGateId,
                    final String currentGateCountry,
                    final String body,
                    final String errorCode) {
        final boolean isError = errorCode != null;
        final String edelivery = "EDELIVERY";
        this.log(LogRegistryDto.builder()
                .messageDate(DateTimeFormatter.ofPattern(DATE_FORMAT).format(LocalDateTime.now()))
                .componentType(ComponentType.GATE)
                .componentId(currentGateId)
                .componentCountry(currentGateCountry)
                .requestingComponentType(ComponentType.PLATFORM)
                .requestingComponentId(metadataDto.getEFTIPlatformUrl())
                .requestingComponentCountry(currentGateCountry)
                .respondingComponentType(ComponentType.GATE)
                .respondingComponentId(currentGateId)
                .respondingComponentCountry(currentGateCountry)
                .messageContent(body)
                .statusMessage(isError ? StatusEnum.ERROR.name() : StatusEnum.COMPLETE.name())
                .errorCodeMessage(isError ? errorCode : "")
                .errorDescriptionMessage(isError ? ErrorCodesEnum.valueOf(errorCode).getMessage() : "")
                .timeoutComponentType(TIMEOUT_COMPONENT_TYPE)
                .metadataId(metadataDto.getMetadataUUID())
                .eFTIDataId(metadataDto.getEFTIDataUuid())
                .interfaceType(edelivery)
                .build());
    }
        public void log(final MetadataDto metadataDto,
                    final String currentGateId,
                    final String currentGateCountry,
                    final String body) {
        this.log(metadataDto, currentGateId, currentGateCountry, body, null);

    }

    @Override
    public void log(final LogRegistryDto data) {
        final String content = serializeUtils.mapObjectToJsonString(data);
        logger.info(MarkerFactory.getMarker(MARKER.name()), content);
    }
}
