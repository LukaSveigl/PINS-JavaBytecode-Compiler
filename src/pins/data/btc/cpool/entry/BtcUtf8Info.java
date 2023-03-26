package pins.data.btc.cpool.entry;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A UTF-8 constant pool entry as defined in the JVM specification (section 4.4.7).
 */
public class BtcUtf8Info extends BtcCpInfo {

    /** The Utf8_info structure length field. */
    private final int length;

    /** The Utf8_info structure bytes field. */
    private final String bytes;

    /**
     * Constructs a new Utf8_info structure.
     *
     * @param bytes The Utf8_info structure bytes field.
     */
    public BtcUtf8Info(String bytes) {
        super(Tag.UTF8);

        this.bytes = bytes;
        this.length = bytes.getBytes(StandardCharsets.UTF_16BE).length;
    }

    @Override
    public String value() {
        return bytes;
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) length);
        byteBuffer.put(bytes.getBytes(StandardCharsets.UTF_16BE));
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        // Tag + Length + Bytes
        return 1 + 2 + length;
    }

    @Override
    public void log(String pfx) {

    }

    @Override
    public String toString() {
        return super.toString();
    }
}

