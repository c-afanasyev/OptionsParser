package optionsParser;

import lombok.Getter;
import lombok.SneakyThrows;
import optionsParser.feed.OptionsCsvProducer;
import optionsParser.multithreadSettings.InterThreadCommunication;
import optionsParser.parsing.OptionByteBuffer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OptionsMinMaxPricesFinder {
    @Getter
    private final InterThreadCommunication communication = new InterThreadCommunication();
    
    @SneakyThrows
    public ConcurrentMap<OptionByteBuffer,int[]> getMinAndMaxPricesInContracts(Path folderWithArchives) {
        long startTime = System.currentTimeMillis();
        try {
            List<OptionsCsvProducer> readingTasks = getReadingTasks(folderWithArchives);
            communication.getTicksParsers().forEach(Thread::start); // start ticks parsers
            System.gc(); // invoke GC at before the main logic start
            communication.getFileReaders().invokeAll(readingTasks); // wait for executor finish
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        communication.shutDownExecutors(); //shutdown readers ExecutorService and parsing Workers gracefully
        long endTime = System.currentTimeMillis();

        System.out.println("Total running time: " + (endTime-startTime)/1000 + " seconds.");
        
        return communication.getCombinedResults();
    }

    @SneakyThrows
    private List<OptionsCsvProducer> getReadingTasks(Path folderWithArchives) {
        Predicate<Path> isCsv = path -> path.toString().endsWith(".csv");
        Comparator<Path> fileSizeDesc = (file1,file2) -> (int) (file1.toFile().length() - file2.toFile().length());

        return Files.walk(folderWithArchives)
                .filter(Files::isRegularFile)
                .filter(isCsv)
                .collect(Collectors.toList())
                .stream()
                .sorted(fileSizeDesc)
                .map(path -> new OptionsCsvProducer(path, communication))
                .collect(Collectors.toList());
    }
}
