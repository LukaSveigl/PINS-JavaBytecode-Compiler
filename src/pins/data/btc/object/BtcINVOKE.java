package pins.data.btc.object;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The method invoke instruction.
 *
 * Invokes a method.
 */
public class BtcINVOKE extends BtcInstr {

    /** The type of the method. */
    public enum Type {
        VIRTUAL, SPECIAL, STATIC, INTERFACE, DYNAMIC
    }

    /** The type of the method. */
    public final Type type;

    /** The method index in the constant pool. */
    public final int index;

    /**
     * Constructs a new method invoke instruction.
     *
     * @param type  The type of the method.
     * @param index The method index in the constant pool.
     */
    public BtcINVOKE(Type type, int index) {
        this.type = type;
        this.index = index;
        this.opcode = switch (type) {
            case VIRTUAL -> 0xb6;
            case SPECIAL -> 0xb7;
            case STATIC -> 0xb8;
            case INTERFACE -> 0xb9;
            case DYNAMIC -> 0xba;
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        hex.add(index);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "INVOKE" +  type + " " + index);
    }

    @Override
    public String toString() {
        return "INVOKE" + type + " " + index;
    }

}
