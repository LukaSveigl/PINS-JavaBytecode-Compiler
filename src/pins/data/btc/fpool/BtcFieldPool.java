package pins.data.btc.fpool;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The JVM field pool, as specified by the JVM specification (section 4.5).
 */
public class BtcFieldPool implements Loggable, BtcComp {

    /** The fields. */
    private final Vector<BtcField> entries = new Vector<>();

    private int currentIndex = 0;

    /**
     * Constructs a new field pool.
     */
    public BtcFieldPool() {

    }

    /**
     * Adds a field to the field pool.
     *
     * @param entry The field to add.
     */
    public int addField(BtcField entry) {
        entries.add(entry);
        return entries.size() - 1;
    }

    /**
     * Returns the field in the field pool at the specified index.
     *
     * @param index The index of the field.
     * @return The field in the field pool at the specified index.
     */
    public BtcField getField(int index) {
        return entries.get(index);
    }

    /**
     * Returns the field in the field pool with the specified name index. Same as searching by name.
     *
     * @param nameIndex The name index of the field.
     * @return The field in the field pool with the specified name index.
     */
    public BtcField getFieldByNameIndex(int nameIndex) {
        return entries.stream().filter(f -> f.nameIndex == nameIndex).findFirst().orElse(null);
    }

    /**
     * Returns the field pool entries.
     *
     * @return The field pool entries.
     */
    public Vector<BtcField> entries() {
        return entries;
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.putShort((short) entries.size());
        for (BtcField entry : entries) {
            byteBuffer.put(entry.toBytecode());
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        int size = 2;
        for (BtcField entry : entries) {
            size += entry.getBytecodeLength();
        }
        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "BtcFieldPool:");
        for (BtcField entry : entries) {
            entry.log(pfx + "  ");
        }
    }

    @Override
    public String toString() {
        return "BtcFieldPool{}";
    }

}
