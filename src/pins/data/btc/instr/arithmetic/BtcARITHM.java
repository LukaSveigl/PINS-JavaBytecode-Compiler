package pins.data.btc.instr.arithmetic;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The arithmetic instruction.
 *
 * Performs an arithmetic operation on the top two values on the stack (or 1 in case of NEG).
 * The result is pushed back onto the stack.
 */
public class BtcARITHM extends BtcInstr {

    /** The arithmetic instruction operation. */
    public enum Oper {
        ADD, SUB, MUL, DIV, REM, NEG
    }

    /** The arithmetic instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE
    }

    /** The arithmetic instruction operation. */
    public final Oper oper;

    /** The arithmetic instruction type. */
    public final Type type;

    /**
     * Constructs a new arithmetic instruction.
     *
     * @param oper The arithmetic instruction operation.
     * @param type The arithmetic instruction type.
     */
    public BtcARITHM(Oper oper, Type type) {
        this.oper = oper;
        this.type = type;
        int opcode = 0x60; // iadd
        opcode += switch (oper) {
            case ADD -> 0; // 0x60 - 0x63
            case SUB -> 4; // 0x64 - 0x67
            case MUL -> 8; // 0x68 - 0x6b
            case DIV -> 12; // 0x6c - 0x6f
            case REM -> 16; // 0x70 - 0x73
            case NEG -> 20; // 0x74 - 0x77
        };
        opcode += switch (type) {
            case INT -> 0;
            case LONG -> 1;
            case FLOAT -> 2;
            case DOUBLE -> 3;
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
