package com.ingroupe.efti.eftigate.service;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ControlService {

    private final ControlRepository controlRepository;

    public ControlEntity getById(long id) {
        Optional<ControlEntity> controlEntity = controlRepository.findById(id);
        return controlEntity.orElse(null);
    }
}
