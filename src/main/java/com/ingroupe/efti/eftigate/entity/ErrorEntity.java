package com.ingroupe.efti.eftigate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "error", catalog = "efti")
@Getter
@Setter
public class ErrorEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    
    @Column(name = "requestid")
    private int requestId;
    
    @Column(name = "errorcode")
    private String errorCode;
    
    @Column(name = "errordescription")
    private String errorDescription;
}
