package optionsParser.parsing;

public class OptionDataFormatParser {
    static final int TICKER = 0;
    static final int TYPE = 1;
    static final int EXPIRATION = 2;
    static final int STRIKE = 3;
    static final int PREMIUM = 4;

    /*     
     * Expected data format:
     * 
     *  Timestamp,Ticker,Type,Side,Info,PutCall,Expiration,Strike,Quantity,Premium,Exchange
     *  06:30:18.527,ACWI,O, , ,C,20200117,770000,1336,3200,Z
     *  06:30:18.528,ACWI,O, , ,P,20200117,610000,35,1200,P
     */
    public String[] parseLine(String line) {
        String[] result = new String[5];
        int i = 13; //start from index = 13 to skip timestamp
        i = getValueBeforeNextComma(line, i, TICKER, result);
        i = skipValueBeforeComma(line, i, 3);
        i = getValueBeforeNextComma(line, i, TYPE, result);
        i = getValueBeforeNextComma(line, i, EXPIRATION, result);
        i = getValueBeforeNextComma(line, i, STRIKE, result);
        i = skipValueBeforeComma(line, i, 1);
        getValueBeforeNextComma(line, i, PREMIUM, result);

        return result;
    }

    private int skipValueBeforeComma(String line, int fromIndex, int howManyTimes) {
        do {
            char c = line.charAt(fromIndex++);
            if (c == ',') howManyTimes--;
        } while (howManyTimes > 0);

        return fromIndex;
    }

    private int getValueBeforeNextComma(String line, int fromIndex, int whereToStore, String[] results) {
        StringBuilder value = new StringBuilder();

        char c = line.charAt(fromIndex);
        while (c != ',') {
            value.append(c);
            c = line.charAt(++fromIndex);
        }
        results[whereToStore] = value.toString();

        return ++fromIndex;
    }
    
}
