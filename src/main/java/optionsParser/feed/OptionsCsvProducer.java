package optionsParser.feed;

import lombok.SneakyThrows;
import optionsParser.multithreadSettings.InterThreadCommunication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public final class OptionsCsvProducer implements Callable<Object> {
    
    private final Path path;
    private final InterThreadCommunication communication;
    private final LinkedBlockingQueue<CsvBytesBatch> emptyBatches;
    private final LinkedBlockingQueue<CsvBytesBatch> filledBatches;
    private final int batchSize;
    private final byte NULL = 0;
    
    @SneakyThrows
    public OptionsCsvProducer(Path path, InterThreadCommunication communication) {
        if (!path.toFile().exists()) throw new FileNotFoundException(path.toString()); // fast fail
        this.path = path;
        this.communication = communication;
        this.emptyBatches = communication.getEmptyBatches();
        this.filledBatches = communication.getFilledBatches();
        this.batchSize = communication.getTicksParsersMaxBatchSize();
    }
    
    @Override
    public Object call() {
        produceTicksToParsers();
        return path.toString();
    }

    private void produceTicksToParsers() {
        try (CsvBufferedReader br = new CsvBytesBufferedReader(new FileInputStream(path.toString()), batchSize)) {
            CsvBytesBatch batch = getEmptyBatch();
            int charsQtyRed;
            br.skip(84); // skip schema on the first line
            while ((charsQtyRed = br.read(batch.getBatch())) > 0) {
                putFilledBatchToParsingQueue(batch, charsQtyRed);
                batch = getEmptyBatch();
                //if (batch.getBatch()[batchSize-1] == NULL) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private CsvBytesBatch getEmptyBatch() {
        CsvBytesBatch emptyBatch = null;
        while (emptyBatch == null) {
            try {
                emptyBatch = emptyBatches.take();
            } catch (InterruptedException ignore){}
        }
        return emptyBatch;
    }
    
    private void putFilledBatchToParsingQueue(CsvBytesBatch filledBatch, int currentBatchSize) {
        filledBatch.setCurrentBatchSize(currentBatchSize);
        try {
            filledBatches.put(filledBatch);
        } catch (InterruptedException ignore) {}
    }
}
