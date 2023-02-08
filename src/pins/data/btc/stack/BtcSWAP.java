package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

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
        this.opcode = 0x5f;
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
