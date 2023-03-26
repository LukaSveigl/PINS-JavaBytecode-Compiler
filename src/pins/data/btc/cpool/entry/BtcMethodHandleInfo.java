package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A MethodHandle constant pool entry as defined in the JVM specification (section 4.4.8).
 */
public class BtcMethodHandleInfo extends BtcCpInfo {

    /** The MethodHandle_info structure reference_kind field. */
    private final int referenceKind;

    /** The MethodHandle_info structure reference_index field. */
    private final int referenceIndex;

    /**
     * Constructs a new MethodHandle_info structure.
     *
     * @param referenceKind  The MethodHandle_info structure reference_kind field.
     * @param referenceIndex The MethodHandle_info structure reference_index field.
     */
    public BtcMethodHandleInfo(int referenceKind, int referenceIndex) {
        super(Tag.METHOD_HANDLE);
        this.referenceKind = referenceKind;
        this.referenceIndex = referenceIndex;
    }

    @Override
    public String value() {
        return Integer.toString(referenceKind) + " " + Integer.toString(referenceIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.put((byte) referenceKind);
        byteBuffer.putShort((short) referenceIndex);
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
