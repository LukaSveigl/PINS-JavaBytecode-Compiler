package pins.data.btc.method.instr.stack;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The POP instruction.
 * <p>
 * Pops the top value (or 2 values) from the stack.
 */
public class BtcLOAD extends BtcInstr {

    public enum Type {
        INT, LONG, FLOAT, DOUBLE, ARR
    }

    /** The index of the local. */
    public final int index;

    /** The type of the local. */
    public final Type type;

    /**
     * Constructs a new LOAD instruction.
     *
     * @param instrIndex The index of the instruction.
     * @param localIndex The index of the local.
     * @param type       The type of the local.
     */
    public BtcLOAD(int instrIndex, int localIndex, Type type) {
        super(instrIndex);
        this.index = localIndex;
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("ILOAD");
            case LONG -> BtcInstr.opcodes.get("LLOAD");
            case FLOAT -> BtcInstr.opcodes.get("FLOAD");
            case DOUBLE -> BtcInstr.opcodes.get("DLOAD");
            case ARR -> BtcInstr.opcodes.get("ALOAD");
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + super.index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + index + "]");
    }

}
