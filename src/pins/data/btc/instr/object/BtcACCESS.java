package pins.data.btc.instr.object;

import pins.data.btc.instr.BtcInstr;

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
        this.opcode = switch (dir) {
            case GET -> switch (type) {
                case STATIC -> 0xb2;
                case FIELD -> 0xb4;
            };
            case PUT -> switch (type) {
                case STATIC -> 0xb3;
                case FIELD -> 0xb5;
            };
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        hex.add(index >> 8);
        hex.add(index & 0xff);
        return hex;
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
