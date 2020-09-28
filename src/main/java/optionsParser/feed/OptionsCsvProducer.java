package optionsParser.feed;

import lombok.SneakyThrows;
import optionsParser.multithreadSettings.InterThreadCommunication;
import optionsParser.parsing.OptionsMinMaxPriceFinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

public final class OptionsCsvProducer implements Callable<Object> {
    
    private final Path path;
    private final InterThreadCommunication communication;
    private final ThreadPoolExecutor consumersPool;
    private final BlockingQueue<Runnable> consumersPoolQueue;
    private final Object lock;
    private final int BATCH_SIZE;
    private final int CONSUMERS_MAX_QUEUE;
    
    @SneakyThrows
    public OptionsCsvProducer(Path path, InterThreadCommunication communication) {
        if (!path.toFile().exists()) throw new FileNotFoundException(path.toString()); // fast fail
        this.path = path;
        this.communication = communication;
        this.consumersPool = communication.getTicksParsers();
        this.consumersPoolQueue = consumersPool.getQueue();
        this.lock = communication.getBackpressureLock();
        this.BATCH_SIZE = communication.getTicksParsersMaxBatchSize();
        this.CONSUMERS_MAX_QUEUE = communication.getBackpressureMaxQueueLength();
    }
    
    @Override
    public Object call() {
        produceTicksToParsers();
        return path.toString();
    }

    private void produceTicksToParsers() {
/*      var bis = new BufferedInputStream(new FileInputStream(path.toString()));
        var uncompressedInput = new CompressorStreamFactory().createCompressorInputStream(bis);
        br =  new BufferedReader(new InputStreamReader(uncompressedInput));*/
        
        try (var br = new BufferedReader(new FileReader(path.toString()))) {
            br.readLine(); // skip first line with metadata
            String line = br.readLine();
            
            while (line != null) {
                ArrayList<String> ticksBuffer = new ArrayList<>(BATCH_SIZE);
                int i;
                for (i = 0; i < BATCH_SIZE && line != null; i++) {
                    ticksBuffer.add(line);
                    line = br.readLine();
                }
                if(i < BATCH_SIZE) ticksBuffer.trimToSize();
                submitToConsumers(ticksBuffer); // send ticks to parsers in batches
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void submitToConsumers(ArrayList<String> ticks) {
        while (consumersPoolQueue.size() > CONSUMERS_MAX_QUEUE) { // backpressure
            try {
                synchronized (lock) { lock.wait(); }
            } catch (InterruptedException ignore) {}
        }
        consumersPool.submit(new OptionsMinMaxPriceFinder(ticks, communication));
    }
}
