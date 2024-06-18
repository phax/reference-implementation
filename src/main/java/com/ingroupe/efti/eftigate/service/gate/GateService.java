package com.ingroupe.efti.eftigate.service.gate;

import com.ingroupe.efti.eftigate.dto.GateDto;
import com.ingroupe.efti.eftigate.mapper.MapperUtils;
import com.ingroupe.efti.eftigate.repository.GateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class GateService {

    private final MapperUtils mapperUtils;

    private final GateRepository gateRepository;

    public boolean checkGateUrl(final String gateUrl) {
        if (StringUtils.isBlank(gateUrl)) {
            return false;
        }
        GateDto gateDto = mapperUtils.gateEntityToGateDto(gateRepository.findByUrl(gateUrl));
        return gateDto != null;
    }
}
