package optionsParser.multithreadSettings;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import optionsParser.feed.CsvBytesBatch;
import optionsParser.parsing.BatchOptionsTicksConsumer;
import optionsParser.parsing.OptionByteBuffer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class InterThreadCommunication {
    private int readingWorkersQty = 2;
    private int parsingWorkersQty = 10;
    private int backpressureMaxQueueLength = 50;
    private int ticksParsersMaxBatchSize = 8192;
    
    private ConcurrentMap<OptionByteBuffer,int[]> combinedResults;
    
    private ExecutorService fileReaders;
    private List<Thread> ticksParsers;

    private final LinkedBlockingQueue<CsvBytesBatch> emptyBatches;
    private final LinkedBlockingQueue<CsvBytesBatch> filledBatches;
    private final List<CsvBytesBatch> terminationTasks;

    public InterThreadCommunication() {
        combinedResults = new ConcurrentHashMap<>(8192, 0.75f, parsingWorkersQty);
        fileReaders = Executors.newFixedThreadPool(readingWorkersQty);
        emptyBatches = initEmptyBatchesForFileReaders();
        filledBatches = new LinkedBlockingQueue<>(backpressureMaxQueueLength);
        terminationTasks = initTerminatingTasks();
        ticksParsers = initTicksParsingWorkers();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutDownExecutors));
    }

    private List<Thread> initTicksParsingWorkers() {
        return IntStream.range(0, parsingWorkersQty)
                .mapToObj(i -> new Thread(new BatchOptionsTicksConsumer(this)))
                .peek(thread -> thread.setUncaughtExceptionHandler((currThread,throwable) -> {
                    System.out.println(throwable.getMessage());
                    throwable.printStackTrace();
                }))
                .collect(Collectors.toList());
    }

    private LinkedBlockingQueue<CsvBytesBatch> initEmptyBatchesForFileReaders() {
        Supplier<LinkedBlockingQueue<CsvBytesBatch>> queueWithMaxLength = () -> new LinkedBlockingQueue<>(backpressureMaxQueueLength);
        return IntStream.range(0, backpressureMaxQueueLength)
                .mapToObj(i -> new CsvBytesBatch(ticksParsersMaxBatchSize))
                .collect(Collectors.toCollection(queueWithMaxLength));
    }

    /**
     *  isTerminationSignal flag says to finish a Worker Thread.
     */
    private List<CsvBytesBatch> initTerminatingTasks() {
        return IntStream.range(0,parsingWorkersQty).mapToObj(i -> new CsvBytesBatch(true)).collect(Collectors.toList());
    }

    public void shutDownExecutors(){
        shutDownExecutor(fileReaders);
        terminateParsingWorkers();
    }
    
    private void shutDownExecutor(ExecutorService executor) {
        executor.shutdown();
        while (!executor.isShutdown()) {
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignore){}
        }
    }

    public void terminateParsingWorkers() {
        filledBatches.addAll(terminationTasks);
        for (Thread worker : ticksParsers) {
            while (worker.isAlive()) {
                try {
                    worker.join();
                } catch (InterruptedException ignore) {}
            }
        }
    }
}

