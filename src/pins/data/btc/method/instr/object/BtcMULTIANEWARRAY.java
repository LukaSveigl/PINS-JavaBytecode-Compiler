package pins.data.btc.method.instr.object;

import pins.data.btc.method.instr.BtcInstr;

/**
 * A multi array instruction.
 * <p>
 * Creates a new multi-dimensional array. The sizes of the dimensions are popped from the stack. The array reference is
 * pushed back.
 */
public class BtcMULTIANEWARRAY extends BtcInstr {

    /** The array dimensions type. */
    public final int dimensions;

    // TODO: Verify if this is correct.

    /**
     * Constructs a new MULTIANEWARRAY instruction.
     *
     * @param index      The instruction index.
     * @param dimensions The array dimensions type.
     */
    public BtcMULTIANEWARRAY(int index, int dimensions) {
        super(index);
        this.dimensions = dimensions;
        this.opcode = BtcInstr.opcodes.get("MULTIANEWARRAY");
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + dimensions + "]");
    }

}
