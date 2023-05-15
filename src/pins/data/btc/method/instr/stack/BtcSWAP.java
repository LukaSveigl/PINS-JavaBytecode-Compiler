package pins.data.btc.method.instr.stack;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The SWAP instruction.
 * <p>
 * Swaps the top two values on the stack. Must not be used unless the top two values are both values of a category 1
 * computational type (not long or double).
 */
public class BtcSWAP extends BtcInstr {

    /**
     * Constructs a new SWAP instruction.
     *
     * @param index The index of the instruction.
     */
    public BtcSWAP(int index) {
        super(index);
        this.opcode = BtcInstr.opcodes.get("SWAP");
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
