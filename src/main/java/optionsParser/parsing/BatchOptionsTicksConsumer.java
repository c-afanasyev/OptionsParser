package optionsParser.parsing;

import optionsParser.feed.CsvBytesBatch;
import optionsParser.multithreadSettings.InterThreadCommunication;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;


public class BatchOptionsTicksConsumer implements Runnable {
    public static final byte MIN = 0, MAX = 1;

    private final LinkedBlockingQueue<CsvBytesBatch> filledBatches;
    private final LinkedBlockingQueue<CsvBytesBatch> emptyBatches;
    
    private final ConcurrentMap<OptionByteBuffer,int[]> combinedResults;

    private final OptionDataFormatParser linesParser = new OptionDataFormatParser();
    
    public BatchOptionsTicksConsumer(InterThreadCommunication communication) {
        this.filledBatches = communication.getFilledBatches();
        this.emptyBatches = communication.getEmptyBatches();
        this.combinedResults = communication.getCombinedResults();
    }

    @Override
    public void run() {
        CsvBytesBatch batch = getNextBatch();
        while (!batch.isTerminationSignal()) {
            try {
                linesParser.parseLinesInBatch(batch);
                putBatchBackForReuse(batch);
                batch = getNextBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        mergeLocalBufferWithCombinedResults(linesParser.getLocalBufferMap());
    }

    private CsvBytesBatch getNextBatch() {
        CsvBytesBatch batch = null;
        while (batch == null) {
            try {
                batch = filledBatches.take();
            } catch (InterruptedException ignore) {}
        }
        return batch;
    }

    private void putBatchBackForReuse(CsvBytesBatch batch) {
        try { 
            emptyBatches.put(batch); //reuse already allocated in memory array
        } catch (InterruptedException ignore) {}
    }
    
    private void mergeLocalBufferWithCombinedResults(Map<OptionByteBuffer,int[]> localMapBufferMap) {
        localMapBufferMap.forEach((key1,val1) -> combinedResults.compute(key1, (key2, val2) -> {
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
