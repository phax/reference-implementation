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
@Table(name = "authority", catalog = "efti")
@Getter
@Setter
public class AuthorityEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    
    @Column(name = "controlid")
    private int controlId;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "physicaladdress")
    private String physicalAddress;
    
    @Column(name = "isemergencyservice")
    private boolean isEmergencyService;
    
    @Column(name = "authorityname")
    private String authorityName;
    
    @Column(name = "nationaluniqueidentifier")
    private String nationalUniqueIdentifier;
}
