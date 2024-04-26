package com.ingroupe.efti.eftigate.constant;

import com.ingroupe.efti.commons.enums.RequestTypeEnum;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.ingroupe.efti.commons.enums.RequestTypeEnum.*;

@UtilityClass
public final class EftiGateConstants {
    public static final List<RequestTypeEnum> UIL_TYPES = List.of(LOCAL_UIL_SEARCH, EXTERNAL_UIL_SEARCH, EXTERNAL_ASK_UIL_SEARCH);
    public static final List<RequestTypeEnum> IDENTIFIERS_TYPES = List.of(LOCAL_METADATA_SEARCH, EXTERNAL_METADATA_SEARCH, EXTERNAL_ASK_METADATA_SEARCH);
    public static final List<RequestTypeEnum> LOCAL_REQUESTS_TYPES = List.of(LOCAL_METADATA_SEARCH, LOCAL_UIL_SEARCH);

}
