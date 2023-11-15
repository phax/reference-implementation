package com.ingroupe.efti.eftigate.service;


import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

class ControlServiceTest {
    @Mock
    ControlRepository controlRepository = Mockito.mock(ControlRepository.class);

    @InjectMocks
    ControlService controlService = new ControlService(controlRepository);

    @Test
    void getByIdWithDataTest() {
        Mockito.when(controlRepository.findById(1L)).thenReturn(Optional.of(new ControlEntity()));

        ControlEntity controlEntity = controlService.getById(1L);

        Mockito.verify(controlRepository, Mockito.times(1)).findById(1L);
        Assertions.assertNotNull(controlEntity);
    }
}
