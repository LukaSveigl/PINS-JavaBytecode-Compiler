package pins.data.btc.method.instr.object;

import pins.data.btc.method.instr.BtcInstr;

/**
 * A new instruction.
 */
public class BtcNEW extends BtcInstr {

    /** The class name. */
    public final String className;

    /**
     * Constructs a new NEW instruction.
     *
     * @param index The instruction index.
     */
    public BtcNEW(int index, String className) {
        super(index);
        this.className = className;
        this.opcode = BtcInstr.opcodes.get("NEW");
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + className + "]");
    }
}
