package com.ingroupe.efti.commons.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class AbstractModel {

    /**
     * date that the data have been created
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    /**
     * date that the data have been modified
     */
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
