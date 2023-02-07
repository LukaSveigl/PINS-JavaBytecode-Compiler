package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The constant instruction.
 */
public class BtcCONST extends BtcInstr {

    /** The constant push instruction type. */
    public enum Type {
        ICONST, LCONST, FCONST, DCONST, ACONST
    }

    /** The constant push value. */
    public final long value;

    /** The constant push type. */
    public final Type type;

    /**
     * Constructs a new constant push instruction.
     *
     * @param value The constant push value.
     * @param type  The constant push type.
     */
    public BtcCONST(long value, Type type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type + " " + value);
        /*switch (type) {
            case ICONST -> System.out.println(pfx + "ICONST(" + value + ")");
            case LCONST -> System.out.println(pfx + "LCONST(" + value + ")");
            case FCONST -> System.out.println(pfx + "FCONST(" + value + ")");
            case DCONST -> System.out.println(pfx + "DCONST(" + value + ")");
            case ACONST -> System.out.println(pfx + "ACONST(" + value + ")");
        }*/
    }

    @Override
    public String toString() {
        return "CONST(" + value + ", " + type + ")";
    }

}
