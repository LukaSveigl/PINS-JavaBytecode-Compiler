package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The POP instruction.
 *
 * Pops the top value (or 2 values) from the stack.
 */
public class BtcPOP extends BtcInstr {

    /** The POP instruction kind. */
    public enum Kind {
        POP, POP2;
    }

    /** The POP instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new POP instruction.
     *
     * @param kind The POP instruction kind.
     */
    public BtcPOP(Kind kind) {
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
