package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * An Integer constant pool entry as defined in the JVM specification (section 4.4.4).
 */
public class BtcIntegerInfo extends BtcCpInfo {

    /** The Integer_info structure bytes field. */
    private final int bytes;

    /**
     * Constructs a new Integer_info structure.
     *
     * @param bytes The Integer_info structure bytes field.
     */
    public BtcIntegerInfo(int bytes) {
        super(Tag.INTEGER);
        this.bytes = bytes;
    }

    @Override
    public String value() {
        return Integer.toString(bytes);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putInt(bytes);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 5;
    }

    @Override
    public void log(String pfx) {

    }

    @Override
    public String toString() {
        return null;
    }

}
