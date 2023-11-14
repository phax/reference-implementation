package com.ingroupe.efti.eftigate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "control", schema = "efti", catalog = "efti")
@Getter
@Setter
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
    private Object subseteurequested;

    @Column(name = "subsetmsrequested")
    private Object subsetmsrequested;

    @Column(name = "createddate")
    private Timestamp createddate;

    @Column(name = "lastmodifieddate")
    private Timestamp lastmodifieddate;

    @Column(name = "eftidata")
    private byte[] eftidata;

    @Column(name = "transportmetadata")
    private Object transportmetadata;

    @Column(name = "fromgateurl")
    private String fromgateurl;

}
