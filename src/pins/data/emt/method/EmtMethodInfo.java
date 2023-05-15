package pins.data.emt.method;

import pins.data.emt.EmtInfo;
import pins.data.emt.attr.EmtAttributeInfo;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * JVM class method_info struct as specified by the JVM spec (section 4.6).
 */
public class EmtMethodInfo implements EmtInfo {

    public enum AccessFlag {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, BRIDGE, VARARGS, NATIVE, ABSTRACT, STRICT, SYNTHETIC
    }

    /** The method_info structure access_flags field. */
    private final int accessFlags;

    /** The method_info structure name_index field. */
    private final int nameIndex;

    /** The method_info structure descriptor_index field. */
    private final int descriptorIndex;

    /** The method_info structure attributes_count field. */
    private final int attributesCount;

    /** The method_info structure attributes field. */
    private final Vector<EmtAttributeInfo> attributes;

    /**
     * Constructs a new method_info structure.
     *
     * @param accessFlags     The method_info structure access_flags field.
     * @param nameIndex       The method_info structure name_index field.
     * @param descriptorIndex The method_info structure descriptor_index field.
     * @param attributesCount The method_info structure attributes_count field.
     * @param attributes      The method_info structure attributes field.
     */
    public EmtMethodInfo(
            Vector<AccessFlag> accessFlags, int nameIndex, int descriptorIndex, int attributesCount,
            Vector<EmtAttributeInfo> attributes
    ) {
        this.accessFlags = accessFlagsToInt(accessFlags);
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.attributesCount = attributesCount;
        this.attributes = attributes;
    }

    /**
     * Converts a vector of access flags to an integer representation.
     *
     * @param accessFlags The vector of access flags.
     * @return The integer representation of the access flags.
     */
    private int accessFlagsToInt(Vector<AccessFlag> accessFlags) {
        int accessFlagsInt = 0;
        for (AccessFlag flag : accessFlags) {
            accessFlagsInt |= switch (flag) {
                case PUBLIC -> 0x0001;
                case PRIVATE -> 0x0002;
                case PROTECTED -> 0x0004;
                case STATIC -> 0x0008;
                case FINAL -> 0x0010;
                case SYNCHRONIZED -> 0x0020;
                case BRIDGE -> 0x0040;
                case VARARGS -> 0x0080;
                case NATIVE -> 0x0100;
                case ABSTRACT -> 0x0400;
                case STRICT -> 0x0800;
                case SYNTHETIC -> 0x1000;
            };
        }
        return accessFlagsInt;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.putShort((short) accessFlags);
        byteBuffer.putShort((short) nameIndex);
        byteBuffer.putShort((short) descriptorIndex);
        byteBuffer.putShort((short) attributesCount);
        for (EmtAttributeInfo attribute : attributes) {
            byteBuffer.put(attribute.bytecode());
        }
        return byteBuffer.array();
    }

    @Override
    public int size() {
        int size = 2 + // Access flags
                   2 + // Name index
                   2 + // Descriptor index
                   2;  // Attributes count
        for (EmtAttributeInfo attribute : attributes) {
            size += attribute.size();
        }
        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "MethodInfo[" + accessFlags + ", " + nameIndex + ", " + descriptorIndex + ", " + attributesCount + "]");
        for (EmtAttributeInfo attribute : attributes) {
            attribute.log(pfx + "  ");
        }
    }

}
