package pins.data.btc.cpool;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;
import pins.data.btc.cpool.entry.BtcCpInfo;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The JVM constant pool, as specified by the JVM specification (section 4.4).
 */
public class BtcConstPool implements Loggable, BtcComp {

    /** The constant pool entries. */
    private final Vector<BtcCpInfo> entries = new Vector<>();

    /**
     * Constructs a new constant pool.
     */
    public BtcConstPool() {

    }

    /**
     * Adds a constant pool entry to the constant pool.
     *
     * @param entry The constant pool entry to add.
     */
    public int addEntry(BtcCpInfo entry) {
        entries.add(entry);
        return entries.size();
    }

    /**
     * Returns the entry in the constant pool at the specified index.
     *
     * @param index The index of the entry.
     * @return The entry in the constant pool at the specified index.
     */
    public BtcCpInfo getEntry(int index) {
        return entries.get(index);
    }

    /**
     * Returns the constant pool entry with the specified value.
     *
     * @param value The value of the entry.
     * @return The constant pool entry with the specified value.
     */
    public BtcCpInfo getEntry(String value) {
        return entries.stream().filter(entry -> entry.value().equals(value)).findFirst().orElse(null);
    }

    /**
     * Returns the constant pool entries.
     *
     * @return The constant pool entries.
     */
    public Vector<BtcCpInfo> entries() {
        return entries;
    }

    /**
     * Returns the constant pool size.
     *
     * @return The constant pool size.
     */
    public int getEntryCount() {
        return entries.size();
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getBytecodeLength());
        byteBuffer.putShort((short) (entries.size() + 1));
        for (BtcCpInfo entry : entries) {
            byteBuffer.put(entry.toBytecode());
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        int size = 2;
        for (BtcCpInfo entry : entries) {
            size += entry.getBytecodeLength();
        }
        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "Constant pool:");
        for (BtcCpInfo entry : entries) {
            entry.log(pfx + " ");
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Constant pool: ");
        stringBuilder.append(entries.size());
        for (BtcCpInfo entry : entries) {
            stringBuilder.append(entry);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

}
