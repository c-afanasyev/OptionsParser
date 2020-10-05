package optionsParser.parsing;

import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.Arrays;

@NoArgsConstructor
public class OptionByteBuffer implements Cloneable {
    @Getter
    private byte[] singleTick;
    @Getter
    private int hashCodeCache = -1;
    
    public OptionByteBuffer(OptionByteBuffer cloneable) {
        this.singleTick = cloneable.getSingleTick().clone();
        hashCodeCache = cloneable.getHashCodeCache();
    }
    
    public OptionByteBuffer setSingleTick(byte[] singleTick) {
        this.singleTick = singleTick;
        hashCodeCache = calcHashCode();
        return this;
    }

    @Override
    public int hashCode() {
        return hashCodeCache;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof OptionByteBuffer)) {
            return false;
        } else {
            OptionByteBuffer other = (OptionByteBuffer)o;
            return Arrays.equals(singleTick, other.getSingleTick());
        }
    }

    private int calcHashCode() {
        return 59 + Arrays.hashCode(singleTick);
    }
}
