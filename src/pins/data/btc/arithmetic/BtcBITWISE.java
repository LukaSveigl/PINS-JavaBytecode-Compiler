package pins.data.btc.arithmetic;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The bitwise instruction.
 *
 * Performs a bitwise operation on the top two values on the stack. The result is pushed back onto the stack.
 */
public class BtcBITWISE extends BtcInstr {

    /** The bitwise instruction operation. */
    public enum Oper {
        AND, OR, XOR, NOT, SHL, SHR, USHL, USHR
    }

    /** The bitwise instruction type. */
    public enum Type {
        INT, LONG
    }

    /** The bitwise instruction operation. */
    public final Oper oper;

    /** The bitwise instruction type. */
    public final Type type;

    /**
     * Constructs a new bitwise instruction.
     *
     * @param oper The bitwise instruction operation.
     * @param type The bitwise instruction type.
     */
    public BtcBITWISE(Oper oper, Type type) {
        this.oper = oper;
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + oper.toString());
    }

    @Override
    public String toString() {
        return type.toString().charAt(0) + oper.toString();
    }

}
