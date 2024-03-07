package com.ingroupe.efti.eftigate.entity;

import com.ingroupe.efti.commons.model.AbstractModel;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "request", catalog = "efti")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Convert(attributeName = "entityAttrName", converter = JsonBinaryType.class)
public class RequestEntity extends AbstractModel implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "edeliverymessageid")
    private String edeliveryMessageId;
    
    @Column(name = "retry")
    private Integer retry;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reponsedata")
    private Object reponseData;
    
    @Column(name = "nextretrydate")
    private LocalDateTime nextRetryDate;
    
    @Column(name = "gateurldest")
    private String gateUrlDest;

    @ManyToOne
    @JoinColumn(name = "control")
    ControlEntity control;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "error", referencedColumnName = "id")
    ErrorEntity error;
}
