package pins.data.btc.flow;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The unconditional jump instruction.
 *
 * Jumps to the specified instruction.
 */
public class BtcGOTO extends BtcInstr {

    /** The jump target represented as a method instruction index. */
    public final int target;

    /**
     * Constructs a new unconditional jump instruction.
     *
     * @param target The jump target represented as a method instruction index.
     */
    public BtcGOTO(int target) {
        this.target = target;
        this.opcode = 0xa7;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        hex.add(target);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "GOTO " + target);
    }

    @Override
    public String toString() {
        return "GOTO " + target;
    }
}
