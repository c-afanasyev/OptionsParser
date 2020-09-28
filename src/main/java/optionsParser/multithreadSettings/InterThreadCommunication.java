package optionsParser.multithreadSettings;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public class InterThreadCommunication {
    private ConcurrentMap<String,int[]> combinedResults = new ConcurrentHashMap<>(8192, 0.75f, 10);
    private ThreadPoolExecutor fileReaders = (ThreadPoolExecutor)Executors.newFixedThreadPool(2);
    private ThreadPoolExecutor ticksParsers = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
    private int ticksParsersMaxBatchSize = 25_000;
    private int backpressureMaxQueueLength = 50;
    private Object backpressureLock = new Object();

    public InterThreadCommunication() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutDownExecutors));
    }

    public void shutDownExecutors(){
        shutDownExecutor(fileReaders);
        shutDownExecutor(ticksParsers);
    }
    
    private void shutDownExecutor(ThreadPoolExecutor executor) {
        executor.shutdown();
        while (!executor.isShutdown()) {
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignore){}
        }
    }
}

