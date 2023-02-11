package pins.data.btc.instr.arithmetic;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The bitwise instruction.
 *
 * Performs a bitwise operation on the top two values on the stack. The result is pushed back onto the stack.
 */
public class BtcBITWISE extends BtcInstr {

    /** The bitwise instruction operation. */
    public enum Oper {
        AND, OR, XOR, SHL, SHR, USHR
    }

    /** The bitwise instruction type. */
    public enum Type {
        INT, LONG
    }

    /** The bitwise instruction operation. */
    public final Oper oper;

    /** The bitwise instruction type. */
    public final Type type;

    /**
     * Constructs a new bitwise instruction.
     *
     * @param oper The bitwise instruction operation.
     * @param type The bitwise instruction type.
     */
    public BtcBITWISE(Oper oper, Type type) {
        this.oper = oper;
        this.type = type;
        int opcode = 0x78; // ishl
        opcode += switch (oper) {
            case SHL -> 0; // 0x78 - 0x79
            case SHR -> 2; // 0x7a - 0x7b
            case USHR -> 4; // 0x7c - 0x7d
            case AND -> 6; // 0x7e - 0x7f
            case OR -> 8; // 0x80 - 0x81
            case XOR -> 10; // 0x82 - 0x83
        };
        opcode += switch (type) {
            case INT -> 0;
            case LONG -> 1;
        };
        this.opcode = opcode;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + oper.toString());
    }

    @Override
    public String toString() {
        return type.toString().charAt(0) + oper.toString();
    }

}
