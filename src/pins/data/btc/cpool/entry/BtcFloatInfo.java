package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Float constant pool entry as defined in the JVM specification (section 4.4.4).
 */
public class BtcFloatInfo extends BtcCpInfo {

    /** The Float_info structure bytes field. */
    private final float bytes;

    /**
     * Constructs a new Float_info structure.
     *
     * @param bytes The Float_info structure bytes field.
     */
    public BtcFloatInfo(float bytes) {
        super(Tag.FLOAT);
        this.bytes = bytes;
    }

    @Override
    public String value() {
        return Float.toString(bytes);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putFloat(bytes);
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
