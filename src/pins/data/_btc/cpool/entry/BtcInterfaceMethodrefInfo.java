package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * An InterfaceMethodref constant pool entry as defined in the JVM specification (section 4.4.2).
 */
public class BtcInterfaceMethodrefInfo extends BtcCpInfo {

    /** The InterfaceMethodref_info structure class_index field. */
    private final int classIndex;

    /** The InterfaceMethodref_info structure name_and_type_index field. */
    private final int nameAndTypeIndex;

    /**
     * Constructs a new InterfaceMethodref_info structure.
     *
     * @param classIndex       The InterfaceMethodref_info structure class_index field.
     * @param nameAndTypeIndex The InterfaceMethodref_info structure name_and_type_index field.
     */
    public BtcInterfaceMethodrefInfo(int classIndex, int nameAndTypeIndex) {
        super(Tag.INTERFACE_METHOD_REF);
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
