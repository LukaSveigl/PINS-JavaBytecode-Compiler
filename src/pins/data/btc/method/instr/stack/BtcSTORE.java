package pins.data.btc.method.instr.stack;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The STORE instruction.
 * <p>
 * Stores the top value on the stack into a local variable.
 */
public class BtcSTORE extends BtcInstr {

    public enum Type {
        INT, LONG, FLOAT, DOUBLE, ARR
    }

    /** The index of the local. */
    public final int index;

    /** The type of the local. */
    public final Type type;

    /**
     * Constructs a new STORE instruction.
     *
     * @param instrIndex The index of the instruction.
     * @param localIndex The index of the local.
     * @param type       The type of the local.
     */
    public BtcSTORE(int instrIndex, int localIndex, Type type) {
        super(instrIndex);
        this.index = localIndex;
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("ISTORE");
            case LONG -> BtcInstr.opcodes.get("LSTORE");
            case FLOAT -> BtcInstr.opcodes.get("FSTORE");
            case DOUBLE -> BtcInstr.opcodes.get("DSTORE");
            case ARR -> BtcInstr.opcodes.get("ASTORE");
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
