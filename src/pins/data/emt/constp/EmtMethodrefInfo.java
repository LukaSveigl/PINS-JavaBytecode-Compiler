package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Methodref constant pool entry as defined in the JVM spec (section 4.4.2).
 */
public class EmtMethodrefInfo extends EmtConstPoolInfo {

    /** The Methodref_info structure class_index field. */
    public final int classIndex;

    /** The Methodref_info structure name_and_type_index field. */
    public final int nameAndTypeIndex;

    /**
     * Constructs a new Methodref_info structure.
     *
     * @param classIndex       The Methodref_info structure class_index field.
     * @param nameAndTypeIndex The Methodref_info structure name_and_type_index field.
     */
    public EmtMethodrefInfo(int classIndex, int nameAndTypeIndex) {
        super(Tag.METHOD_REF);
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
        System.out.println(pfx + "MethodrefInfo:");
        System.out.println(pfx + "  " + classIndex);
        System.out.println(pfx + "  " + nameAndTypeIndex);
    }

}
