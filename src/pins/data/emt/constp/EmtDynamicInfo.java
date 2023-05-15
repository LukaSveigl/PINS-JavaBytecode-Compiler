package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Dynamic constant pool entry as defined in the JVM spec (section 4.4.10).
 */
public class EmtDynamicInfo extends EmtConstPoolInfo {

    /** The Dynamic_info structure bootstrap_method_attr_index field. */
    public final int bootstrapMethodAttrIndex;

    /** The Dynamic_info structure name_and_type_index field. */
    public final int nameAndTypeIndex;

    /**
     * Constructs a new Dynamic_info structure.
     *
     * @param bootstrapMethodAttrIndex The Dynamic_info structure bootstrap_method_attr_index field.
     * @param nameAndTypeIndex         The Dynamic_info structure name_and_type_index field.
     */
    public EmtDynamicInfo(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
        super(Tag.DYNAMIC);
        this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) bootstrapMethodAttrIndex);
        byteBuffer.putShort((short) nameAndTypeIndex);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + BootstrapMethodAttrIndex + NameAndTypeIndex
        return 1 + 2 + 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "DynamicInfo:");
        System.out.println(pfx + "  " + bootstrapMethodAttrIndex);
        System.out.println(pfx + "  " + nameAndTypeIndex);
    }

}
