package com.ingroupe.efti.eftigate.batch;

import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import com.ingroupe.efti.eftigate.service.AbstractServceTest;
import com.ingroupe.efti.eftigate.service.RequestService;
import com.ingroupe.efti.commons.enums.RequestStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RetryBatchTest extends AbstractServceTest {

    @Mock
    private RetryBatch retryBatch;

    @Mock
    private RequestRepository requestRepository = Mockito.mock(RequestRepository.class);

    @Mock
    private RequestService requestService = Mockito.mock(RequestService.class);

    private List<RequestEntity> requestEntityList;

    @BeforeEach
    public void before() {
        retryBatch = new RetryBatch(requestRepository, requestService, mapperUtils);

        RequestEntity requestEntity = new RequestEntity();
        requestEntityList = List.of(requestEntity);
    }

    @Test
    void scheduledTaskTest() {
        ReflectionTestUtils.setField(retryBatch,"size", 10);
        when(requestRepository.getRequestEntitiesByStatus(eq(RequestStatusEnum.SEND_ERROR.toString()), eq(10), any())).thenReturn(requestEntityList);
        retryBatch.scheduledTask();
    }
}
