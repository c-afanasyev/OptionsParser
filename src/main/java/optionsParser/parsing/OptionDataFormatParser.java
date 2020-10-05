package optionsParser.parsing;

import lombok.Getter;
import optionsParser.feed.CsvBytesBatch;

import java.util.HashMap;
import java.util.Map;

import static optionsParser.parsing.BytesArrToIntPriceConverter.convertBytesArrToInt;

@Getter
public class OptionDataFormatParser {
    
    private static final byte NEW_LINE = 10, SPACE = 32, COMMA = 44;
    private static final byte MIN = 0, MAX = 1;
    
    private final byte[] currentTickContactSpec = new byte[25]; //max expected line length is 25 characters
    private int charsAlreadyIn = 0; // chars already put inside the 'currentTick' array above
    private int currentTickPrice;

    
    private Map<OptionByteBuffer,int[]> localBufferMap = new HashMap<>(8192);
    private OptionByteBuffer tempBuffer = new OptionByteBuffer();
    
    public OptionDataFormatParser() {
        currentTickContactSpec[24] = SPACE;
    }

    /*     
     * Expected data format:
     * 
     *  Timestamp,Ticker,Type,Side,Info,PutCall,Expiration,Strike,Quantity,Premium,Exchange
     *  06:30:18.527,ACWI,O, , ,C,20200117,770000,1336,3200,Z
     *  06:30:18.528,ACWI,O, , ,P,20200117,610000,35,1200,P
     */
    public void parseLinesInBatch(CsvBytesBatch batch) {
        byte[] chars = batch.getBatch();
        int batchSize = batch.getCurrentBatchSize();
        
        int i = 13; // skip Timestamp
        
        while (i < batchSize) {
            i = putValuesBeforeNextComma(chars, currentTickContactSpec, i, true);  // put Ticker name
            i = skipValuesBeforeComma(chars, i, 3);                             // skip Type, Side and Info
            i = putValuesBeforeNextComma(chars, currentTickContactSpec, i, true);  // put option's Type (Put or Call)
            i = putValuesBeforeNextComma(chars, currentTickContactSpec, i, true);  // put Expiration date
            i = putValuesBeforeNextComma(chars, currentTickContactSpec, i, false); // put Strike
            if (charsAlreadyIn < 24) markRemainingElementsAsSpaceChar(currentTickContactSpec, charsAlreadyIn);
            i = skipValuesBeforeComma(chars, i, 1);             // skip Quantity
            i = resolvePrice(chars, i);                                      // put Premium price
            i = goToNextLine(chars, i);
            putLineIntoLocalBufferMap(currentTickContactSpec, currentTickPrice);
            charsAlreadyIn = 0;
        }
    }
    
    private int putValuesBeforeNextComma(byte[] from, byte[] to, int readFromIndex, boolean addComma) {
        do {
            if (charsAlreadyIn > 24) {
                System.out.println("more than 24");
            }
            to[charsAlreadyIn++] = from[readFromIndex];
        } while (from[++readFromIndex] != COMMA);
        if (addComma) to[charsAlreadyIn++] = COMMA;
        return ++readFromIndex;
    }

    private int skipValuesBeforeComma(byte[] chars, int readFromIndex, int howManyTimes) {
        do { 
            if (chars[readFromIndex++] == COMMA) howManyTimes--;
        } while (howManyTimes > 0);
        return readFromIndex;
    }

    private void markRemainingElementsAsSpaceChar(byte[] singleTick, int charsAlreadyIn) {
        for (int i = 23; i >= charsAlreadyIn; i--) singleTick[i] = SPACE;
    }

    private int resolvePrice(byte[] chars, int readFromIndex) {
        byte places = 0;
        while (chars[readFromIndex++] != COMMA) places++; // count how many places takes the Premium price
        try {
            currentTickPrice = convertBytesArrToInt(chars, readFromIndex-2, places);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return readFromIndex;
    }

    private int goToNextLine(byte[] chars, int readFromIndex) {
        while (chars[readFromIndex++] != NEW_LINE) {}
        return readFromIndex + 13; // skip Timestamp 
    }
    

    private void putLineIntoLocalBufferMap(byte[] singleTickContactSpec, int singleTickPrice) {
        tempBuffer.setSingleTick(singleTickContactSpec);
        
        int[] minAndMaxValues = localBufferMap.get(tempBuffer);
        
        if (minAndMaxValues != null) {
            minAndMaxValues[MIN] = Math.min(minAndMaxValues[MIN], singleTickPrice);
            minAndMaxValues[MAX] = Math.max(minAndMaxValues[MAX], singleTickPrice);
        } else {
            localBufferMap.put(new OptionByteBuffer(tempBuffer), new int[] {singleTickPrice, singleTickPrice});
        }
    }
}
