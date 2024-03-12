package com.ingroupe.efti.eftigate.batch;

import com.ingroupe.efti.eftigate.service.AbstractServiceTest;
import com.ingroupe.efti.eftigate.service.ControlService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

class ControlBatchTest extends AbstractServiceTest {
    AutoCloseable openMocks;
    @Mock
    private ControlService controlService;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);
     }


    @Test
    void work_success() throws Exception {
        openMocks = openMocks(this);
        ControlBatch controlBatch = new ControlBatch(controlService);
        controlBatch.updatePendingControls();
        verify(controlService, times(1)).updatePendingControls();
        openMocks.close();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
}
