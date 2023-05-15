package pins.data._btc.cpool.entry;

import pins.common.logger.Loggable;
import pins.data._btc.BtcComp;

/**
 * The JVM constant pool entry, as specified by the JVM specification (section 4.4).
 */
public abstract class BtcCpInfo implements BtcComp, Loggable {

    /** The constant pool entry tag. */
    public enum Tag {
        UTF8(1), INTEGER(3), FLOAT(4), LONG(5), DOUBLE(6), CLASS(7), STRING(8),
        FIELD_REF(9), METHOD_REF(10), INTERFACE_METHOD_REF(11), NAME_AND_TYPE(12), METHOD_HANDLE(15),
        METHOD_TYPE(16), DYNAMIC(17), INVOKE_DYNAMIC(18), MODULE(19), PACKAGE(20);

        /** The tag value. */
        public final int value;

        /**
         * Constructs a new tag.
         *
         * @param value The tag value.
         */
        Tag(int value) {
            this.value = value;
        }
    }

    /** The constant pool entry tag. */
    protected Tag tag;

    /**
     * Constructs a new constant pool entry.
     *
     * @param tag The constant pool entry tag.
     */
    protected BtcCpInfo(Tag tag) {
        this.tag = tag;
    }

    /**
     * Returns the constant pool entry tag.
     *
     * @return The constant pool entry tag.
     */
    public Tag tag() {
        return tag;
    }

    /**
     * Returns the constant pool entry value.
     *
     * @return The constant pool entry value.
     */
    public abstract String value();

}
