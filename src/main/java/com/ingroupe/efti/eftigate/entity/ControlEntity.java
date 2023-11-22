package com.ingroupe.efti.eftigate.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "control", schema = "efti", catalog = "efti")
@Getter
@Setter
@Convert(attributeName = "entityAttrName", converter = JsonBinaryType.class)
public class ControlEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "eftidatauuid")
    private String eftiDataUuid;
    
    @Column(name = "requestuuid")
    private String requestUuid;

    @Column(name = "requesttype")
    private String requestType;

    @Column(name = "status")
    private String status;

    @Column(name = "eftiplatformurl")
    private String eftiPlatformUrl;

    @Column(name = "eftigateurl")
    private String eftiGateUrl;

    @Column(name = "subseteurequested")
    private String subsetEuRequested;

    @Column(name = "subsetmsrequested")
    private String subsetMsRequested;

    @Column(name = "createddate")
    private LocalDateTime createdDate;

    @Column(name = "lastmodifieddate")
    private LocalDateTime lastModifiedDate;

    @Column(name = "eftidata")
    private byte[] eftiData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transportmetadata")
    private Object transportMetadata;

    @Column(name = "fromgateurl")
    private String fromGateUrl;

}
