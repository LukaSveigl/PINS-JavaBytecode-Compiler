package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The SWAP instruction.
 */
public class BtcSWAP extends BtcInstr {

    /**
     * Constructs a new SWAP instruction.
     */
    public BtcSWAP() {
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
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
