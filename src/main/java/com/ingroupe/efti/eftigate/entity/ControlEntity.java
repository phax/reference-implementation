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
    private String eftidatauuid;
    
    @Column(name = "requestuuid")
    private String requestuuid;

    @Column(name = "requesttype")
    private String requesttype;

    @Column(name = "status")
    private String status;

    @Column(name = "eftiplatformurl")
    private String eftiplatformurl;

    @Column(name = "eftigateurl")
    private String eftigateurl;

    @Column(name = "subseteurequested")
    private String subseteurequested;

    @Column(name = "subsetmsrequested")
    private String subsetmsrequested;

    @Column(name = "createddate")
    private LocalDateTime createddate;

    @Column(name = "lastmodifieddate")
    private LocalDateTime lastmodifieddate;

    @Column(name = "eftidata")
    private byte[] eftidata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transportmetadata")
    private Object transportmetadata;

    @Column(name = "fromgateurl")
    private String fromgateurl;

}
