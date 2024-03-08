package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;

public abstract class AbstractServiceTest {

    public final MapperUtils mapperUtils = new MapperUtils(createModelMapper());

    @Mock
    public ObjectMapper objectMapper;

    private ModelMapper createModelMapper() {
        return new ModelMapper();
    }
}
