package com.ingroupe.efti.eftigate.constant;

import lombok.experimental.UtilityClass;

import java.util.List;

import static com.ingroupe.efti.commons.enums.RequestTypeEnum.*;

@UtilityClass
public final class EftiGateConstants {
    public static final List<String> UIL_TYPES = List.of(LOCAL_UIL_SEARCH.name(), EXTERNAL_UIL_SEARCH.name(), EXTERNAL_ASK_UIL_SEARCH.name());
    public static final List<String> IDENTIFIERS_TYPES = List.of(LOCAL_METADATA_SEARCH.name(), EXTERNAL_METADATA_SEARCH.name(), EXTERNAL_ASK_METADATA_SEARCH.name());
    public static final List<String> LOCAL_REQUESTS_TYPES = List.of(LOCAL_METADATA_SEARCH.name(), LOCAL_UIL_SEARCH.name());

}
