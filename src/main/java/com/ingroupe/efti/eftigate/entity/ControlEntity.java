package com.ingroupe.efti.eftigate.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "control", catalog = "efti")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private SearchParameter transportMetadata;

    @Column(name = "fromgateurl")
    private String fromGateUrl;

    @OneToMany(mappedBy = "control", fetch = FetchType.EAGER)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<RequestEntity> requests;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "authority", referencedColumnName = "id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private AuthorityEntity authority;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "error", referencedColumnName = "id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private ErrorEntity error;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadatas")
    private MetadataResults metadataResults;
}
