package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Module constant pool entry as defined in the JVM spec (section 4.4.11).
 */
public class EmtModuleInfo extends EmtConstPoolInfo {

    /** The Module_info structure name_index field. */
    public final int nameIndex;

    /**
     * Constructs a new Module_info structure.
     *
     * @param nameIndex The Module_info structure name_index field.
     */
    public EmtModuleInfo(int nameIndex) {
        super(Tag.MODULE);
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
        System.out.println(pfx + "ModuleInfo:");
        System.out.println(pfx + "  " + nameIndex);
    }

}
