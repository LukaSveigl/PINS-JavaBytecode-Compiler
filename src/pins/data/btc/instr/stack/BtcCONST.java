package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The constant instruction.
 *
 * Pushes a constant value of type int, float, long, double or Object onto the stack.
 */
public class BtcCONST extends BtcInstr {

    /** The constant push instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE, ARR
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
            case INT -> 0x10;
            case LONG -> 0x11;
            case FLOAT -> 0x12;
            case DOUBLE -> 0x13;
            case ARR -> 0x14;
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        switch (type) {
            case INT, FLOAT, ARR -> {
                hex.add((int) value);
            }
            case LONG, DOUBLE -> {
                hex.add((int) value);
                hex.add((int) (value >> 32));
            }
        }
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + "CONST " + value);
    }

    @Override
    public String toString() {
        return "CONST(" + value + ", " + type + ")";
    }

}
