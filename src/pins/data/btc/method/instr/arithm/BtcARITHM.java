package pins.data.btc.method.instr.arithm;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The arithmetic instruction.
 *
 * Performs an arithmetic operation on the top two values on the stack (or 1 in case of NEG).
 * The result is pushed back onto the stack.
 */
public class BtcARITHM extends BtcInstr {

    public enum Oper {
        ADD, SUB, MUL, DIV, REM, NEG
    }

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
     * @param index The instruction index.
     * @param oper  The arithmetic instruction operation.
     * @param type  The arithmetic instruction type.
     */
    public BtcARITHM(int index, Oper oper, Type type) {
        super(index);
        this.oper = oper;
        this.type = type;
        this.opcode = BtcInstr.opcodes.get(type.toString().charAt(0) + oper.toString());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + "]");
    }

}
