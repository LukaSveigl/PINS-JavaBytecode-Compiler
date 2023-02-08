package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The STORE instruction.
 *
 * Stores the top value on the stack into a local variable.
 */
public class BtcSTORE extends BtcInstr {

    /** The STORE instruction kind. */
    public enum Kind {
        ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
    }

    /** The STORE instruction index (points to local). */
    public final int index;

    /** The STORE instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new STORE instruction.
     *
     * @param index The STORE instruction index.
     * @param kind  The STORE instruction kind.
     */
    public BtcSTORE(int index, Kind kind) {
        this.index = index;
        this.kind = kind;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + kind + " " + index);
    }

    @Override
    public String toString() {
        return kind.toString() + " " + index;
    }

}
