package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.config.MyForkJoinWorkerThreadFactory;
import com.ingroupe.efti.eftigate.dto.AuthorityDto;
import com.ingroupe.efti.eftigate.dto.ControlDto;
import com.ingroupe.efti.eftigate.dto.ErrorDto;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import com.ingroupe.efti.eftigate.dto.requestbody.RequestBodyDto;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.utils.ErrorCodesEnum;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Service
@AllArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestSendingService requestSendingService;
    private final GateProperties gateProperties;
    private final MapperUtils mapperUtils;
    private final ObjectMapper objectMapper;

    public RequestDto createAndSendRequest(final ControlDto controlDto) {
        RequestDto requestDto = new RequestDto(controlDto);
        log.info("Request has been register with controlId : {}", requestDto.getControl().getId());
        final RequestDto result = this.save(requestDto);
        this.sendRequest(result);
        return result;
    }

    public void sendRequest(final RequestDto requestDto) {
        final ApRequestDto apRequestDto;
        try {
            apRequestDto = buildApRequestDto(requestDto);
        } catch (TechnicalException e) {
            log.error("error while building request", e);
            requestDto.setError(ErrorDto.builder().errorCode(ErrorCodesEnum.REQUEST_BUILDING.name()).build());
            this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
            return;
        }

        //see https://stackoverflow.com/questions/49113207/completablefuture-forkjoinpool-set-class-loader/59444016#59444016
        final ForkJoinPool myCommonPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                new MyForkJoinWorkerThreadFactory()
                , null, false);

        CompletableFuture.runAsync(() -> {
            try {
                final String result = this.requestSendingService.sendRequest(apRequestDto);
                requestDto.setEdeliveryMessageId(result);
                this.updateStatus(requestDto, RequestStatusEnum.IN_PROGRESS);
            } catch (SendRequestException e) {
                log.error("error while sending request");
                requestDto.setError(ErrorDto.builder()
                        .errorCode(ErrorCodesEnum.AP_SUBMISSION_ERROR.name())
                        .errorDescription("Error while sending request to AP").build());
                this.updateStatus(requestDto, RequestStatusEnum.SEND_ERROR);
            }
        }, myCommonPool);
    }

    private RequestDto updateStatus(final RequestDto requestDto, final RequestStatusEnum status) {
        requestDto.setStatus(status.name());
        return this.save(requestDto);
    }

    private RequestDto save(final RequestDto requestDto) {
        return mapperUtils.requestToRequestDto(requestRepository.save(mapperUtils.requestDtoToRequestEntity(requestDto)));
    }

    private ApRequestDto buildApRequestDto(final RequestDto requestDto) throws TechnicalException {
        return ApRequestDto
                .builder()
                .sender(gateProperties.getOwner())
                .receiver(requestDto.getControl().getEftiPlatformUrl())
                .body(buildBody(requestDto))
                .apConfig(ApConfigDto
                        .builder()
                        .username(gateProperties.getAp().getUsername())
                        .password(gateProperties.getAp().getPassword())
                        .url(gateProperties.getAp().getUrl())
                        .build())
                .build();
    }

    private String buildBody(final RequestDto requestDto) throws TechnicalException {
        final RequestBodyDto requestBodyDto = new RequestBodyDto();
        requestBodyDto.setRequestUuid(requestDto.getControl().getRequestUuid());
        requestBodyDto.setAuthority(mapperUtils.authorityDtoToAuthorityBodyDto(new AuthorityDto()));
        requestBodyDto.setEFTIDataUuid(requestDto.getControl().getEftiDataUuid());
        requestBodyDto.setSubsetEU(new LinkedList<>());
        requestBodyDto.setSubsetMS(new LinkedList<>());

        try {
            return objectMapper.writeValueAsString(requestBodyDto);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("error while building request body", e);
        }
    }
}
