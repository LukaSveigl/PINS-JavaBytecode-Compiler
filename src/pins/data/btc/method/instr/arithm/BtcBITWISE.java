package pins.data.btc.method.instr.arithm;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The bitwise instruction.
 *
 * Performs a bitwise operation on the top two values on the stack. The result is pushed back onto the stack.
 */
public class BtcBITWISE extends BtcInstr {

    public enum Oper {
        AND, OR, XOR, SHL, SHR, USHR
    }

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
     * @param index The instruction index.
     * @param oper  The bitwise instruction operation.
     * @param type  The bitwise instruction type.
     */
    public BtcBITWISE(int index, Oper oper, Type type) {
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
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + index + "]");
    }

}
