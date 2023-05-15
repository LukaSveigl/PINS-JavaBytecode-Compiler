package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Methodref constant pool entry as defined in the JVM specification (section 4.4.2).
 */
public class BtcMethodrefInfo extends BtcCpInfo {

    /** The Methodref_info structure class_index field. */
    private final int classIndex;

    /** The Methodref_info structure name_and_type_index field. */
    private final int nameAndTypeIndex;

    /**
     * Constructs a new Methodref_info structure.
     *
     * @param classIndex       The Methodref_info structure class_index field.
     * @param nameAndTypeIndex The Methodref_info structure name_and_type_index field.
     */
    public BtcMethodrefInfo(int classIndex, int nameAndTypeIndex) {
        super(Tag.METHOD_REF);
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
