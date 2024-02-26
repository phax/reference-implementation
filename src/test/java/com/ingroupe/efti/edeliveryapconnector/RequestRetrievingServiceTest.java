package com.ingroupe.efti.edeliveryapconnector;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.NotificationContentDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestRetrievingService;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
class RequestRetrievingServiceTest {

    private RequestRetrievingService service;
    private final static String FOLDER = "src/test/java/resources/wiremock";
    private WireMockServer wireMockServer;

    @BeforeEach
    void init() {
        service = new RequestRetrievingService();

        wireMockServer = new WireMockServer(WireMockConfiguration
                .wireMockConfig().withRootDirectory(FOLDER).dynamicPort()
                .notifier(new ConsoleNotifier(true)));
        wireMockServer.start();
    }

    @Test
    void shouldBuildRequest() throws SendRequestException, RetrieveMessageFault {
        final String messageId = "messageId";

        wireMockServer.stubFor(get(urlEqualTo("/domibus/services/wsplugin?wsdl"))
                .willReturn(aResponse().withBodyFile("WebServicePlugin.wsdl")));
        wireMockServer.stubFor(post(urlEqualTo("/domibus/services/wsplugin?wsdl"))
                .willReturn(aResponse().withBodyFile("retrieve-response.xml")));

        final ApConfigDto requestDto = ApConfigDto.builder()
                        .url(String.format("http://localhost:%s/domibus/services/wsplugin?wsdl", wireMockServer.port()))
                        .username("username")
                        .password("password")
                        .build();

        final NotificationContentDto result = service.retrieveMessage(requestDto, messageId);
        assertNotNull(result);
        assertEquals("getUIL", result.getAction());
        assertEquals("9992596f-9a6a-11ee-90b4-0242ac13000e@domibus.eu", result.getMessageId());
        assertNotNull(result.getBody());
    }
}
