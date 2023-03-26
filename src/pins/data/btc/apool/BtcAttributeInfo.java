package pins.data.btc.apool;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The JVM class attribute_info struct as specified by the JVM spec (section 4.7).
 */
public class BtcAttributeInfo implements Loggable, BtcComp {

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
    public BtcAttributeInfo(int attributeNameIndex, int attributeLength, String info) {
        this.attributeNameIndex = attributeNameIndex;
        this.attributeLength = attributeLength;
        this.info = info.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.putShort((short) attributeNameIndex);
        byteBuffer.putInt(attributeLength);
        byteBuffer.put(info);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 2 + 4 + info.length;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "BtcAttributeInfo: " + attributeNameIndex + " " + attributeLength + ": ");
        for (byte b : info) {
            System.out.print(b + " ");
        }
    }

    public String toString() {
        return "BtcAttributeInfo: " + attributeNameIndex + " " + attributeLength;
    }

}
