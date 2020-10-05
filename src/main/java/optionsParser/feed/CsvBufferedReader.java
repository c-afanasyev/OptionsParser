package optionsParser.feed;

public interface CsvBufferedReader extends AutoCloseable {
    int read(byte[] b);
    long skip(long bytesToBeSkipped);
}
