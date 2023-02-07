package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The DUP instruction.
 */
public class BtcDUP extends BtcInstr {

    /** The DUP instruction kind. */
    public enum Kind {
        DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2
    }

    /** The DUP instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new DUP instruction.
     *
     * @param kind The DUP instruction kind.
     */
    public BtcDUP(Kind kind) {
        this.kind = kind;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + kind);
    }

    @Override
    public String toString() {
        return kind.toString();
    }

}
