package eu.efti.eftigate.service.gate;

import eu.efti.commons.dto.MetadataRequestDto;
import eu.efti.commons.enums.CountryIndicator;
import eu.efti.eftigate.entity.GateEntity;
import eu.efti.eftigate.repository.GateRepository;
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
    public List<String> resolve(final MetadataRequestDto metadataRequestDto){
        final List<GateEntity> destinationGates;
        if (CollectionUtils.isNotEmpty(metadataRequestDto.getEFTIGateIndicator())){
            final List<CountryIndicator> countryIndicators = metadataRequestDto.getEFTIGateIndicator().stream().map(CountryIndicator::valueOf).toList();
            destinationGates = gateRepository.findByCountryIn(countryIndicators);
        } else {
             destinationGates = gateRepository.findAll();
        }
        return CollectionUtils.emptyIfNull(destinationGates).stream()
                .map(GateEntity::getUrl)
                .toList();
    }
}
