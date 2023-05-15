package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Integer constant pool entry as defined in the JVM spec (section 4.4.4).
 */
public class EmtIntegerInfo extends EmtConstPoolInfo {

    /** The Integer_info structure bytes field. */
    public final int bytes;

    /**
     * Constructs a new Integer_info structure.
     *
     * @param bytes The Integer_info structure bytes field.
     */
    public EmtIntegerInfo(int bytes) {
        super(Tag.INTEGER);
        this.bytes = bytes;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putInt(bytes);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + Bytes
        return 1 + 4;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "IntegerInfo:");
        System.out.println(pfx + "  " + bytes);
    }

}
