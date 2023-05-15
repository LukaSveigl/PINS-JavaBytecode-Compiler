package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * NameAndType constant pool entry as defined in the JVM spec (section 4.4.6).
 */
public class EmtNameAndTypeInfo extends EmtConstPoolInfo {

    /** The NameAndType_info structure name_index field. */
    public final int nameIndex;

    /** The NameAndType_info structure descriptor_index field. */
    public final int descriptorIndex;

    /**
     * Constructs a new NameAndType_info structure.
     *
     * @param nameIndex       The NameAndType_info structure name_index field.
     * @param descriptorIndex The NameAndType_info structure descriptor_index field.
     */
    public EmtNameAndTypeInfo(int nameIndex, int descriptorIndex) {
        super(Tag.NAME_AND_TYPE);
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) nameIndex);
        byteBuffer.putShort((short) descriptorIndex);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + NameIndex + DescriptorIndex
        return 1 + 2 + 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "NameAndTypeInfo:");
        System.out.println(pfx + "  " + nameIndex);
        System.out.println(pfx + "  " + descriptorIndex);
    }

}
