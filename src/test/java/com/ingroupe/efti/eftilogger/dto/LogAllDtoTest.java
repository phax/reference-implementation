package com.ingroupe.efti.eftilogger.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class LogAllDtoTest {

    @Test
    void getLinkedListFieldsMultipleTest() {
        final LogRequestDto logstashRequestDto = LogRequestDto.builder()
                .componentCountry("componentCountry")
                .errorCodeMessage("errorCodeMessage")
                .build();

        final String[] result = logstashRequestDto.getLinkedListFields();

        Assertions.assertEquals(24, result.length);
        Assertions.assertTrue(Arrays.stream(result).toList().contains("componentCountry"));
        Assertions.assertTrue(Arrays.stream(result).toList().contains("errorCodeMessage"));
    }

    @Test
    void getLinkedListFieldsOnlyOneTest() {
        final LogRequestDto logstashRequestDto = LogRequestDto.builder()
                .officerId("setOfficerId")
                .build();

        final String[] result = logstashRequestDto.getLinkedListFields();

        Assertions.assertEquals(24, result.length);
        Assertions.assertTrue(Arrays.stream(result).toList().contains("setOfficerId"));
    }

    @Test
    void getLinkedListFieldsOnlyNullTest() {
        final LogRequestDto logstashRequestDto = LogRequestDto.builder()
                .build();

        final String[] result = logstashRequestDto.getLinkedListFields();

        Assertions.assertEquals(24, result.length);
        Arrays.stream(result).forEach(value -> Assertions.assertNull(value));
    }
}
