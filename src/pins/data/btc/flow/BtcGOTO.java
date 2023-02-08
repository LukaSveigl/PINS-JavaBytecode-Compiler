package pins.data.btc.flow;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The unconditional jump instruction.
 *
 * Jumps to the specified instruction.
 */
public class BtcGOTO extends BtcInstr {

    /** The jump target represented as a method line number. */
    public final int target;

    /**
     * Constructs a new unconditional jump instruction.
     *
     * @param target The jump target represented as a method line number.
     */
    public BtcGOTO(int target) {
        this.target = target;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
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
