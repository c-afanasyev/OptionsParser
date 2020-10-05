package optionsParser.parsing;

public class BytesArrToIntPriceConverter {
    
    public static int convertBytesArrToInt(byte[] bytes, int startFrom, byte places) {
        switch (places) {
            case 2: return convert2BytesToInt(bytes, startFrom);
            case 3: return convert3BytesToInt(bytes, startFrom);
            case 4: return convert4BytesToInt(bytes, startFrom);
            case 5: return convert5BytesToInt(bytes, startFrom);
            case 6: return convert6BytesToInt(bytes, startFrom);
            case 7: return convert7BytesToInt(bytes, startFrom);
            case 8: return convert8BytesToInt(bytes, startFrom);
            default: return bytes[startFrom] - 48;
        }
    }

    private static int convert2BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10);
    }

    private static int convert3BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10) +
                ((bytes[startFrom-2] - 48) * 100);
    }

    private static int convert4BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10) +
                ((bytes[startFrom-2] - 48) * 100) +
                ((bytes[startFrom-3] - 48) * 1000);
    }

    private static int convert5BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10) +
                ((bytes[startFrom-2] - 48) * 100) +
                ((bytes[startFrom-3] - 48) * 1000) +
                ((bytes[startFrom-4] - 48) * 10000);
    }

    private static int convert6BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10) +
                ((bytes[startFrom-2] - 48) * 100) +
                ((bytes[startFrom-3] - 48) * 1000) +
                ((bytes[startFrom-4] - 48) * 10000) +
                ((bytes[startFrom-5] - 48) * 10000);
    }

    private static int convert7BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10) +
                ((bytes[startFrom-2] - 48) * 100) +
                ((bytes[startFrom-3] - 48) * 1000) +
                ((bytes[startFrom-4] - 48) * 10000) +
                ((bytes[startFrom-5] - 48) * 100000) +
                ((bytes[startFrom-6] - 48) * 1000000);
    }

    private static int convert8BytesToInt(byte[] bytes, int startFrom) {
        return (bytes[startFrom] - 48) +
                ((bytes[startFrom-1] - 48) * 10) +
                ((bytes[startFrom-2] - 48) * 100) +
                ((bytes[startFrom-3] - 48) * 1000) +
                ((bytes[startFrom-4] - 48) * 10000) +
                ((bytes[startFrom-5] - 48) * 100000) +
                ((bytes[startFrom-6] - 48) * 1000000) +
                ((bytes[startFrom-7] - 48) * 10000000);
    }
}
