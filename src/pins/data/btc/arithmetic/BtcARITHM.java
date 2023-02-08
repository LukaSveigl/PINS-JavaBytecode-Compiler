package pins.data.btc.arithmetic;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The arithmetic instruction.
 */
public class BtcARITHM extends BtcInstr {

    /** The arithmetic instruction operation. */
    public enum Oper {
        ADD, SUB, MUL, DIV, REM, NEG
    }

    /** The arithmetic instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE
    }

    /** The arithmetic instruction operation. */
    public final Oper oper;

    /** The arithmetic instruction type. */
    public final Type type;

    /**
     * Constructs a new arithmetic instruction.
     *
     * @param oper The arithmetic instruction operation.
     * @param type The arithmetic instruction type.
     */
    public BtcARITHM(Oper oper, Type type) {
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
