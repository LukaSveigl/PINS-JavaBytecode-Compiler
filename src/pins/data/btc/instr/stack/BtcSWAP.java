package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The SWAP instruction.
 *
 * Swaps the top two values on the stack. Must not be used unless the top two values are both values of
 * a category 1 computational type (not long or double).
 */
public class BtcSWAP extends BtcInstr {

    /**
     * Constructs a new SWAP instruction.
     */
    public BtcSWAP() {
        this.opcode = BtcInstr.opcodes.get("SWAP");
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "SWAP");
    }

    @Override
    public String toString() {
        return "SWAP";
    }
}
