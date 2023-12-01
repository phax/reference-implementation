package com.ingroupe.efti.edeliveryapconnector;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.ingroupe.efti.edeliveryapconnector.dto.ApConfigDto;
import com.ingroupe.efti.edeliveryapconnector.dto.ApRequestDto;
import com.ingroupe.efti.edeliveryapconnector.exception.SendRequestException;
import com.ingroupe.efti.edeliveryapconnector.service.RequestSendingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class RequestServiceTest {

    private RequestSendingService service;
    private final static String FOLDER = "src/test/java/resources/wiremock";
    private static WireMockServer wireMockServer;

    @BeforeEach
    void init() {
        service = new RequestSendingService();

        wireMockServer = new WireMockServer(WireMockConfiguration
                .wireMockConfig().withRootDirectory(FOLDER).port(3838)
                .notifier(new ConsoleNotifier(true)));
        wireMockServer.start();
    }

    @Test
    void shouldBuildRequest() throws SendRequestException {
        wireMockServer.stubFor(get(urlEqualTo("/domibus/services/wsplugin?wsdl"))
                .willReturn(aResponse().withBodyFile("WebServicePlugin.wsdl")));
        wireMockServer.stubFor(post(urlEqualTo("/domibus/services/wsplugin?wsdl"))
                .willReturn(aResponse().withBodyFile("response.xml")));

        final ApRequestDto requestDto = ApRequestDto
            .builder()
            .sender("syldavia")
            .receiver("borduria")
            .body("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=")
            .apConfig(ApConfigDto.
                    builder()
                    .url("http://localhost:3838/domibus/services/wsplugin?wsdl")
                    .username("username")
                    .password("password")
                    .build()).build();

        final String result = service.sendRequest(requestDto);
        assertEquals("fc0e70cf-8d57-11ee-a62e-0242ac13000d@domibus.eu", result);
    }

    @Test
    void shouldThrowExceptionIfResponseEmpty() throws SendRequestException {
        wireMockServer.stubFor(get(urlEqualTo("/domibus/services/wsplugin?wsdl"))
                .willReturn(aResponse().withBodyFile("WebServicePlugin.wsdl")));
        wireMockServer.stubFor(post(urlEqualTo("/domibus/services/wsplugin?wsdl"))
                .willReturn(aResponse().withBodyFile("response.xml")));
        final ApRequestDto requestDto = ApRequestDto
            .builder()
            .sender("syldavia")
            .receiver("borduria")
            .body("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=")
            .apConfig(ApConfigDto.
                    builder()
                    .url("http://localhost:3838/domibus/services/wsplugin?wsdl")
                    .username("username")
                    .password("password")
                    .build()).build();

        final String result = service.sendRequest(requestDto);
        assertEquals("fc0e70cf-8d57-11ee-a62e-0242ac13000d@domibus.eu", result);
    }
}
