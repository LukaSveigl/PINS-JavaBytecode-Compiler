package pins.data.btc.arithmetic;

import pins.common.report.Report;
import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The cast instruction.
 *
 * Casts a value on top of the stack to a different type.
 */
public class BtcCAST extends BtcInstr {

    /** The cast instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE, BYTE, CHAR, SHORT
    }

    /** The cast instruction source type. */
    public final Type from;

    /** The cast instruction destination type. */
    public final Type to;

    /**
     * Constructs a new cast instruction.
     *
     * @param from The cast instruction source type.
     * @param to   The cast instruction destination type.
     */
    public BtcCAST(Type from, Type to) {
        if (!this.validateTypes(from, to)) {
            throw new Report.InternalError();
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + from.toString().charAt(0) + "2" + to.toString().charAt(0));
    }

    @Override
    public String toString() {
        return from.toString().charAt(0) + "2" + to.toString().charAt(0);
    }

    /**
     * Validates the types of the cast instruction.
     *
     * @param from The type to cast from.
     * @param to   The type to cast to.
     * @return True if the types are valid, false otherwise.
     */
    private boolean validateTypes(Type from, Type to) {
        if (from == Type.INT) {
            return true;
        }
        if (from == Type.LONG) {
            return to == Type.INT || to == Type.FLOAT || to == Type.DOUBLE;
        }
        if (from == Type.FLOAT) {
            return to == Type.INT || to == Type.LONG || to == Type.DOUBLE;
        }
        if (from == Type.DOUBLE) {
            return to == Type.INT || to == Type.LONG || to == Type.FLOAT;
        }
        // Might extend later, depending on the needs.
        return false;
    }

}
