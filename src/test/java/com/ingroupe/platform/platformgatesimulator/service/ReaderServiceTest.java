package com.ingroupe.platform.platformgatesimulator.service;

import com.ingroupe.platform.platformgatesimulator.config.GateProperties;
import com.ingroupe.platform.platformgatesimulator.exception.UuidFileNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReaderServiceTest {

    AutoCloseable openMocks;

    private ReaderService readerService;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        final GateProperties gateProperties = GateProperties.builder()
                .owner("france")
                .minSleep(1000)
                .maxSleep(2000)
                .cdaPath("C:\\projet\\EFTI\\platform-gate-simulator\\src\\main\\resources\\cda\\")
                .ap(GateProperties.ApConfig.builder()
                        .url("url")
                        .password("password")
                        .username("username").build()).build();
        readerService = new ReaderService(gateProperties);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void uploadFileNullTest() {
        assertThrows(NullPointerException.class, () -> readerService.uploadFile(null));
    }

    @Test
    void uploadFileTest() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "teest.xml",
                "fileName",
                "text/plain",
                "content".getBytes());

        readerService.uploadFile(mockMultipartFile);
    }

    @Test
    void readFromFileJsonTest() throws IOException, UuidFileNotFoundException {
        String result = readerService.readFromFile("C:\\projet\\EFTI\\platform-gate-simulator\\src\\main\\resources\\cda\\test");

        Assertions.assertNotNull(result);
    }

    @Test
    void readFromFileXmlTest() throws IOException, UuidFileNotFoundException {
        String result = readerService.readFromFile("C:\\projet\\EFTI\\platform-gate-simulator\\src\\main\\resources\\cda\\teest");

        Assertions.assertNotNull(result);
    }

    @Test
    void readFromFileXmlNullTest() throws IOException, UuidFileNotFoundException {
        String result = readerService.readFromFile("C:\\projet\\EFTI\\platform-gate-simulator\\src\\main\\resources\\cda\\bouuuuuh");

        Assertions.assertNull(result);
    }
}
