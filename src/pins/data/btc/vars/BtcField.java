package pins.data.btc.vars;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.util.Vector;

/**
 * A field of a bytecode class.
 */
public class BtcField extends BtcVar {

    /** The field name. */
    public final String name;

    /** The field type. */
    public final Type type;

    /** The field index in the constant pool. */
    public final int index;

    public int init = 0;

    public BtcField(String name, Type type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    public void log(String pfx) {
        System.out.println(pfx + "BtcField: " + name + " " + type + " " + index + " " + init);
    }

    public String toString() {
        return name + ": " + type + " " + index + " " + init;
    }


}
