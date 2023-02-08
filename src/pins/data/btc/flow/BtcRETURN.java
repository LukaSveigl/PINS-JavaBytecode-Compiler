package pins.data.btc.flow;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The return instruction.
 */
public class BtcRETURN extends BtcInstr {

    /** The return type. */
    public enum Type {
        IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN
    }

    /** The return type. */
    public final Type type;

    /**
     * Constructs a new return instruction.
     *
     * @param type The return type.
     */
    public BtcRETURN(Type type) {
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type);
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
