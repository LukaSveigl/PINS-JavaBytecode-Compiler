package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The constant instruction.
 *
 * Pushes a constant value of type int, float, long, double or Object onto the stack.
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
        this.opcode = switch (type) {
            case ICONST -> 0x10;
            case LCONST -> 0x11;
            case FCONST -> 0x12;
            case DCONST -> 0x13;
            case ACONST -> 0x14;
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        switch (type) {
            case ICONST, FCONST, ACONST -> {
                hex.add((int) value);
            }
            case LCONST, DCONST -> {
                hex.add((int) value);
                hex.add((int) (value >> 32));
            }
        }
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type + " " + value);
    }

    @Override
    public String toString() {
        return "CONST(" + value + ", " + type + ")";
    }

}
