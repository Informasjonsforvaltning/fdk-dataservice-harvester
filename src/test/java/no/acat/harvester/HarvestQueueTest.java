package no.acat.harvester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class HarvestQueueTest {
    private static final Logger logger = LoggerFactory.getLogger(HarvestExecutor.class);

    private HarvestQueue queue = new HarvestQueue();
    @Mock
    private ApiHarvester harvester;

    @InjectMocks
    private HarvestExecutor executor = new HarvestExecutor(harvester, queue);

    @BeforeEach
    void resetMocks() {
        Mockito.reset(
            harvester
        );
    }

    @Test
    public void testExecutionLogic() throws Throwable {
        doNothing().when(harvester).harvestAll();
        executor.harvestLoop();

        String ht = HarvestExecutor.HARVEST_ALL;

        queue.addTask(ht);

        queue.addTask(ht); // should be blocked by queue since it already has the same task registered

        Thread.sleep(1100);

        verify(harvester, times(1)).harvestAll();

        queue.addTask(ht);

        Thread.sleep(1100);

        verify(harvester, times(2)).harvestAll();

        String tsk = queue.poll();

        assertEquals("queue should have no elements", tsk, null);

    }

    @Test
    public void harvestAllFails() throws Throwable {
        executor.harvestLoop();

        queue.addTask(HarvestExecutor.HARVEST_ALL);

        Thread.sleep(1300); //Timer is every 1000 ms. Added 300 ms to make the test work on slow or loaded machines.

        assertNull("queue should be empty", queue.poll());

    }

}
