package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A MethodType constant pool entry as defined in the JVM specification (section 4.4.9).
 */
public class BtcMethodTypeInfo extends BtcCpInfo {

    /** The MethodType_info structure descriptor_index field. */
    private final int descriptorIndex;

    /**
     * Constructs a new MethodType_info structure.
     *
     * @param descriptorIndex The MethodType_info structure descriptor_index field.
     */
    public BtcMethodTypeInfo(int descriptorIndex) {
        super(Tag.METHOD_TYPE);
        this.descriptorIndex = descriptorIndex;
    }

    @Override
    public String value() {
        return Integer.toString(descriptorIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) descriptorIndex);
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
