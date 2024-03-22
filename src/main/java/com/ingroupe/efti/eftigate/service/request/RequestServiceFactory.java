package com.ingroupe.efti.eftigate.service.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class RequestServiceFactory {

    private final List<RequestService> requestServices;

    public RequestService getRequestServiceByRequestType(final String requestType)
    {
        return requestServices.stream()
                .filter(requestService -> requestService.supports(requestType)).findFirst()
                .orElseThrow();
    }
}
