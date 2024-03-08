package com.ingroupe.efti.eftigate.batch;

import com.ingroupe.efti.eftigate.service.AbstractServiceTest;
import com.ingroupe.efti.eftigate.service.ControlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

class ControlBatchTest extends AbstractServiceTest {
    AutoCloseable openMocks;
    @Mock
    private ControlService controlService;

    @Test
    void work_success()
    {
        openMocks = openMocks(this);
        ControlBatch controlBatch = new ControlBatch(controlService);
        controlBatch.updatePendingControls();
        verify(controlService, times(1)).updatePendingControls();
    }
}
