package pins.data.btc.apool;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The JVM class attribute pool.
 */
public class BtcAttributePool implements Loggable, BtcComp {

    /** The attribute pool entries. */
    private Vector<BtcAttributeInfo> entries = new Vector<>();

    /**
     * Constructs a new attribute pool.
     */
    public BtcAttributePool() {

    }

    /**
     * Adds an attribute to the attribute pool.
     *
     * @param entry The attribute to add.
     * @return The index of the attribute in the attribute pool.
     */
    public int addEntry(BtcAttributeInfo entry) {
        entries.add(entry);
        return entries.size() - 1;
    }

    /**
     * Returns the attribute at the specified index.
     *
     * @param index The index of the attribute.
     * @return The attribute at the specified index.
     */
    public BtcAttributeInfo getEntry(int index) {
        return entries.get(index);
    }

    /**
     * Returns the attribute pool entries.
     *
     * @return The attribute pool entries.
     */
    public Vector<BtcAttributeInfo> entries() {
        return entries;
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.putShort((short) entries.size());
        for (BtcAttributeInfo entry : entries) {
            byteBuffer.put(entry.toBytecode());
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        int size = 2;
        for (BtcAttributeInfo entry : entries) {
            size += entry.getBytecodeLength();
        }
        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "Attribute pool:");
        for (BtcAttributeInfo entry : entries) {
            entry.log(pfx + "  ");
        }
    }

    @Override
    public String toString() {
        return "Attribute pool: " + entries.size() + " entries";
    }

}
