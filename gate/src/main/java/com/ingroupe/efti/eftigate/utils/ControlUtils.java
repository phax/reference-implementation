package com.ingroupe.efti.eftigate.utils;

import com.ingroupe.efti.commons.dto.AuthorityDto;
import com.ingroupe.efti.commons.dto.ControlDto;
import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.dto.MetadataResultsDto;
import com.ingroupe.efti.commons.dto.NotesDto;
import com.ingroupe.efti.commons.dto.SearchParameter;
import com.ingroupe.efti.commons.dto.UilDto;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.edeliveryapconnector.dto.IdentifiersMessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.MessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotesMessageBodyDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import lombok.experimental.UtilityClass;

import java.util.UUID;

import static com.ingroupe.efti.commons.enums.StatusEnum.PENDING;

@UtilityClass
public class ControlUtils {

    public static final String SUBSET_EU_REQUESTED = "SubsetEuRequested";
    public static final String SUBSET_MS_REQUESTED = "SubsetMsRequested";

    public static ControlDto fromGateToGateMessageBodyDto(final MessageBodyDto messageBodyDto, final RequestTypeEnum requestTypeEnum, final NotificationDto notificationDto, final String eftiGateUrl) {
        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(messageBodyDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(eftiGateUrl);
        controlDto.setFromGateUrl(notificationDto.getContent().getFromPartyId());
        controlDto.setEftiPlatformUrl(messageBodyDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(messageBodyDto.getRequestUuid());
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(StatusEnum.PENDING);
        controlDto.setSubsetEuRequested(SUBSET_EU_REQUESTED);
        controlDto.setSubsetMsRequested(SUBSET_MS_REQUESTED);
        controlDto.setAuthority(null);
        return controlDto;
    }

    public static ControlDto fromGateToGateNoteMessageBodyDto(final NotesMessageBodyDto messageBodyDto, final RequestTypeEnum requestTypeEnum, final NotificationDto notificationDto, final String eftiGateUrl) {
        final ControlDto controlDto = initControlDto(messageBodyDto, requestTypeEnum, notificationDto, eftiGateUrl);
        controlDto.setNotes(messageBodyDto.getNote());
        return controlDto;
    }

    private static ControlDto initControlDto(final NotesMessageBodyDto messageBodyDto, final RequestTypeEnum requestTypeEnum, final NotificationDto notificationDto, final String eftiGateUrl) {
        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(messageBodyDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(eftiGateUrl);
        controlDto.setFromGateUrl(notificationDto.getContent().getFromPartyId());
        controlDto.setEftiPlatformUrl(messageBodyDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(messageBodyDto.getRequestUuid());
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(StatusEnum.PENDING);
        controlDto.setSubsetEuRequested(SUBSET_EU_REQUESTED);
        controlDto.setSubsetMsRequested(SUBSET_MS_REQUESTED);
        controlDto.setAuthority(null);
        return controlDto;
    }

    public static ControlDto fromUilControl(final UilDto uilDto, final RequestTypeEnum requestTypeEnum) {
        final String uuidGenerator = UUID.randomUUID().toString();

        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(uilDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(uilDto.getEFTIGateUrl());
        controlDto.setEftiPlatformUrl(uilDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(uuidGenerator);
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(StatusEnum.PENDING);
        controlDto.setSubsetEuRequested(SUBSET_EU_REQUESTED);
        controlDto.setSubsetMsRequested(SUBSET_MS_REQUESTED);
        controlDto.setAuthority(uilDto.getAuthority());
        return controlDto;
    }

    public static ControlDto fromLocalMetadataControl(final MetadataRequestDto metadataRequestDto, final RequestTypeEnum requestTypeEnum) {
        final AuthorityDto authorityDto = metadataRequestDto.getAuthority();

        final ControlDto controlDto = getControlFrom(requestTypeEnum, authorityDto, UUID.randomUUID().toString());
        controlDto.setTransportMetaData(SearchParameter.builder()
                .vehicleId(metadataRequestDto.getVehicleID())
                .transportMode(metadataRequestDto.getTransportMode())
                .vehicleCountry(metadataRequestDto.getVehicleCountry())
                .isDangerousGoods(metadataRequestDto.getIsDangerousGoods())
                .build());
        return controlDto;
    }

    public static ControlDto fromExternalMetadataControl(final IdentifiersMessageBodyDto messageBodyDto, final RequestTypeEnum requestTypeEnum, final String fromGateUrl, final String eftiGateUrl, final MetadataResultsDto metadataResults) {
        final ControlDto controlDto = getControlFrom(requestTypeEnum, null, messageBodyDto.getRequestUuid());
        //to check
        controlDto.setEftiGateUrl(eftiGateUrl);
        controlDto.setFromGateUrl(fromGateUrl);
        controlDto.setTransportMetaData(SearchParameter.builder()
                .vehicleId(messageBodyDto.getVehicleID())
                .transportMode(messageBodyDto.getTransportMode())
                .vehicleCountry(messageBodyDto.getVehicleCountry())
                .isDangerousGoods(messageBodyDto.getIsDangerousGoods())
                .build());
        controlDto.setMetadataResults(metadataResults);
        return controlDto;
    }

    private static ControlDto getControlFrom(final RequestTypeEnum requestTypeEnum, final AuthorityDto authorityDto, final String requestUuid) {
        final ControlDto controlDto = new ControlDto();
        controlDto.setRequestUuid(requestUuid);
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(PENDING);
        controlDto.setSubsetEuRequested(SUBSET_EU_REQUESTED);
        controlDto.setSubsetMsRequested(SUBSET_MS_REQUESTED);
        controlDto.setAuthority(authorityDto);
        return controlDto;
    }

    public static ControlDto fromNotesControl(final NotesDto notesDto, final RequestTypeEnum requestTypeEnum) {
        final String uuidGenerator = UUID.randomUUID().toString();

        final ControlDto controlDto = new ControlDto();
        controlDto.setEftiDataUuid(notesDto.getEFTIDataUuid());
        controlDto.setEftiGateUrl(notesDto.getEFTIGateUrl());
        controlDto.setEftiPlatformUrl(notesDto.getEFTIPlatformUrl());
        controlDto.setRequestUuid(uuidGenerator);
        controlDto.setRequestType(requestTypeEnum);
        controlDto.setStatus(PENDING);
        controlDto.setSubsetEuRequested(SUBSET_EU_REQUESTED);
        controlDto.setSubsetMsRequested(SUBSET_MS_REQUESTED);
        return controlDto;
    }
}
