package com.ingroupe.platform.platformgatesimulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import com.ingroupe.platform.platformgatesimulator.exception.UuidFileNotFoundException;
import com.sun.istack.ByteArrayDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(GateProperties.class)
@TestPropertySource("classpath:application-test.properties")
class ApIncomingServiceTest {

    AutoCloseable openMocks;


    @Mock
    private RequestSendingService requestSendingService;

    @Mock
    private NotificationService notificationService;
    @Mock
    private GateProperties gateProperties;

    @Mock
    private ReaderService readerService;

    @Mock
    private ObjectMapper objectMapper;

    private ApIncomingService apIncomingService;

    @BeforeEach
    public void before() {
        final GateProperties gateProperties = GateProperties.builder()
                .owner("france")
                .minSleep(1000)
                .maxSleep(2000)
                .cdaPath("./cda/")
                .ap(GateProperties.ApConfig.builder()
                        .url("url")
                        .password("password")
                        .username("username").build()).build();
        openMocks = MockitoAnnotations.openMocks(this);
        apIncomingService = new ApIncomingService(requestSendingService, notificationService, gateProperties, readerService, objectMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void manageIncomingNotificationBadFilesTest() throws IOException, UuidFileNotFoundException, InterruptedException {
        final String body = """
        {
            "requestUuid" : "reques",
            "eFTIDataUuid" : "oki",
            "status": "oki",
            "errorDescription": "oki",
            "eftiData": "oki"
        }
        """;

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setContent(NotificationContentDto.builder()
                .messageId("messageId")
                .body(new ByteArrayDataSource(body.getBytes(), "osef"))
                .action(EDeliveryAction.GET_UIL.getValue())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .build());
        Mockito.when(notificationService.consume(any(), any())).thenReturn(Optional.of(notificationDto));
        apIncomingService.manageIncomingNotification(new ReceivedNotificationDto());
    }

    @Test
    void manageIncomingNotificationTest() throws IOException, UuidFileNotFoundException, InterruptedException {
        final String body = """
        {
            "requestUuid" : "12345678-ab12-4ab6-8999-123456789abc",
            "eFTIDataUuid" : "12345678-ab12-4ab6-8999-123456789abc",
            "status": "oki",
            "errorDescription": "oki",
            "eftiData": "oki"
        }
        """;

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setContent(NotificationContentDto.builder()
                .messageId("messageId")
                .body(new ByteArrayDataSource(body.getBytes(), "osef"))
                .action(EDeliveryAction.GET_UIL.getValue())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .build());
        Mockito.when(notificationService.consume(any(), any())).thenReturn(Optional.of(notificationDto));
        Mockito.when(readerService.readFromFile(any())).thenReturn("eftidata");
        apIncomingService.manageIncomingNotification(new ReceivedNotificationDto());
    }
}
