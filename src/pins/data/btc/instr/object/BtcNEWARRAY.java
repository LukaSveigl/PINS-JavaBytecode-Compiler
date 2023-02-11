package pins.data.btc.instr.object;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The new array instruction.
 *
 * Creates a new array. The size of the array is popped from the stack. The array reference is pushed back.
 */
public class BtcNEWARRAY extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        T_BOOLEAN, T_CHAR, T_FLOAT, T_DOUBLE, T_BYTE, T_SHORT, T_INT, T_LONG
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new new array instruction.
     *
     * @param type The type of the array.
     */
    public BtcNEWARRAY(Type type) {
        this.type = type;
        this.opcode = 0xbc;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        // The type codes start at 4.
        hex.add(type.ordinal() + 4);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "NEWARRAY " + type.toString().charAt(2));
    }

    @Override
    public String toString() {
        return "NEWARRAY " + type.toString().charAt(2);
    }
}
