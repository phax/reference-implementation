package eu.efti.eftigate.service;

import eu.efti.commons.dto.MetadataResultDto;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.edeliveryapconnector.service.RequestUpdaterService;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.ControlDto;
import eu.efti.eftigate.dto.RequestDto;
import eu.efti.eftigate.dto.UilDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.MetadataResult;
import eu.efti.eftigate.entity.MetadataResults;
import eu.efti.eftigate.entity.RequestEntity;
import eu.efti.eftigate.repository.RequestRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest extends AbstractServiceTest {
    @Mock
    protected RequestRepository requestRepository;
    @Mock
    protected RabbitSenderService rabbitSenderService;
    @Mock
    protected ControlService controlService;
    @Mock
    protected RequestUpdaterService requestUpdaterService;


    protected GateProperties gateProperties;

    protected final UilDto uilDto = new UilDto();
    protected final ControlDto controlDto = new ControlDto();
    protected final ControlEntity controlEntity = new ControlEntity();
    protected final RequestEntity requestEntity = new RequestEntity();
    protected final RequestEntity secondRequestEntity = new RequestEntity();
    protected final RequestDto requestDto = new RequestDto();
    protected final MetadataResult metadataResult = new MetadataResult();
    protected final MetadataResults metadataResults = new MetadataResults();
    protected final MetadataResultDto metadataResultDto = new MetadataResultDto();


    public void before() {
        gateProperties = GateProperties.builder().ap(GateProperties.ApConfig.builder().url("url").password("pwd").username("usr").build()).owner("owner").build();


        final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        final String requestUuid = "67fe38bd-6bf7-4b06-b20e-206264bd639c";

        this.uilDto.setEFTIGateUrl("gate");
        this.uilDto.setEFTIDataUuid("uuid");
        this.uilDto.setEFTIPlatformUrl("plateform");
        this.controlDto.setEftiDataUuid(uilDto.getEFTIDataUuid());
        this.controlDto.setEftiGateUrl(uilDto.getEFTIGateUrl());
        this.controlDto.setEftiPlatformUrl(uilDto.getEFTIPlatformUrl());
        this.controlDto.setRequestUuid(requestUuid);
        this.controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH);
        this.controlDto.setStatus(StatusEnum.PENDING);
        this.controlDto.setSubsetEuRequested("oki");
        this.controlDto.setSubsetMsRequested("oki");
        this.controlDto.setCreatedDate(localDateTime);
        this.controlDto.setLastModifiedDate(localDateTime);

        this.controlEntity.setEftiDataUuid(controlDto.getEftiDataUuid());
        this.controlEntity.setRequestUuid(controlDto.getRequestUuid());
        this.controlEntity.setRequestType(controlDto.getRequestType());
        this.controlEntity.setStatus(controlDto.getStatus());
        this.controlEntity.setEftiPlatformUrl(controlDto.getEftiPlatformUrl());
        this.controlEntity.setEftiGateUrl(controlDto.getEftiGateUrl());
        this.controlEntity.setSubsetEuRequested(controlDto.getSubsetEuRequested());
        this.controlEntity.setSubsetMsRequested(controlDto.getSubsetMsRequested());
        this.controlEntity.setCreatedDate(controlDto.getCreatedDate());
        this.controlEntity.setLastModifiedDate(controlDto.getLastModifiedDate());
        this.controlEntity.setEftiData(controlDto.getEftiData());
        this.controlEntity.setTransportMetadata(controlDto.getTransportMetaData());
        this.controlEntity.setFromGateUrl(controlDto.getFromGateUrl());

        this.requestDto.setStatus(RequestStatusEnum.RECEIVED);
        this.requestDto.setRetry(0);
        this.requestDto.setCreatedDate(localDateTime);
        this.requestDto.setGateUrlDest(controlEntity.getEftiGateUrl());
        this.requestDto.setControl(ControlDto.builder().id(1).build());
        this.requestDto.setControl(controlDto);

        this.requestEntity.setStatus(this.requestDto.getStatus());
        this.requestEntity.setRetry(this.requestDto.getRetry());
        this.requestEntity.setCreatedDate(this.requestEntity.getCreatedDate());
        this.requestEntity.setGateUrlDest(this.requestDto.getGateUrlDest());
        this.requestEntity.setControl(controlEntity);

        this.secondRequestEntity.setStatus(this.requestDto.getStatus());
        this.secondRequestEntity.setRetry(this.requestDto.getRetry());
        this.secondRequestEntity.setCreatedDate(this.requestEntity.getCreatedDate());
        this.secondRequestEntity.setGateUrlDest(this.requestDto.getGateUrlDest());
        this.secondRequestEntity.setControl(controlEntity);

        controlEntity.setRequests(List.of(requestEntity, secondRequestEntity));

        metadataResult.setCountryStart("FR");
        metadataResult.setCountryEnd("FR");
        metadataResult.setDisabled(false);
        metadataResult.setDangerousGoods(true);

        metadataResults.setMetadataResult(Collections.singletonList(metadataResult));

        metadataResultDto.setCountryStart("FR");
        metadataResultDto.setCountryEnd("FR");
        metadataResultDto.setDisabled(false);
        metadataResultDto.setDangerousGoods(true);    }

}
