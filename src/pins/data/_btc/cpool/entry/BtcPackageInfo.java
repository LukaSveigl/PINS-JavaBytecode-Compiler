package pins.data._btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Package constant pool entry as defined in the JVM specification (section 4.4.12).
 */
public class BtcPackageInfo extends BtcCpInfo {

    /** The Package_info structure name_index field. */
    private final int nameIndex;

    /**
     * Constructs a new Package_info structure.
     *
     * @param nameIndex The Package_info structure name_index field.
     */
    public BtcPackageInfo(int nameIndex) {
        super(Tag.PACKAGE);
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
