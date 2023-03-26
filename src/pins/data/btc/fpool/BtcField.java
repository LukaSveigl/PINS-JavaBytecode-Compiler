package pins.data.btc.fpool;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;
import pins.data.btc.apool.BtcAttributeInfo;

import java.nio.ByteBuffer;

/**
 * The JVM field_info structure, as specified by the JVM specification (section 4.5).
 */
public class BtcField implements Loggable, BtcComp {

    /**
     * The field descriptor.
     */
    public enum Descriptor {
        BOOLEAN("Z"),
        BYTE("B"),
        CHAR("C"),
        SHORT("S"),
        INT("I"),
        LONG("J"),
        FLOAT("F"),
        DOUBLE("D"),
        REFERENCE("L"),
        ARRAY("[");

        /** The descriptor. */
        public final String descriptor;

        /**
         * Constructs a new descriptor.
         *
         * @param descriptor The descriptor.
         */
        Descriptor(String descriptor) {
            this.descriptor = descriptor;
        }
    }

    /** The field access modifiers. */
    public final int accessFlags = 0x0001; // Public

    /** The field name index in the class constant pool. */
    public final int nameIndex;

    /** The field descriptor index in the class constant pool. */
    public final int descriptorIndex;

    /** The number field attributes. */
    public final int attributesCount;

    /** The field attributes. */
    public BtcAttributeInfo[] attributes = new BtcAttributeInfo[0];

    /**
     * Constructs a new field.
     *
     * @param nameIndex       The field name index in the class constant pool.
     * @param descriptorIndex The field descriptor index in the class constant pool.
     * @param attributesCount The number field attributes.
     */
    public BtcField(int nameIndex, int descriptorIndex, int attributesCount) {
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.attributesCount = attributesCount;
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.putShort((short) accessFlags);
        byteBuffer.putShort((short) nameIndex);
        byteBuffer.putShort((short) descriptorIndex);
        byteBuffer.putShort((short) attributesCount);
        for (BtcAttributeInfo attribute : attributes) {
            byteBuffer.put(attribute.toBytecode());
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        int size = 2 + 2 + 2 + 2;
        for (BtcAttributeInfo attribute : attributes) {
            size += attribute.getBytecodeLength();
        }
        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "BtcField: " + nameIndex + " " + descriptorIndex + " " + attributesCount);
        for (BtcAttributeInfo attribute : attributes) {
            attribute.log(pfx + "  ");
        }
    }

    @Override
    public String toString() {
        return "FieldInfo[" +
               "accessFlags:" + accessFlags +
               ", nameIndex:" + nameIndex +
               ", descriptorIndex:" + descriptorIndex +
               ", attributesCount:" + attributesCount +
               ']';
    }
}
