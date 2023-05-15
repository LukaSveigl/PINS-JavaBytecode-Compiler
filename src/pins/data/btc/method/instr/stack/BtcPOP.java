package pins.data.btc.method.instr.stack;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The POP instruction.
 * <p>
 * Pops the top value (or 2 values) from the stack.
 */
public class BtcPOP extends BtcInstr {

    public enum Kind {
        POP, POP2
    }

    /** The POP instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new POP instruction.
     *
     * @param index The index of the instruction.
     * @param kind  The POP instruction kind.
     */
    public BtcPOP(int index, Kind kind) {
        super(index);
        this.kind = kind;
        this.opcode = kind == Kind.POP ? BtcInstr.opcodes.get("POP") : BtcInstr.opcodes.get("POP2");
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
