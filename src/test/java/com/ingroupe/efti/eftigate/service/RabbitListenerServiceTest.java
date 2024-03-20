package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.exception.TechnicalException;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.RequestRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class RabbitListenerServiceTest {

    AutoCloseable openMocks;

    @Mock
    private ControlService controlService;
    @Mock
    private RequestSendingService requestSendingService;
    @Mock
    private MapperUtils mapperUtils;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ApIncomingService apIncomingService;

    private final static String url = "url";
    private final static String password = "password";
    private final static String username = "username";

    private RabbitListenerService rabbitListenerService;

    @BeforeEach
    void before() {

        final GateProperties gateProperties = GateProperties.builder()
                .owner("france")
                .ap(GateProperties.ApConfig.builder()
                        .url(url)
                        .password(password)
                        .username(username).build()).build();
        openMocks = MockitoAnnotations.openMocks(this);

        rabbitListenerService = new RabbitListenerService(controlService, gateProperties, requestSendingService, mapperUtils, requestRepository, apIncomingService);
    }

    @Test
    void listenMessageReceiveDeadQueueTest() {
        String message = "{\"id\":0,\"journeyStart\":\"2024-01-26T10:54:51+01:00\",\"countryStart\":\"FR\",\"journeyEnd\":\"2024-01-27T10:54:51+01:00\",\"countryEnd\":\"FR\",\"metadataUUID\":\"032ad16a-ce1b-4ed2-a943-3b3975be9148\",\"transportVehicles\":[{\"id\":0,\"transportMode\":\"ROAD\",\"sequence\":1,\"vehicleId\":null,\"vehicleCountry\":\"FR\",\"journeyStart\":\"2024-01-26T10:54:51+01:00\",\"countryStart\":\"FR\",\"journeyEnd\":\"2024-01-27T10:54:51+01:00\",\"countryEnd\":\"FRANCE\"}],\"dangerousGoods\":false,\"disabled\":false,\"eFTIGateUrl\":null,\"eFTIDataUuid\":\"032ad16a-ce1b-4ed2-a943-3b3975be9169\",\"eFTIPlatformUrl\":\"http://efti.platform.acme.com\"}";

        rabbitListenerService.listenMessageReceiveDeadQueue(message);
    }

    @Test
    void listenReceiveMessageTest() {
        String message = "{\"id\":0,\"journeyStart\":\"2024-01-26T10:54:51+01:00\",\"countryStart\":\"FR\",\"journeyEnd\":\"2024-01-27T10:54:51+01:00\",\"countryEnd\":\"FR\",\"metadataUUID\":\"032ad16a-ce1b-4ed2-a943-3b3975be9148\",\"transportVehicles\":[{\"id\":0,\"transportMode\":\"ROAD\",\"sequence\":1,\"vehicleId\":null,\"vehicleCountry\":\"FR\",\"journeyStart\":\"2024-01-26T10:54:51+01:00\",\"countryStart\":\"FR\",\"journeyEnd\":\"2024-01-27T10:54:51+01:00\",\"countryEnd\":\"FRANCE\"}],\"dangerousGoods\":false,\"disabled\":false,\"eFTIGateUrl\":null,\"eFTIDataUuid\":\"032ad16a-ce1b-4ed2-a943-3b3975be9169\",\"eFTIPlatformUrl\":\"http://efti.platform.acme.com\"}";

        rabbitListenerService.listenReceiveMessage(message);
    }

    @Test
    void listenReceiveMessageExceptiontest() {
        String message = "ça va pétteeeeer";

        Exception exception = assertThrows(TechnicalException.class, () -> {
            rabbitListenerService.listenReceiveMessage(message);
        });

        Assertions.assertEquals("Error when try to map requestDto with message : ça va pétteeeeer", exception.getMessage());
    }

    @Test
    void listenSendMessageTest() {
        String message = "{\"id\":151,\"status\":\"RECEIVED\",\"edeliveryMessageId\":null,\"retry\":0,\"reponseData\":null,\"nextRetryDate\":null,\"createdDate\":[2024,3,5,15,6,52,135892300],\"lastModifiedDate\":null,\"gateUrlDest\":\"http://efti.gate.borduria.eu\",\"control\":{\"id\":102,\"eftiDataUuid\":\"12345678-ab12-4ab6-8999-123456789abe\",\"requestUuid\":\"c5ed0840-bf60-4052-8172-35530d423672\",\"requestType\":\"LOCAL_UIL_SEARCH\",\"status\":\"PENDING\",\"eftiPlatformUrl\":\"http://efti.platform.acme.com\",\"eftiGateUrl\":\"http://efti.gate.borduria.eu\",\"subsetEuRequested\":\"SubsetEuRequested\",\"subsetMsRequested\":\"SubsetMsRequested\",\"createdDate\":[2024,3,5,15,6,51,987861600],\"lastModifiedDate\":[2024,3,5,15,6,51,987861600],\"eftiData\":null,\"transportMetaData\":null,\"fromGateUrl\":null,\"requests\":null,\"authority\":{\"id\":99,\"country\":\"SY\",\"legalContact\":{\"id\":197,\"email\":\"nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn.A@63ccccccccccccccccccccccccccccccccccccccccccccccccccccccccgmail.63ccccccccccccccccccccccccccccccccccccccccccccccccccccccccgmail.commmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm\",\"streetName\":\"rue des rossignols\",\"buildingNumber\":\"12\",\"city\":\"Acheville\",\"additionalLine\":null,\"postalCode\":\"62320\"},\"workingContact\":{\"id\":198,\"email\":\"toto@gmail.com\",\"streetName\":\"rue des cafés\",\"buildingNumber\":\"14\",\"city\":\"Lille\",\"additionalLine\":\"osef\",\"postalCode\":\"59000\"},\"isEmergencyService\":null,\"name\":\"aaaa\",\"nationalUniqueIdentifier\":\"aaa\"},\"error\":null,\"metadataResults\":null},\"error\":null}";
        rabbitListenerService.listenSendMessage(message);
    }

    @Test()
    void listenSendMessageFailedBuildRequestApRequestDtoTest() {
        String message = "oki";

        Exception exception = assertThrows(TechnicalException.class, () -> {
            rabbitListenerService.listenSendMessage(message);
        });

        Assertions.assertEquals("Error when try to map requestDto with message : oki", exception.getMessage());
    }

    @Test
    void listenSendMessageFailedSendDomibusTest() {
        String message = "{\"id\":151,\"status\":\"RECEIVED\",\"edeliveryMessageId\":null,\"retry\":0,\"reponseData\":null,\"nextRetryDate\":null,\"createdDate\":[2024,3,5,15,6,52,135892300],\"lastModifiedDate\":null,\"gateUrlDest\":\"http://efti.gate.borduria.eu\",\"control\":{\"id\":102,\"eftiDataUuid\":\"12345678-ab12-4ab6-8999-123456789abe\",\"requestUuid\":\"c5ed0840-bf60-4052-8172-35530d423672\",\"requestType\":\"LOCAL_UIL_SEARCH\",\"status\":\"PENDING\",\"eftiPlatformUrl\":\"http://efti.platform.acme.com\",\"eftiGateUrl\":\"http://efti.gate.borduria.eu\",\"subsetEuRequested\":\"SubsetEuRequested\",\"subsetMsRequested\":\"SubsetMsRequested\",\"createdDate\":[2024,3,5,15,6,51,987861600],\"lastModifiedDate\":[2024,3,5,15,6,51,987861600],\"eftiData\":null,\"transportMetaData\":null,\"fromGateUrl\":null,\"requests\":null,\"authority\":{\"id\":99,\"country\":\"SY\",\"legalContact\":{\"id\":197,\"email\":\"nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn.A@63ccccccccccccccccccccccccccccccccccccccccccccccccccccccccgmail.63ccccccccccccccccccccccccccccccccccccccccccccccccccccccccgmail.commmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm\",\"streetName\":\"rue des rossignols\",\"buildingNumber\":\"12\",\"city\":\"Acheville\",\"additionalLine\":null,\"postalCode\":\"62320\"},\"workingContact\":{\"id\":198,\"email\":\"toto@gmail.com\",\"streetName\":\"rue des cafés\",\"buildingNumber\":\"14\",\"city\":\"Lille\",\"additionalLine\":\"osef\",\"postalCode\":\"59000\"},\"isEmergencyService\":null,\"name\":\"aaaa\",\"nationalUniqueIdentifier\":\"aaa\"},\"error\":null,\"metadataResults\":null},\"error\":null}";

        Mockito.when(requestSendingService.sendRequest(any(), any())).thenThrow(SendRequestException.class);

        Exception exception = assertThrows(TechnicalException.class, () -> {
            rabbitListenerService.listenSendMessage(message);
        });

        Assertions.assertEquals("Error when try to send message to domibus", exception.getMessage());
    }

    @Test
    void listenSendMessageDeadLetterTest() {
        String message = "{\"id\":151,\"status\":\"RECEIVED\",\"edeliveryMessageId\":null,\"retry\":0,\"reponseData\":null,\"nextRetryDate\":null,\"createdDate\":[2024,3,5,15,6,52,135892300],\"lastModifiedDate\":null,\"gateUrlDest\":\"http://efti.gate.borduria.eu\",\"control\":{\"id\":102,\"eftiDataUuid\":\"12345678-ab12-4ab6-8999-123456789abe\",\"requestUuid\":\"c5ed0840-bf60-4052-8172-35530d423672\",\"requestType\":\"LOCAL_UIL_SEARCH\",\"status\":\"PENDING\",\"eftiPlatformUrl\":\"http://efti.platform.acme.com\",\"eftiGateUrl\":\"http://efti.gate.borduria.eu\",\"subsetEuRequested\":\"SubsetEuRequested\",\"subsetMsRequested\":\"SubsetMsRequested\",\"createdDate\":[2024,3,5,15,6,51,987861600],\"lastModifiedDate\":[2024,3,5,15,6,51,987861600],\"eftiData\":null,\"transportMetaData\":null,\"fromGateUrl\":null,\"requests\":null,\"authority\":{\"id\":99,\"country\":\"SY\",\"legalContact\":{\"id\":197,\"email\":\"nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn.A@63ccccccccccccccccccccccccccccccccccccccccccccccccccccccccgmail.63ccccccccccccccccccccccccccccccccccccccccccccccccccccccccgmail.commmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm\",\"streetName\":\"rue des rossignols\",\"buildingNumber\":\"12\",\"city\":\"Acheville\",\"additionalLine\":null,\"postalCode\":\"62320\"},\"workingContact\":{\"id\":198,\"email\":\"toto@gmail.com\",\"streetName\":\"rue des cafés\",\"buildingNumber\":\"14\",\"city\":\"Lille\",\"additionalLine\":\"osef\",\"postalCode\":\"59000\"},\"isEmergencyService\":null,\"name\":\"aaaa\",\"nationalUniqueIdentifier\":\"aaa\"},\"error\":null,\"metadataResults\":null},\"error\":null}";
        rabbitListenerService.listenSendMessageDeadLetter(message);
    }
}
