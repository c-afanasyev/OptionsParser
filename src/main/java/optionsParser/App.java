package optionsParser;

import optionsParser.resultsSaving.CsvWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    
    public static void main(String[] args) {
        Path folderWithArchives = Paths.get(System.getProperty("user.home") + "/OptionsData/");
        var parser = new OptionsMinMaxPricesFinder();
        var minMaxPrices = parser.getMinAndMaxPricesInContracts(folderWithArchives);
        System.out.println("size " + minMaxPrices.size());
        CsvWriter writer = new CsvWriter();
        //writer.writeCsv(minMaxPrices);
    }
}
