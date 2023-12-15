package com.ingroupe.efti.eftigate.batch;

import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.RequestService;
import com.ingroupe.efti.eftigate.utils.RequestStatusEnum;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@Slf4j
public class RetryBatch {
    @Value("${batch.retry.size:10}")
    private Integer size;
    private final RequestService requestService;
    private final RequestRepository requestRepository;
    private final MapperUtils mapperUtils;

    RetryBatch(RequestRepository requestRepository, RequestService requestService, MapperUtils mapperUtils) {
        this.requestService = requestService;
        this.requestRepository = requestRepository;
        this.mapperUtils = mapperUtils;
    }

    @Scheduled(cron = "${batch.retry.cron}")
    @SchedulerLock(name = "TaskScheduler_scheduledTask",
            lockAtLeastFor = "PT19S", lockAtMostFor = "PT19S")
    public void scheduledTask() {
        log.info("BATCH retry started");
        List<RequestEntity> requestEntityList = requestRepository.getRequestEntitiesByStatus(RequestStatusEnum.SEND_ERROR.toString(), size, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        log.info("number of retry to do {}", requestEntityList.size());
        for (RequestEntity requestEntity: requestEntityList) {
            this.requestService.sendRetryRequest(mapperUtils.requestToRequestDto(requestEntity));
        }
        log.info("BATCH retry finished");
    }
}
