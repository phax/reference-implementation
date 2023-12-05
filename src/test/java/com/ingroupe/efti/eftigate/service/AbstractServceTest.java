package com.ingroupe.efti.eftigate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public abstract class AbstractServceTest {

    public final MapperUtils mapperUtils = new MapperUtils(createModelMapper());

    @Mock
    public ObjectMapper objectMapper;

    private ModelMapper createModelMapper() {
        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
        return modelMapper;
    }
}
