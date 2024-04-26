package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.mapper.SerializeUtils;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;

public abstract class AbstractServiceTest {

    @Spy
    public final MapperUtils mapperUtils = new MapperUtils(createModelMapper());

    public final SerializeUtils serializeUtils = new SerializeUtils(objectMapper(), xmlMapper());

    @Mock
    public ObjectMapper objectMapper;

    private ModelMapper createModelMapper() {
        return new ModelMapper();
    }

    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public XmlMapper xmlMapper() {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.registerModule(new JavaTimeModule());
        return xmlMapper;
    }
}
