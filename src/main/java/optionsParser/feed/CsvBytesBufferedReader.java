package optionsParser.feed;

import lombok.SneakyThrows;

import java.io.FileInputStream;

public class CsvBytesBufferedReader implements CsvBufferedReader {
    private static final byte NEW_LINE = 10;
    
    private final int bufferLength;
    private final byte[] buffer;
    private int off = 0;
    
    private final FileInputStream inputStream;
    
    @SneakyThrows
    public CsvBytesBufferedReader(FileInputStream inputStream, int bufferLength) {
        this.inputStream = inputStream;
        this.bufferLength = bufferLength;
        this.buffer = new byte[bufferLength];
    }

    @Override
    @SneakyThrows
    public int read(byte[] b) {
        if (off > 0) {
            System.arraycopy(buffer, 0, b, 0, off);
        }
        int totalBytesRed = inputStream.read(b, off, bufferLength-off);
        off = 0;
        for (int i = b.length-1; b[i] != NEW_LINE ; i--){ off++; } // count length of truncated tick
        for (int i = b.length-off, j = 0; i < b.length; i++, j++) {
            buffer[j] = b[i]; // put truncated tick into buffer
        }

        return totalBytesRed - off;
    }

    @Override
    @SneakyThrows
    public long skip(long bytesToBeSkipped) {
        return inputStream.skip(bytesToBeSkipped);
    }
    
    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
