package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Double constant pool entry as defined in the JVM specification (section 4.4.5).
 */
public class BtcDoubleInfo extends BtcCpInfo {

    /** The Double_info structure bytes field. */
    private final double bytes;

    /**
     * Constructs a new Double_info structure.
     *
     * @param bytes The Double_info structure bytes field.
     */
    public BtcDoubleInfo(double bytes) {
        super(Tag.DOUBLE);
        this.bytes = bytes;
    }

    @Override
    public String value() {
        return Double.toString(bytes);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putDouble(bytes);
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
