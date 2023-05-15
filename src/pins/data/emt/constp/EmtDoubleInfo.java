package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Double constant pool entry as defined in the JVM spec (section 4.4.4).
 */
public class EmtDoubleInfo extends EmtConstPoolInfo {

    /** The Double_info structure bytes field. */
    public final double bytes;

    /**
     * Constructs a new Double_info structure.
     *
     * @param bytes The Double_info structure bytes field.
     */
    public EmtDoubleInfo(double bytes) {
        super(Tag.DOUBLE);
        this.bytes = bytes;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putDouble(bytes);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + Bytes
        return 1 + 8;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "DoubleInfo:");
        System.out.println(pfx + "  " + bytes);
    }

}
