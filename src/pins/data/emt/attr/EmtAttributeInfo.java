package pins.data.emt.attr;

import pins.data.emt.EmtInfo;

import java.nio.ByteBuffer;

/**
 * JVM class attribute_info struct as specified by the JVM spec (section 4.7).
 */
public class EmtAttributeInfo implements EmtInfo {

    /** The attribute_info structure attribute_name_index field. */
    public final int attributeNameIndex;

    /** The attribute_info structure attribute_length field. */
    public final int attributeLength;

    /** The attribute_info structure info field. */
    public final byte[] info;

    /**
     * Constructs a new attribute_info structure.
     *
     * @param attributeNameIndex The attribute_info structure attribute_name_index field.
     * @param attributeLength    The attribute_info structure attribute_length field.
     * @param info               The attribute_info structure info field.
     */
    public EmtAttributeInfo(int attributeNameIndex, int attributeLength, byte[] info) {
        this.attributeNameIndex = attributeNameIndex;
        this.attributeLength = attributeLength;
        this.info = info;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.putShort((short) attributeNameIndex);
        byteBuffer.putInt(attributeLength);
        byteBuffer.put(info);
        return byteBuffer.array();
    }

    @Override
    public int size() {
        return 2 + // Attribute name index
               4 + // Attribute length
               info.length;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "AttributeInfo[" + attributeNameIndex + ", " + attributeLength + "]");
        System.out.print(pfx + "  ");
        for (byte b : info) {
            System.out.print(b + " ");
        }
    }

}
