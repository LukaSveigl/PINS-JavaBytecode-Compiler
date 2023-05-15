package pins.data.btc.var;

/**
 * JVM bytecode method local variable.
 */
public class BtcLOCAL extends BtcVar {

    /** The local variable line number. */
    public final int index;

    /**
     * Constructs a new local variable.
     *
     * @param index The local variable index.
     * @param type  The local variable type.
     */
    public BtcLOCAL(int index, Type type) {
        super(type);
        this.index = index;
    }

    /**
     * Returns the local variable size measured in local variable slots.
     *
     * @return The local variable size measured in local variable slots.
     */
    public int size() {
        return switch (type) {
            case INT, FLOAT, BOOL, STRING, ARRAY, OBJECT -> 1;
            case LONG, DOUBLE -> 2;
            default -> 0;
        };
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "LOCAL[" + index + ", " + type + "]");
    }

}
