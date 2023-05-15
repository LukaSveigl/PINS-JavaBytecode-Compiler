package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Long constant pool entry as defined in the JVM spec (section 4.4.4).
 */
public class EmtLongInfo extends EmtConstPoolInfo {

    /** The Long_info structure bytes field. */
    public final long bytes;

    /**
     * Constructs a new Long_info structure.
     *
     * @param bytes The Long_info structure bytes field.
     */
    public EmtLongInfo(long bytes) {
        super(Tag.LONG);
        this.bytes = bytes;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putLong(bytes);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + Bytes
        return 1 + 8;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "LongInfo:");
        System.out.println(pfx + "  " + bytes);
    }

}
