package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Module constant pool entry as defined in the JVM specification (section 4.4.11).
 */
public class BtcModuleInfo extends BtcCpInfo {

    /** The Module_info structure name_index field. */
    private final int nameIndex;

    /**
     * Constructs a new Module_info structure.
     *
     * @param nameIndex The Module_info structure name_index field.
     */
    public BtcModuleInfo(int nameIndex) {
        super(Tag.MODULE);
        this.nameIndex = nameIndex;
    }

    @Override
    public String value() {
        return Integer.toString(nameIndex);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) nameIndex);
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
