package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Float constant pool entry as defined in the JVM spec (section 4.4.4).
 */
public class EmtFloatInfo extends EmtConstPoolInfo {

    /** The Float_info structure bytes field. */
    public final float bytes;

    /**
     * Constructs a new Float_info structure.
     *
     * @param bytes The Float_info structure bytes field.
     */
    public EmtFloatInfo(float bytes) {
        super(Tag.FLOAT);
        this.bytes = bytes;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putFloat(bytes);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + Bytes
        return 1 + 4;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "FloatInfo:");
        System.out.println(pfx + "  " + bytes);
    }

}
