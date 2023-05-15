package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * InvokeDynamic constant pool entry as defined in the JVM spec (section 4.4.10).
 */
public class EmtInvokeDynamicInfo extends EmtConstPoolInfo {

    /** The InvokeDynamic_info structure bootstrap_method_attr_index field. */
    public final int bootstrapMethodAttrIndex;

    /** The InvokeDynamic_info structure name_and_type_index field. */
    public final int nameAndTypeIndex;

    /**
     * Constructs a new InvokeDynamic_info structure.
     *
     * @param bootstrapMethodAttrIndex The InvokeDynamic_info structure bootstrap_method_attr_index field.
     * @param nameAndTypeIndex         The InvokeDynamic_info structure name_and_type_index field.
     */
    public EmtInvokeDynamicInfo(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
        super(Tag.INVOKE_DYNAMIC);
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
        System.out.println(pfx + "InvokeDynamicInfo:");
        System.out.println(pfx + "  " + bootstrapMethodAttrIndex);
        System.out.println(pfx + "  " + nameAndTypeIndex);
    }

}
