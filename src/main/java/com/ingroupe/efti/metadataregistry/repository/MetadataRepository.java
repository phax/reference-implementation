package com.ingroupe.efti.metadataregistry.repository;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface MetadataRepository extends JpaRepository<MetadataEntity, Long>, JpaSpecificationExecutor<MetadataEntity> {

    String VEHICLE_COUNTRY = "vehicleCountry";
    String TRANSPORT_MODE = "transportMode";
    String IS_DANGEROUS_GOODS = "isDangerousGoods";
    String TRANSPORT_VEHICLES = "transportVehicles";
    String VEHICLE_ID = "vehicleId";

    @Query(value = "SELECT m FROM MetadataEntity m where m.eFTIGateUrl = :gate and m.eFTIDataUuid = :uuid and m.eFTIPlatformUrl = :platform")
    Optional<MetadataEntity> findByUil(final String gate, final String uuid, final String platform);

    default List<MetadataEntity> searchByCriteria(final MetadataRequestDto request) {
        return this.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.join(TRANSPORT_VEHICLES).get(VEHICLE_ID), request.getVehicleID()));
            if(request.getIsDangerousGoods() != null) {
                predicates.add(cb.equal(root.get(IS_DANGEROUS_GOODS), request.getIsDangerousGoods()));
            }
            if(request.getTransportMode() != null) {
                predicates.add(cb.equal(root.get(TRANSPORT_MODE), request.getTransportMode()));
            }
            if(request.getVehicleCountry() != null) {
                predicates.add(cb.equal(root.get(VEHICLE_COUNTRY), request.getVehicleCountry()));
            }
            return cb.and(predicates.toArray(new Predicate[] {}));
        });
    }
}
