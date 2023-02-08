package pins.data.btc.object;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The member access instruction.
 *
 * Loads a value from a class field.
 */
public class BtcACCESS extends BtcInstr {

    /** The access direction. */
    public enum Dir {
        GET, PUT
    }

    /** The access type. */
    public enum Type {
        STATIC, FIELD
    }

    /** The access direction. */
    public final Dir dir;

    /** The access type. */
    public final Type type;

    /** The field index in the constant pool. */
    public final int index;

    /**
     * Constructs a new member access instruction.
     *
     * @param dir   The access direction.
     * @param type  The access type.
     * @param index The field index in the constant pool.
     */
    public BtcACCESS(Dir dir, Type type, int index) {
        this.dir = dir;
        this.type = type;
        this.index = index;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + dir + "" + type + " " + index);
    }

    @Override
    public String toString() {
        return dir + "" + type + " " + index;
    }

}
