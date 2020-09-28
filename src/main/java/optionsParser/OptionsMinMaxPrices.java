package optionsParser;

import lombok.SneakyThrows;
import optionsParser.feed.OptionsCsvProducer;
import optionsParser.multithreadSettings.InterThreadCommunication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OptionsMinMaxPrices {
    private static InterThreadCommunication communication = new InterThreadCommunication();
    
    @SneakyThrows
    public ConcurrentMap<String,int[]> getMinAndMaxPricesInContracts() {
        long startTime = System.currentTimeMillis();

        ThreadPoolExecutor fileReaders = communication.getFileReaders();
        ThreadPoolExecutor ticksParsers = communication.getTicksParsers();
        var resultsMap = communication.getCombinedResults();

        List<OptionsCsvProducer> readingTasks = getReadingTasks();
        fileReaders.invokeAll(readingTasks); // wait for executor finish
        
        communication.shutDownExecutors();

        long endTime = System.currentTimeMillis();

        System.out.println("Total running time: " + (endTime-startTime)/1000 + " seconds.");
        System.out.println("Total results size: " + resultsMap.size());
        
        return resultsMap;
    }

    @SneakyThrows
    private List<OptionsCsvProducer> getReadingTasks() {
        Path folderWithArchives = Paths.get(System.getProperty("user.home") + "/OptionsData/");
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
