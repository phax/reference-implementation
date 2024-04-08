package com.ingroupe.efti.eftigate.service.request;

import com.ingroupe.efti.commons.enums.EDeliveryAction;
import com.ingroupe.efti.eftigate.constant.EftiGateConstants;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class RequestTypeToEDeliveryFunction implements Function<String, EDeliveryAction> {
    @Override
    public EDeliveryAction apply(String requestType) {
        if (StringUtils.isNotBlank(requestType)) {
            if (EftiGateConstants.IDENTIFIERS_TYPES.contains(requestType)) {
                return EDeliveryAction.GET_IDENTIFIERS;
            } else if (EftiGateConstants.UIL_TYPES.contains(requestType)) {
                return EDeliveryAction.GET_UIL;
            }
        } else return null;
        return null;
    }
}
