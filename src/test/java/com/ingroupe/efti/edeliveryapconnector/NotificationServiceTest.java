package com.ingroupe.efti.edeliveryapconnector;

import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationType;
import com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto;
import com.ingroupe.efti.edeliveryapconnector.exception.RetrieveMessageException;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.NotificationService;
import com.ingroupe.efti.edeliveryapconnector.service.RequestRetrievingService;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;

import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.MESSAGE_ID;
import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.RECEIVE_SUCCESS;
import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.SENT_FAILURE;
import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.SENT_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NotificationServiceTest {

    AutoCloseable openMocks;
    private NotificationService service;
    @Mock
    private RequestRetrievingService requestRetrievingService;

    @BeforeEach
    void init() {
        openMocks = MockitoAnnotations.openMocks(this);
        service = new NotificationService(requestRetrievingService);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void shouldConsumeNotificationAndCallRetrieveMessage() throws SendRequestException, RetrieveMessageFault {
        final String messageId = "id_du_message";
        final ApConfigDto apConfigDto = ApConfigDto.builder().build();
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of(RECEIVE_SUCCESS, Map.of(MESSAGE_ID, messageId))).build();
        when(requestRetrievingService.retrieveMessage(any(), any())).thenReturn(new NotificationContentDto());
        service.consume(apConfigDto, receivedNotificationDto);
        verify(requestRetrievingService).retrieveMessage(apConfigDto, messageId);
    }

    @Test
    void shouldReThrowExceptionIfRetrieveFail() throws SendRequestException, RetrieveMessageFault {
        final String messageId = "id_du_message";
        final ApConfigDto apConfigDto = ApConfigDto.builder().build();
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of(RECEIVE_SUCCESS, Map.of(MESSAGE_ID, messageId))).build();
        when(requestRetrievingService.retrieveMessage(apConfigDto, messageId)).thenThrow(SendRequestException.class);
        assertThrows(RetrieveMessageException.class , () -> service.consume(apConfigDto, receivedNotificationDto));
    }

    @Test
    void shouldConsumeNotificationAndManageFailNotif() throws SendRequestException, RetrieveMessageFault {
        final String messageId = "id_du_message";
        final ApConfigDto apConfigDto = ApConfigDto.builder().build();
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of(SENT_FAILURE, Map.of(MESSAGE_ID, messageId))).build();
        final Optional<NotificationDto> result  = service.consume(apConfigDto, receivedNotificationDto);
        verify(requestRetrievingService, never()).retrieveMessage(apConfigDto, messageId);
        assertTrue(result.isPresent());
        assertEquals(messageId, result.get().getMessageId());
        assertEquals(NotificationType.SEND_FAILURE, result.get().getNotificationType());
    }

    @Test
    void shouldConsumeNotificationAndManageSendSuccess() throws SendRequestException, RetrieveMessageFault {
        final String messageId = "id_du_message";
        final ApConfigDto apConfigDto = ApConfigDto.builder().build();
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of(SENT_SUCCESS, Map.of(MESSAGE_ID, messageId))).build();
        final Optional<NotificationDto> result  = service.consume(apConfigDto, receivedNotificationDto);
        verify(requestRetrievingService, never()).retrieveMessage(apConfigDto, messageId);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDoNothingIfUnknowNotif() throws SendRequestException, RetrieveMessageFault {
        final String messageId = "id_du_message";
        final ApConfigDto apConfigDto = ApConfigDto.builder().build();
        final ReceivedNotificationDto receivedNotificationDto = ReceivedNotificationDto.builder()
                .body(Map.of("random type", Map.of(MESSAGE_ID, messageId))).build();
        service.consume(apConfigDto, receivedNotificationDto);
        verify(requestRetrievingService, never()).retrieveMessage(apConfigDto, messageId);
    }

    @Test
    void setMarkedAsDownloadTest() throws MalformedURLException {
        final ApConfigDto apConfigDto = ApConfigDto.builder().build();

        service.setMarkedAsDownload(apConfigDto, "messageIdQuiEstTropBeauDeFouCommeLePsgMamaIlEstLongLeMessageId");

        verify(requestRetrievingService, times(1)).setMarkedAsDownload(any(),any());
    }
}
