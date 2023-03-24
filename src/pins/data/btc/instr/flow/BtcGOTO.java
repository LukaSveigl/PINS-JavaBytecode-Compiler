package pins.data.btc.instr.flow;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The unconditional jump instruction.
 *
 * Jumps to the specified instruction.
 */
public class BtcGOTO extends BtcInstr {

    /** The jump target represented as a method instruction index. */
    public int target;

    /**
     * Constructs a new unconditional jump instruction. The size of the target determines the opcode (GOTO and GOTO_W).
     *
     * @param target The jump target represented as a method instruction index.
     */
    public BtcGOTO(int target) {
        this.target = target;
        if (target < 0xffff) {
            this.opcode = BtcInstr.opcodes.get("GOTO");
        } else { // GOTO_W
            this.opcode = BtcInstr.opcodes.get("GOTO_W");
        }
        //this.opcode = 0xa7;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        if (opcode == 0xc8) {
            hex.add(target >> 24);
            hex.add(target >> 16 & 0xff);
        }
        hex.add(target >> 8 & 0xff);
        hex.add(target & 0xff);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return BtcInstr.getInstructionFromOpcode(this.opcode) + " " + target;
    }
}
