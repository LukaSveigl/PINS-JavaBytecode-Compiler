package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * MethodType constant pool entry as defined in the JVM spec (section 4.4.9).
 */
public class EmtMethodTypeInfo extends EmtConstPoolInfo {

    /** The MethodType_info structure descriptor_index field. */
    public final int descriptorIndex;

    /**
     * Constructs a new MethodType_info structure.
     *
     * @param descriptorIndex The MethodType_info structure descriptor_index field.
     */
    public EmtMethodTypeInfo(int descriptorIndex) {
        super(Tag.METHOD_TYPE);
        this.descriptorIndex = descriptorIndex;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) descriptorIndex);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + DescriptorIndex
        return 1 + 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "MethodTypeInfo:");
        System.out.println(pfx + "  " + descriptorIndex);
    }

}
