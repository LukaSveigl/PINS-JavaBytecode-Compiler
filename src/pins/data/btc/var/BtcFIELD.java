package pins.data.btc.var;

import java.util.Vector;

/**
 * JVM bytecode field.
 */
public class BtcFIELD extends BtcVar {

    public enum Flags {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, VOLATILE, TRANSIENT, SYNTHETIC, ENUM
    }

    /** The optional field subtype, in case of array or pointer. */
    public final Type subType;

    /** The field name. */
    public final String name;

    /** The field flags. */
    private final Vector<Flags> flags = new Vector<Flags>();

    /**
     * Constructs a new field.
     *
     * @param name The field name.
     * @param type The field type.
     */
    public BtcFIELD(String name, Type type, Type subType) {
        super(type);
        this.name = name;
        this.flags.add(Flags.PUBLIC);
        this.flags.add(Flags.STATIC);
        this.subType = subType;
    }

    /**
     * Returns the field flags.
     *
     * @return The field flags.
     */
    public Vector<Flags> flags() {
        return flags;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "FIELD[" + name + ", " + type + "]");
    }

}
