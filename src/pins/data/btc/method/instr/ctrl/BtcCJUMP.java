package pins.data.btc.method.instr.ctrl;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The conditional jump instruction.
 * <p>
 * Jumps to the specified target if the condition is true. The condition is popped.
 */
public class BtcCJUMP extends BtcInstr {

    /** The jump condition. */
    public enum Oper {
        EQ, NE, LT, LE, GT, GE
    }

    /** The jump condition. */
    public final Oper oper;

    /** The jump target represented as a method line number. */
    public int target;

    /**
     * Constructs a new conditional jump instruction.
     *
     * @param index The instruction index.
     * @param oper  The jump condition.
     */
    public BtcCJUMP(int index, Oper oper) {
        super(index);
        this.oper = oper;
        this.opcode = switch (oper) {
            case EQ -> BtcInstr.opcodes.get("IFEQ");
            case NE -> BtcInstr.opcodes.get("IFNE");
            case LT -> BtcInstr.opcodes.get("IFLT");
            case GE -> BtcInstr.opcodes.get("IFGE");
            case GT -> BtcInstr.opcodes.get("IFGT");
            case LE -> BtcInstr.opcodes.get("IFLE");
        };
    }

    /**
     * Sets the jump target.
     *
     * @param target The jump target represented as a method line number.
     */
    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + target + "]");
    }

}
