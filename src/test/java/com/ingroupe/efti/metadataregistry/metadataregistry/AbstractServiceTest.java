package com.ingroupe.efti.metadataregistry.metadataregistry;

import com.ingroupe.efti.metadataregistry.MetadataMapper;
import org.modelmapper.ModelMapper;

public abstract class AbstractServiceTest {

    public final MetadataMapper mapperUtils = new MetadataMapper(createModelMapper());

    private ModelMapper createModelMapper() {
        return new ModelMapper();
    }
}
