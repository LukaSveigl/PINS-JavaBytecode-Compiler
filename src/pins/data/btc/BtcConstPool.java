package pins.data.btc;

import java.util.HashMap;
import java.util.Vector;

/**
 * The JVM class constant pool.
 */
public class BtcConstPool implements BtcComp {

    /**
     * The constant pool tags.
     */
    public enum Tag {
        UTF8(1), INTEGER(3), FLOAT(4), LONG(5), DOUBLE(6), CLASS(7), STRING(8),
        FIELDREF(9), METHODREF(10), INTERFACE_METHODREF(11), NAME_AND_TYPE(12), METHOD_HANDLE(15),
        METHOD_TYPE(16), DYNAMIC(17), INVOKE_DYNAMIC(18), MODULE(19), PACKAGE(20);

        private int value;

        Tag(int value) {
            this.value = value;
        }

        public int tagValue() {
            return this.value;
        }
    }

    /**
     * The constant pool entry, specified by the tag and value.
     */
    public static class BtcConstPoolEntry {
        /**
         * The entry index in the constant pool.
         */
        public final Tag tag;

        /**
         * The entry value in the constant pool.
         */
        public final String value;

        /**
         * Constructs a new constant pool entry.
         *
         * @param tag   The entry tag.
         * @param value The entry value.
         */
        public BtcConstPoolEntry(Tag tag, String value) {
            this.tag = tag;
            this.value = value;
        }
    }

    /**
     * The constant pool, which is a mapping of indices and entries.
     */
    private HashMap<Integer, BtcConstPoolEntry> constPool = new HashMap<>();

    /**
     * The const pool size.
     */
    private int constPoolSize = 1;

    /**
     * Constructs a new constant pool.
     */
    public BtcConstPool() {

    }

    /**
     * Adds an entry to the constant pool and increments the constant pool counter.
     *
     * @param tag   The entry tag.
     * @param value The entry value.
     */
    public void addEntry(Tag tag, String value) {
        this.constPool.put(this.constPoolSize, new BtcConstPoolEntry(tag, value));

        if (tag == Tag.LONG || tag == Tag.DOUBLE) {
            this.constPoolSize += 2;
        } else {
            this.constPoolSize++;
        }
    }

    /**
     * Gets the entry at the specified index.
     *
     * @param index The const pool index.
     * @return The entry, or null if no index like this exists.
     */
    public BtcConstPoolEntry getEntry(int index) {
        return this.constPool.getOrDefault(index, null);
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        return null;
    }

}
