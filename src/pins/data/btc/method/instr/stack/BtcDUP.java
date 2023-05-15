package pins.data.btc.method.instr.stack;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The DUP instruction.
 * <p>
 * Duplicates the top value on the stack. Must not be used unless the top value is a value of a category 1 computational
 * type (not long or double).
 */
public class BtcDUP extends BtcInstr {

    public enum Kind {
        DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2
    }

    /** The DUP instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new DUP instruction.
     *
     * @param index The index of the instruction.
     * @param kind  The DUP instruction kind.
     */
    public BtcDUP(int index, Kind kind) {
        super(index);
        this.kind = kind;
        this.opcode = switch (kind) {
            case DUP -> BtcInstr.opcodes.get("DUP");
            case DUP_X1 -> BtcInstr.opcodes.get("DUP_X1");
            case DUP_X2 -> BtcInstr.opcodes.get("DUP_X2");
            case DUP2 -> BtcInstr.opcodes.get("DUP2");
            case DUP2_X1 -> BtcInstr.opcodes.get("DUP2_X1");
            case DUP2_X2 -> BtcInstr.opcodes.get("DUP2_X2");
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
