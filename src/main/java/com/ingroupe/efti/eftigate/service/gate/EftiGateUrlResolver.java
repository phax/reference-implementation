package com.ingroupe.efti.eftigate.service.gate;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.eftigate.entity.GateEntity;
import com.ingroupe.efti.eftigate.repository.GateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class EftiGateUrlResolver {

    private final GateRepository gateRepository;
    public List<GateEntity> resolve(final MetadataRequestDto metadataRequestDto){
        final List<GateEntity> destinationGates;
        if (CollectionUtils.isNotEmpty(metadataRequestDto.getEFTIGateIndicator())){
            final List<CountryIndicator> countryIndicators = metadataRequestDto.getEFTIGateIndicator().stream().map(CountryIndicator::valueOf).toList();
            destinationGates = gateRepository.findByCountryIn(countryIndicators);
        } else {
             destinationGates = gateRepository.findAll();
        }
        return destinationGates;
    }
}
