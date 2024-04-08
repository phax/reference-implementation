package com.ingroupe.efti.eftigate.service.gate;

import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.eftigate.config.GateProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class GateToRequestTypeFunction implements Function<List<String>, RequestTypeEnum> {
    private final GateProperties gateProperties;
    @Override
    public RequestTypeEnum apply(List<String> gatesUrls) {
        if (gatesUrls != null && gatesUrls.size() == 1){
            return getRequestTypeEnumForSingleUrl(gatesUrls);
        }
        return RequestTypeEnum.EXTERNAL_METADATA_SEARCH;
    }

    private RequestTypeEnum getRequestTypeEnumForSingleUrl(List<String> gatesUrls) {
        String destUrl = gatesUrls.iterator().next();
        if (gateProperties != null && destUrl.equalsIgnoreCase(gateProperties.getOwner())){
            return RequestTypeEnum.LOCAL_METADATA_SEARCH;
        } else {
            return RequestTypeEnum.EXTERNAL_ASK_METADATA_SEARCH;
        }
    }
}
