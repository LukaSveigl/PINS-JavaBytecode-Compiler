package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;

/**
 * A Class constant pool entry as defined in the JVM specification (section 4.4.1).
 */
public class BtcClassInfo extends BtcCpInfo {

    /** The Class_info structure name_index field. */
    private final int nameIndex;

    /**
     * Constructs a new Class_info structure.
     *
     * @param nameIndex The Class_info structure name_index field.
     */
    public BtcClassInfo(int nameIndex) {
        super(Tag.CLASS);
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

