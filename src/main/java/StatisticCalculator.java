import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class StatisticCalculator implements Statistic {
    private static final StatisticCalculator _instance = new StatisticCalculator();

    public static StatisticCalculator getInstance() {
        return _instance;
    }


    class Stats {
        private float mean;
        private float variance;
        private int min;
        private int max;

        public Stats() {
        }

        public Stats(float mean, float variance, int min, int max) {
            this.mean = mean;
            this.variance = variance;
            this.min = min;
            this.max = max;
        }
    }

    private Map<Long, List<Integer>> statsByMints = new ConcurrentHashMap<>();
    private LongAdder adder = new LongAdder();
    private volatile Stats currentStats = new Stats(0.0f, 0.0f, 0, 0);

    @Override
    public synchronized void event(int value) {
        adder.increment();
        Stats oldStats = currentStats;

        Stats newStats = new Stats();

        // Mean and Variance calculator
        // Calculate variance from a stream - https://math.stackexchange.com/questions/20593/calculate-variance-from-a-stream-of-sample-values

        // Calculating mean
        newStats.mean = oldStats.mean + ((value - oldStats.mean ) / adder.floatValue());

        // Calculating variance
        newStats.variance = oldStats.variance + (value - oldStats.mean) *
                (value- newStats.mean);

        // Finding Min
        newStats.min = Math.min(oldStats.min, value);

        // Finding Max
        newStats.max = Math.max(oldStats.max, value);

        long mins = toMinutes(new Date());
        statsByMints.putIfAbsent(mins, new ArrayList<>());

        // Assigning latest values
        currentStats = newStats;
        statsByMints.get(mins).add(value);
    }

    @Override
    public float mean() {
        return currentStats.mean;
    }

    @Override
    public float mean(int lastNMinutes) {
        if(lastNMinutes <0){
            throw new IllegalArgumentException("Please provide positive integer");
        }
        Set<Long> lastNMintsInLong = getLastNMints(lastNMinutes);
        List<Integer> totalValues = new ArrayList<>();
        for(Long time : lastNMintsInLong){
            totalValues.addAll(statsByMints.getOrDefault(time, Collections.EMPTY_LIST));
        }

        return mean(totalValues);
    }

    private float mean(List<Integer> totalValues){
        if(totalValues.isEmpty()){
            return 0.0f;
        }
        Long sum = 0L;
        for(Integer i : totalValues){
            sum += i;
        }
        return sum/(float)totalValues.size();
    }

    private Set<Long> getLastNMints(int lastNMinutes){
        Calendar currentTimeNow = Calendar.getInstance();
        Set<Long> lastNMinutesList = new TreeSet<>(Collections.reverseOrder());
        lastNMinutesList.add(toMinutes(currentTimeNow.getTime()));
        for(int i = 1; i<lastNMinutes; i++){
            currentTimeNow.add(Calendar.MINUTE, -1);
            lastNMinutesList.add(toMinutes(currentTimeNow.getTime()));
        }
        return lastNMinutesList;
    }
    private Long toMinutes(Date date){
        return date.toInstant().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
    }

    @Override
    public float variance() {
        return currentStats.variance;
    }

    @Override
    public int minimum() {
        return currentStats.min;
    }

    @Override
    public int maximum() {
        return currentStats.max;
    }
}
