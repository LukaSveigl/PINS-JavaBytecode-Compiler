package pins.data.btc.object;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The new array instruction.
 */
public class BtcNEWARRAY extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        T_BOOLEAN, T_CHAR, T_FLOAT, T_DOUBLE, T_BYTE, T_SHORT, T_INT, T_LONG
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new new array instruction.
     *
     * @param type The type of the array.
     */
    public BtcNEWARRAY(Type type) {
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "NEWARRAY " + type.toString().charAt(2));
    }

    @Override
    public String toString() {
        return "NEWARRAY " + type.toString().charAt(2);
    }
}
