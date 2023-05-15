package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Fieldref constant pool entry as defined in the JVM spec (section 4.4.2).
 */
public class EmtFieldrefInfo extends EmtConstPoolInfo {

    /** The Fieldref_info structure class_index field. */
    public final int classIndex;

    /** The Fieldref_info structure name_and_type_index field. */
    public final int nameAndTypeIndex;

    /**
     * Constructs a new Fieldref_info structure.
     *
     * @param classIndex       The Fieldref_info structure class_index field.
     * @param nameAndTypeIndex The Fieldref_info structure name_and_type_index field.
     */
    public EmtFieldrefInfo(int classIndex, int nameAndTypeIndex) {
        super(Tag.FIELD_REF);
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) classIndex);
        byteBuffer.putShort((short) nameAndTypeIndex);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + ClassIndex + NameAndTypeIndex
        return 1 + 2 + 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "FieldrefInfo:");
        System.out.println(pfx + "  " + classIndex);
        System.out.println(pfx + "  " + nameAndTypeIndex);
    }

}
