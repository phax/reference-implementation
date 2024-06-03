package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.dto.logstash.LogstashAllDto;
import com.ingroupe.efti.eftigate.dto.logstash.LogstashRequestDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class LogstashAllDtoTest {

    @Test
    void getLinkedListFieldsMultipleTest() {
        final LogstashRequestDto logstashRequestDto = new LogstashRequestDto();
        logstashRequestDto.setComponentCountry("componentCountry");
        logstashRequestDto.setErrorCodeMessage("errorCodeMessage");

        final String[] result = logstashRequestDto.getLinkedListFields();

        Assertions.assertEquals(24, result.length);
        Assertions.assertTrue(Arrays.stream(result).toList().contains("componentCountry"));
        Assertions.assertTrue(Arrays.stream(result).toList().contains("errorCodeMessage"));
    }

    @Test
    void getLinkedListFieldsOnlyOneTest() {
        final LogstashRequestDto logstashRequestDto = new LogstashRequestDto();
        logstashRequestDto.setOfficerId("setOfficerId");

        final String[] result = logstashRequestDto.getLinkedListFields();

        Assertions.assertEquals(24, result.length);
        Assertions.assertTrue(Arrays.stream(result).toList().contains("setOfficerId"));
    }

    @Test
    void getLinkedListFieldsOnlyNullTest() {
        final LogstashRequestDto logstashRequestDto = new LogstashRequestDto();

        final String[] result = logstashRequestDto.getLinkedListFields();

        Assertions.assertEquals(24, result.length);
        Arrays.stream(result).forEach(value -> Assertions.assertEquals(null, value));
    }
}
