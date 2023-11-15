package com.ingroupe.efti.eftigate.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "request", schema = "efti", catalog = "efti")
@Getter
@Setter
public class RequestEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    
    @Column(name = "controlid")
    private int controlid;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "edeliverymessageid")
    private String edeliverymessageid;
    
    @Column(name = "retry")
    private Integer retry;
    
    @Column(name = "reponsedata")
    private Object reponsedata;
    
    @Column(name = "lastretrydate")
    private Timestamp lastretrydate;
    
    @Column(name = "createddate")
    private Timestamp createddate;
    
    @Column(name = "lastmodifieddate")
    private Timestamp lastmodifieddate;
    
    @Column(name = "gateurldest")
    private String gateurldest;
}
