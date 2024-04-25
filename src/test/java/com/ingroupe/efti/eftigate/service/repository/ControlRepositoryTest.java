package com.ingroupe.efti.eftigate.service.repository;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import com.ingroupe.efti.eftigate.entity.RequestEntity;
import com.ingroupe.efti.eftigate.repository.ControlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= {ControlRepository.class})
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@EnableJpaRepositories(basePackages = {"com.ingroupe.efti.eftigate.repository"})
@EntityScan("com.ingroupe.efti.eftigate.entity")
class ControlRepositoryTest {

    @Autowired
    private ControlRepository controlRepository;

    @Test
    void shouldFindControlByCriteria(){
        //Arrange
        RequestEntity firstRequest = new RequestEntity();
        firstRequest.setStatus("IN_PROGRESS");
        RequestEntity secondRequest = new RequestEntity();
        secondRequest.setStatus("RECEIVED");
        ControlEntity firstControl = ControlEntity.builder().requestUuid("67fe38bd-6bf7-4b06-b20e-206264bd639c").status("PENDING").requests(List.of(firstRequest)).build();
        firstRequest.setControl(firstControl);
        ControlEntity firstSavedControl = controlRepository.save(firstControl);
        ControlEntity secondControl = ControlEntity.builder().requestUuid("23fe38bd-6bf7-4b06-b20e-206264bd66c").status("ERROR").requests(List.of(secondRequest)).build();
        secondRequest.setControl(secondControl);
        ControlEntity secondSavedControl = controlRepository.save(secondControl);

        //Act
        List<ControlEntity> controls = controlRepository.findByCriteria("67fe38bd-6bf7-4b06-b20e-206264bd639c", "IN_PROGRESS");

        //Assert
        assertThat(controls).containsExactlyInAnyOrder(firstSavedControl);
    }

}
