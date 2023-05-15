package pins.data._btc.fpool;

import pins.common.logger.Loggable;
import pins.data._btc.BtcComp;
import pins.data._btc.apool.BtcAttributeInfo;

import java.nio.ByteBuffer;

/**
 * The JVM field_info structure, as specified by the JVM specification (section 4.5).
 */
public class BtcField implements Loggable, BtcComp {

    /** The field access modifiers. */
    public final int accessFlags = 0x0009; // Public, static

    /** The field name index in the class constant pool. */
    public final int nameIndex;

    /** The field descriptor index in the class constant pool. */
    public final int descriptorIndex;

    /** The number of field attributes. */
    public final int attributesCount;

    /** The field attributes. */
    public BtcAttributeInfo[] attributes = new BtcAttributeInfo[0];

    /**
     * Constructs a new field.
     *
     * @param nameIndex       The field name index in the class constant pool.
     * @param descriptorIndex The field descriptor index in the class constant pool.
     * @param attributes      The field attributes.
     */
    public BtcField(int nameIndex, int descriptorIndex, BtcAttributeInfo[] attributes) {
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        if (attributes != null) {
            this.attributes = attributes;
            this.attributesCount = attributes.length;
        } else {
            this.attributesCount = 0;
        }
        /*this.attributes = attributes.clone();
        this.attributesCount = attributes.length;*/
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
