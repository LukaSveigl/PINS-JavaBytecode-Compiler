package pins.data.emt.constp;

import java.nio.ByteBuffer;

/**
 * Package constant pool entry as defined in the JVM spec (section 4.4.12).
 */
public class EmtPackageInfo extends EmtConstPoolInfo {

    /** The Package_info structure name_index field. */
    public final int nameIndex;

    /**
     * Constructs a new Package_info structure.
     *
     * @param nameIndex The Package_info structure name_index field.
     */
    public EmtPackageInfo(int nameIndex) {
        super(Tag.PACKAGE);
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
        System.out.println(pfx + "PackageInfo:");
        System.out.println(pfx + "  " + nameIndex);
    }

}
