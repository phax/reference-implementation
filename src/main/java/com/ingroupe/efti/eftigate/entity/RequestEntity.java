package com.ingroupe.efti.eftigate.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Basic;
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
import org.springframework.cglib.core.Local;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "request", schema = "efti", catalog = "efti")
@Getter
@Setter
@Convert(attributeName = "entityAttrName", converter = JsonBinaryType.class)
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reponsedata")
    private Object reponsedata;
    
    @Column(name = "lastretrydate")
    private LocalDateTime lastretrydate;
    
    @Column(name = "createddate")
    private LocalDateTime createddate;
    
    @Column(name = "lastmodifieddate")
    private LocalDateTime lastmodifieddate;
    
    @Column(name = "gateurldest")
    private String gateurldest;
}
