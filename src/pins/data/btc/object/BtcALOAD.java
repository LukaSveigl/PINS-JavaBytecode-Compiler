package pins.data.btc.object;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The array load instruction.
 *
 * Loads a value from an array.
 */
public class BtcALOAD extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array load instruction.
     *
     * @param type The type of the array.
     */
    public BtcALOAD(Type type) {
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + "ALOAD");
    }

    @Override
    public String toString() {
        return type.toString().charAt(0) + "ALOAD";
    }

}
