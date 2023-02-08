package pins.data.btc.object;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The array store instruction.
 *
 * Stores a value into an array.
 */
public class BtcASTORE extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array store instruction.
     *
     * @param type The type of the array.
     */
    public BtcASTORE(Type type) {
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + "ASTORE");
    }

    @Override
    public String toString() {
        return type.toString().charAt(0) + "ASTORE";
    }

}
