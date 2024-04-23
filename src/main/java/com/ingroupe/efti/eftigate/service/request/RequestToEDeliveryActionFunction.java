package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import com.ingroupe.efti.commons.enums.StatusEnum;
import com.ingroupe.efti.eftigate.config.GateProperties;
import com.ingroupe.efti.eftigate.constant.EftiGateConstants;
import com.ingroupe.efti.eftigate.dto.RequestDto;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class RequestToEDeliveryActionFunction implements Function<RequestDto, EDeliveryAction> {

    private final GateProperties gateProperties;

    @Override
    public EDeliveryAction apply(RequestDto requestDto) {
        String requestType = requestDto.getControl().getRequestType();
        if (StringUtils.isNotBlank(requestType)) {
            if (EftiGateConstants.IDENTIFIERS_TYPES.contains(requestType)) {
                return EDeliveryAction.GET_IDENTIFIERS;
            } else if (EftiGateConstants.UIL_TYPES.contains(requestType)) {
                return getEDeliveryActionForGateToGateOrGetUil(requestDto);
            } else return null;
        }
        return null;
    }

    private EDeliveryAction getEDeliveryActionForGateToGateOrGetUil(RequestDto requestDto) {
        if (RequestTypeEnum.LOCAL_UIL_SEARCH.name().equals(requestDto.getControl().getRequestType())) {
            return EDeliveryAction.GET_UIL;
        } else if (RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH.name().equals(requestDto.getControl().getRequestType())) {
            return getEDeliveryActionResponseOrPlatformAsk(requestDto);
        } else if (RequestTypeEnum.EXTERNAL_UIL_SEARCH.name().equals(requestDto.getControl().getRequestType())) {
            return EDeliveryAction.FORWARD_UIL;
        }
        return null;
    }

    private EDeliveryAction getEDeliveryActionResponseOrPlatformAsk(RequestDto requestDto) {
        if (gateProperties.isCurrentGate(requestDto.getControl().getEftiGateUrl())
                && StatusEnum.PENDING.name().equals(requestDto.getControl().getStatus())) {
            return EDeliveryAction.GET_UIL;
        } else {
            return EDeliveryAction.FORWARD_UIL;
        }
    }
}
