package pins.data.emt.constp;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * UTF-8 constant pool entry as defined in the JVM spec (section 4.4.7).
 */
public class EmtUTF8Info extends EmtConstPoolInfo {

    /** The Utf8_info structure length field. */
    public final int length;

    /** The Utf8_info structure bytes field. */
    public final String bytes;

    /**
     * Constructs a new Utf8_info structure.
     *
     * @param bytes The Utf8_info structure bytes field.
     */
    public EmtUTF8Info(String bytes) {
        super(Tag.UTF8);
        this.length = bytes.getBytes(StandardCharsets.UTF_8).length;
        this.bytes = bytes;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) length);
        byteBuffer.put(bytes.getBytes(StandardCharsets.UTF_8));
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + Length + Bytes
        return 1 + 2 + length;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "UTF8Info[" + length + "]:");
        System.out.println(pfx + "  " + bytes);
    }

}
