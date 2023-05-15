package pins.data.btc.method.instr.ctrl;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The return instruction.
 *
 * If type is not void, it pops the return value from the stack.
 */
public class BtcRETURN extends BtcInstr {

    public enum Type {
        INT, FLOAT, LONG, DOUBLE, REFERENCE, VOID
    }

    /** The return type. */
    public final Type type;

    /**
     * Constructs a new return instruction.
     *
     * @param index The instruction index.
     * @param type  The return type.
     */
    public BtcRETURN(int index, Type type) {
        super(index);
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("IRETURN");
            case FLOAT -> BtcInstr.opcodes.get("FRETURN");
            case LONG -> BtcInstr.opcodes.get("LRETURN");
            case DOUBLE -> BtcInstr.opcodes.get("DRETURN");
            case REFERENCE -> BtcInstr.opcodes.get("ARETURN");
            case VOID -> BtcInstr.opcodes.get("RETURN");
        };
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
