package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A String constant pool entry as defined in the JVM specification (section 4.4.3).
 */
public class BtcStringInfo extends BtcCpInfo {

    /** The String_info structure string_index. */
    private final int stringIndex;

    /**
     * Constructs a new String_info structure.
     *
     * @param stringIndex The String_info structure string_index.
     */
    public BtcStringInfo(int stringIndex) {
        super(Tag.STRING);
        this.stringIndex = stringIndex;
    }

    @Override
    public String value() {
        return Integer.toString(stringIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) stringIndex);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 3;
    }

    @Override
    public void log(String pfx) {

    }

    @Override
    public String toString() {
        return null;
    }

}
