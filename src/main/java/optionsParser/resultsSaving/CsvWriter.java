package optionsParser.resultsSaving;

import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import static optionsParser.parsing.BatchOptionsTicksConsumer.*;

public class CsvWriter {
    @SneakyThrows
    public void writeCsv(Map<String,int[]> data) {
        String target = System.getProperty("user.home") + "/OptionsMinMaxPricesFinder/prices.csv";
        File file = new File(target);
        file.getParentFile().mkdirs();
        file.createNewFile();
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(target));
        bw.write("Ticker,PutCall,Expiration,Strike,MinPrice,MaxPrice"); // indicate data format on first line
        bw.newLine();
        
        for (Map.Entry<String,int[]> entry : data.entrySet()) {
            String key = entry.getKey();
            int[] val = entry.getValue();
            StringBuilder line = new StringBuilder(60);
            line.append(key).append(',').append(val[MIN]).append(',').append(val[MAX]);
            bw.write(line.toString());
            bw.newLine();
        }
        
        bw.write("Total pricing lines qty: " + data.size());
        bw.newLine();
        
        bw.flush();
    }
}
