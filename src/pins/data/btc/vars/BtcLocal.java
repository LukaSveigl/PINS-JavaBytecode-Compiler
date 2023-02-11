package pins.data.btc.vars;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.util.Vector;

/**
 * A local variable (or parameter) of a bytecode method.
 */
public class BtcLocal extends BtcVar {

    /* The local variable type. */
    /*public enum Type {
        BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, REF
    }*/

    /** The local variable type. */
    public final Type type;

    /** The local variable index. */
    public final int index;

    /** The local variable value. */
    public long init;

    /**
     * Constructs a new local variable.
     *
     * @param type  The local variable type.
     * @param index The local variable index.
     */
    public BtcLocal(Type type, int index) {
        this.type = type;
        this.index = index;
    }

    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    public void log(String pfx) {
        System.out.println(pfx + "BtcLocal: " + type + " " + index + " " + init);
    }

    public String toString() {
        return type + " " + index + " " + init;
    }

}
