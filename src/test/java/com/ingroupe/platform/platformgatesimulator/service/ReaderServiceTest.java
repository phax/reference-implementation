package com.ingroupe.platform.platformgatesimulator.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

class ReaderServiceTest {

    AutoCloseable openMocks;

    private ReaderService readerService;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
        readerService = new ReaderService();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void readFromFileJsonTest() throws IOException {
        String result = readerService.readFromFile("./cda/test");

        Assertions.assertNotNull(result);
    }

    @Test
    void readFromFileXmlTest() throws IOException {
        String result = readerService.readFromFile("./cda/teest");

        Assertions.assertNotNull(result);
    }
}
