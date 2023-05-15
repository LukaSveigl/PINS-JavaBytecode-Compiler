package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * MethodHandle constant pool entry as defined in the JVM spec (section 4.4.8).
 */
public class EmtMethodHandleInfo extends EmtConstPoolInfo {

    /** The MethodHandle_info structure reference_kind field. */
    public final int referenceKind;

    /** The MethodHandle_info structure reference_index field. */
    public final int referenceIndex;

    /**
     * Constructs a new MethodHandle_info structure.
     *
     * @param referenceKind  The MethodHandle_info structure reference_kind field.
     * @param referenceIndex The MethodHandle_info structure reference_index field.
     */
    public EmtMethodHandleInfo(int referenceKind, int referenceIndex) {
        super(Tag.METHOD_HANDLE);
        this.referenceKind = referenceKind;
        this.referenceIndex = referenceIndex;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.put((byte) referenceKind);
        byteBuffer.putShort((short) referenceIndex);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + ReferenceKind + ReferenceIndex
        return 1 + 1 + 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "MethodHandleInfo:");
        System.out.println(pfx + "  " + referenceKind);
        System.out.println(pfx + "  " + referenceIndex);
    }

}
