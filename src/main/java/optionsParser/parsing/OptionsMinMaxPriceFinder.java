package optionsParser.parsing;

import optionsParser.multithreadSettings.InterThreadCommunication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import static optionsParser.parsing.OptionDataFormatParser.*;


public class OptionsMinMaxPriceFinder implements Runnable {
    public static final int MIN = 0;
    public static final int MAX = 1;

    private final BlockingQueue<Runnable> ticksParsersQueue;
    private final ConcurrentMap<String,int[]> combinedResults;
    private final List<String> parsedLines;
    private final int CONSUMERS_MAX_QUEUE;
    private final Object lock;
    
    private final OptionDataFormatParser parser = new OptionDataFormatParser();
    private final Map<String,int[]> minMaxLocalBufferMap = new HashMap<>();
    
    public OptionsMinMaxPriceFinder(List<String> parsedLines, InterThreadCommunication communication) 
    {
        this.ticksParsersQueue = communication.getTicksParsers().getQueue();
        this.combinedResults = communication.getCombinedResults();
        this.CONSUMERS_MAX_QUEUE = communication.getBackpressureMaxQueueLength();
        this.parsedLines = parsedLines;
        this.lock = communication.getBackpressureLock();
    }

    @Override
    public void run() {
        parsedLines.forEach(line -> putLineIntoLocalBufferMap(parser.parseLine(line)));
        mergeLocalBufferWithCombinedResults();
        if (ticksParsersQueue.size() < CONSUMERS_MAX_QUEUE) synchronized (lock) { lock.notifyAll(); } // release idle producers
    }

    private void putLineIntoLocalBufferMap(String[] parsedLine) {
        StringBuilder sb = new StringBuilder(40);
        sb.append(parsedLine[TICKER]).append(',')
                .append(parsedLine[TYPE]).append(',')
                .append(parsedLine[EXPIRATION]).append(',')
                .append(parsedLine[STRIKE]);
        
        String line = sb.toString();

        int premiumPrice = Integer.parseInt(parsedLine[PREMIUM]);
        int[] minAndMaxValues = minMaxLocalBufferMap.get(line);
        
        if (minAndMaxValues != null) {
            minAndMaxValues[MIN] = Math.min(minAndMaxValues[MIN], premiumPrice);
            minAndMaxValues[MAX] = Math.max(minAndMaxValues[MAX], premiumPrice);
        } else {
            minMaxLocalBufferMap.put(line, new int[] {premiumPrice, premiumPrice});
        }
    }
    
    private void mergeLocalBufferWithCombinedResults() {
        minMaxLocalBufferMap.forEach((key1,val1) -> combinedResults.compute(key1, (key2, val2) -> {
            if (val2 == null) {
                return val1;
            } else {
                val2[MIN]  = Math.min(val1[MIN], val2[MIN]);
                val2[MAX]  = Math.max(val1[MAX], val2[MAX]);
                return val2;
            }
        }));
    }
}
