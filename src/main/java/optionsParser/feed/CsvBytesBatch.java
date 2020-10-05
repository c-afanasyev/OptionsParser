package optionsParser.feed;

import lombok.Data;

@Data
public class CsvBytesBatch {
    
    private byte[] batch;
    private int currentBatchSize;
    private boolean isTerminationSignal = false;
    
    public CsvBytesBatch(int batchMaxSize) {
        batch = new byte[batchMaxSize];
    }

    public CsvBytesBatch(boolean isTerminationSignal) {
        this.isTerminationSignal = isTerminationSignal;
    }
}
