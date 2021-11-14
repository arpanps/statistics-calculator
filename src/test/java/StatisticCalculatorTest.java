import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.mockito.Mockito.*;

public class StatisticCalculatorTest {
    @InjectMocks
    StatisticCalculator statisticCalculator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEvent() throws Exception {
        mockEventValues();
    }

    @Test
    public void testMean() throws Exception {
        mockEventValues();
        float result = statisticCalculator.mean();
        Assert.assertEquals(3.33f, result, 0.01);
    }

    @Test
    public void testMeanLastNMinutes() throws Exception {
        mockEventValues();
        float result = statisticCalculator.mean(0);
        Assert.assertEquals(3.33f, result, 0.01);
    }


    @Test
    public void testMeanLastNMinutesWithBulkEvent() throws Exception {
        mockBulkEventValues();
        float result = statisticCalculator.mean(10);
        Assert.assertEquals(0f, result, 0);
    }


    @Test
    public void testVariance() throws Exception {
        mockEventValues();
        float result = statisticCalculator.variance();
        Assert.assertEquals(8.666f, result, 0.01);
    }

    @Test
    public void testMinimum() throws Exception {
        mockEventValues();
        int result = statisticCalculator.minimum();
        Assert.assertEquals(0, result, 0);
    }

    @Test
    public void testMaximum() throws Exception {
        mockEventValues();
        int result = statisticCalculator.maximum();
        Assert.assertEquals(5.0, result, 0);
    }


    private void mockEventValues() {
        statisticCalculator.event(4);
        statisticCalculator.event(5);
        statisticCalculator.event(1);
    }

    private void mockBulkEventValues() {
        Map<Long, List<Integer>> statsByMints = Mockito.mock(HashMap.class);

        Calendar currentTimeNow = Calendar.getInstance();
        for (int i = 1; i < 12; i++) {
            currentTimeNow.add(Calendar.MINUTE, -1);
            Date date = currentTimeNow.getTime();
            Long time = toMinutes(date);
            List<Integer> values = new ArrayList<>();
            for(int j = 0; j<i; j++){
                values.add(j);
            }
            Mockito.when(statsByMints.getOrDefault(time, Collections.EMPTY_LIST)).thenReturn(values);
        }
    }

    //@Test
    public void testMins() {
        System.out.println(getLastNMints(8));
    }

    private Set<Long> getLastNMints(int lastNMinutes) {
        Calendar currentTimeNow = Calendar.getInstance();
        Set<Long> lastNMinutesList = new TreeSet<>(Collections.reverseOrder());
        for (int i = 1; i <= lastNMinutes; i++) {
            currentTimeNow.add(Calendar.MINUTE, -1);
            Date tenMinsFromNow = currentTimeNow.getTime();
            lastNMinutesList.add(toMinutes(tenMinsFromNow));
        }
        return lastNMinutesList;
    }

    private Long toMinutes(Date date) {
        return date.toInstant().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
    }
}