package optionsParser;

import optionsParser.resultsSaving.CsvWriter;

public class App {
    
    public static void main(String[] args) {
        var parser = new OptionsMinMaxPrices();
        var minMaxPrices = parser.getMinAndMaxPricesInContracts();
        
        CsvWriter writer = new CsvWriter();
        writer.writeCsv(minMaxPrices);
    }
}
