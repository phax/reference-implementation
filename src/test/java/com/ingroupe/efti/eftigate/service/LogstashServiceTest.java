package com.ingroupe.efti.eftigate.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.ingroupe.common.test.log.MemoryAppender;
import com.ingroupe.efti.eftigate.config.GateProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogstashServiceTest {

    protected LogstashService logstashService;

    private MemoryAppender memoryAppender;

    private Logger memoryAppenderTestLogger;

    private static final String LOGGER_NAME = LogstashService.class.getName();

    @BeforeEach
    void before() {
        logstashService = new LogstashService();
        memoryAppenderTestLogger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        memoryAppender =
                MemoryAppender.createInitializedMemoryAppender(
                        Level.INFO, memoryAppenderTestLogger);
    }

    @AfterEach
    public void cleanupLogAppenderForTest() {
        MemoryAppender.shutdownMemoryAppender(memoryAppender, memoryAppenderTestLogger);
    }

    @Test
    void logSucessTest() {
        final String message = "oki";
        final String[] array = {message};

        logstashService.log(array);

        assertTrue(memoryAppender.containedInFormattedLogMessage(message));
        assertEquals(1, memoryAppender.countEventsForLogger(LOGGER_NAME, Level.INFO));
    }

    @Test
    void logSucessWithMutipleTest() {
        final String[] array = {"oki", "doki"};

        logstashService.log(array);

        assertTrue(memoryAppender.containedInFormattedLogMessage("oki|doki"));
        assertEquals(1, memoryAppender.countEventsForLogger(LOGGER_NAME, Level.INFO));
    }

    @Test
    void logNullTest() {
        final String[] array = {"oki", null, "psg>all"};

        logstashService.log(array);

        assertTrue(memoryAppender.containedInFormattedLogMessage("oki||psg>all"));
        assertEquals(1, memoryAppender.countEventsForLogger(LOGGER_NAME, Level.INFO));
    }
}
