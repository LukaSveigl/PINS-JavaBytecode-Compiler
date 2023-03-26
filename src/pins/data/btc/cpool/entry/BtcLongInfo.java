package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Double constant pool entry as defined in the JVM specification (section 4.4.5).
 */
public class BtcLongInfo extends BtcCpInfo {

    /** The Long_info structure bytes field. */
    private final long bytes;

    /**
     * Constructs a new Long_info structure.
     *
     * @param bytes The Long_info structure bytes field.
     */
    public BtcLongInfo(long bytes) {
        super(Tag.LONG);
        this.bytes = bytes;
    }

    @Override
    public String value() {
        return Long.toString(bytes);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putLong(bytes);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 9;
    }

    @Override
    public void log(String pfx) {

    }

    @Override
    public String toString() {
        return null;
    }

}
