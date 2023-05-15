package pins.data.emt.field;

import pins.data.emt.EmtInfo;
import pins.data.emt.attr.EmtAttributeInfo;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * JVM class field_info struct as specified by the JVM spec (section 4.5).
 */
public class EmtFieldInfo implements EmtInfo {

    public enum AccessFlag {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, VOLATILE, TRANSIENT, SYNTHETIC, ENUM
    }

    /** The field_info structure access_flags field. */
    public final int accessFlags;

    /** The field_info structure name_index field. */
    public final int nameIndex;

    /** The field_info structure descriptor_index field. */
    public final int descriptorIndex;

    /** The field_info structure attributes_count field. */
    public final int attributesCount;

    /** The field_info structure attributes field. */
    public final EmtAttributeInfo[] attributes;

    /**
     * Constructs a new field_info structure.
     *
     * @param accessFlags     The field_info structure access_flags field.
     * @param nameIndex       The field_info structure name_index field.
     * @param descriptorIndex The field_info structure descriptor_index field.
     * @param attributes      The field_info structure attributes field.
     */
    public EmtFieldInfo(
            Vector<AccessFlag> accessFlags, int nameIndex, int descriptorIndex, int attributesCount,
            EmtAttributeInfo[] attributes
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
        for (AccessFlag accessFlag : accessFlags) {
            accessFlagsInt |= switch (accessFlag) {
                case PUBLIC -> 0x0001;
                case PRIVATE -> 0x0002;
                case PROTECTED -> 0x0004;
                case STATIC -> 0x0008;
                case FINAL -> 0x0010;
                case VOLATILE -> 0x0040;
                case TRANSIENT -> 0x0080;
                case SYNTHETIC -> 0x1000;
                case ENUM -> 0x4000;
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
        if (attributes != null) {
            for (EmtAttributeInfo attribute : attributes) {
                byteBuffer.put(attribute.bytecode());
            }
        }
        return byteBuffer.array();
    }

    @Override
    public int size() {
        int size = 2 + // Access flags
                   2 + // Name index
                   2 + // Descriptor index
                   2;  // Attributes count
        if (attributes != null) {
            for (EmtAttributeInfo attribute : attributes) {
                size += attribute.size();
            }
        }
        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "FieldInfo[" + accessFlags + ", " + nameIndex + ", " + descriptorIndex + ", " + attributesCount + "]");
    }

}
