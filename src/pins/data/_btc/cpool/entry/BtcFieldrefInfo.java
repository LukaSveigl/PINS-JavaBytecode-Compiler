package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Fieldref constant pool entry as defined in the JVM specification (section 4.4.2).
 */
public class BtcFieldrefInfo extends BtcCpInfo {

    /** The Fieldref_info structure class_index. */
    private final int classIndex;

    /** The Fieldref_info structure name_and_type_index. */
    private final int nameAndTypeIndex;

    /**
     * Constructs a new Fieldref_info structure.
     *
     * @param classIndex       The Fieldref_info structure class_index.
     * @param nameAndTypeIndex The Fieldref_info structure name_and_type_index.
     */
    public BtcFieldrefInfo(int classIndex, int nameAndTypeIndex) {
        super(Tag.FIELD_REF);
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    @Override
    public String value() {
        return Integer.toString(classIndex) + " " + Integer.toString(nameAndTypeIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) classIndex);
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
