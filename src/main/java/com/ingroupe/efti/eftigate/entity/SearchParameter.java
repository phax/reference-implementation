package com.ingroupe.efti.eftigate.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchParameter implements Serializable {
    private String vehicleId;
    private Boolean isDangerousGoods;
    private String vehicleCountry;
    private String transportMode;
}
