package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Class constant pool entry as defined in the JVM spec (section 4.4.1).
 */
public class EmtClassInfo extends EmtConstPoolInfo {

    /** The Class_info structure name_index field. */
    public final int nameIndex;

    /**
     * Constructs a new Class_info structure.
     *
     * @param nameIndex The Class_info structure name_index field.
     */
    public EmtClassInfo(int nameIndex) {
        super(Tag.CLASS);
        this.nameIndex = nameIndex;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.put((byte) tag.value);
        byteBuffer.putShort((short) nameIndex);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        // Tag + NameIndex
        return 1 + 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "ClassInfo:");
        System.out.println(pfx + "  " + nameIndex);
    }

}
