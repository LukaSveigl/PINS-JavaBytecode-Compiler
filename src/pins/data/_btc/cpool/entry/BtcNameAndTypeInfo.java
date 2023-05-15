package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A NameAndType constant pool entry as defined in the JVM specification (section 4.4.6).
 */
public class BtcNameAndTypeInfo extends BtcCpInfo {

    /** The NameAndType_info structure name_index field. */
    private final int nameIndex;

    /** The NameAndType_info structure descriptor_index field. */
    private final int descriptorIndex;

    /**
     * Constructs a new NameAndType_info structure.
     *
     * @param nameIndex       The NameAndType_info structure name_index field.
     * @param descriptorIndex The NameAndType_info structure descriptor_index field.
     */
    public BtcNameAndTypeInfo(int nameIndex, int descriptorIndex) {
        super(Tag.NAME_AND_TYPE);
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
    }

    @Override
    public String value() {
        return Integer.toString(nameIndex) + " " + Integer.toString(descriptorIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) nameIndex);
        byteBuffer.putShort((short) descriptorIndex);
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
