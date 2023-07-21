package pins.data.btc.method.instr.ctrl;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The unconditional jump instruction.
 *
 * Jumps to the specified instruction.
 */
public class BtcGOTO extends BtcInstr {

    /** The jump target represented as a method instruction index. */
    public int target;

    /**
     * Constructs a new unconditional jump instruction.
     *
     * @param index The instruction index.
     */
    public BtcGOTO(int index) {
        super(index);
        this.opcode = BtcInstr.opcodes.get("GOTO");
    }

    /**
     * Sets the jump target.
     *
     * @param target The jump target represented as a method instruction index.
     */
    public void setTarget(int target) {
        this.target = target;
        if (target < 0xffff) {
            this.opcode = BtcInstr.opcodes.get("GOTO");
        } else {
            this.opcode = BtcInstr.opcodes.get("GOTO_W");
        }
    }

    @Override
    public int size() {
        if (this.opcode == BtcInstr.opcodes.get("GOTO")) {
            return 3;
        } else {
            return 5;
        }
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + target + "]");
    }

}
