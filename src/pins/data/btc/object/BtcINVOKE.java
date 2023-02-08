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
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
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
