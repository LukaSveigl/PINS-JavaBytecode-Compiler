package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Dynamic constant pool entry as defined in the JVM specification (section 4.4.10).
 */
public class BtcDynamicInfo extends BtcCpInfo {

    /** The Dynamic_info structure bootstrap_method_attr_index field. */
    private final int bootstrapMethodAttrIndex;

    /** The Dynamic_info structure name_and_type_index field. */
    private final int nameAndTypeIndex;

    /**
     * Constructs a new Dynamic_info structure.
     *
     * @param bootstrapMethodAttrIndex The Dynamic_info structure bootstrap_method_attr_index field.
     * @param nameAndTypeIndex         The Dynamic_info structure name_and_type_index field.
     */
    public BtcDynamicInfo(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
        super(Tag.DYNAMIC);
        this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    @Override
    public String value() {
        return Integer.toString(bootstrapMethodAttrIndex) + " " + Integer.toString(nameAndTypeIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) bootstrapMethodAttrIndex);
        byteBuffer.putShort((short) nameAndTypeIndex);
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
