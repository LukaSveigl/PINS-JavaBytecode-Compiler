package pins.data.btc.arithmetic;

import pins.common.report.Report;
import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The long/double compare instruction.
 *
 * Compares the top two values on the stack. The values must be of type long or double. The result of the
 * comparison is pushed back onto the stack as an int.
 */
public class BtcCMP extends BtcInstr {

    /** The long/double compare instruction operation. */
    public enum Oper {
        CMPL, CMPG, CMP
    }

    /** The long/double compare instruction type. */
    public enum Type {
        LONG, DOUBLE
    }

    /** The long/double compare instruction operation. */
    public final Oper oper;

    /** The long/double compare instruction type. */
    public final Type type;

    /**
     * Constructs a new long/double compare instruction.
     *
     * @param oper The long/double compare instruction operation.
     * @param type The long/double compare instruction type.
     */
    public BtcCMP(Oper oper, Type type) {
        if (oper == Oper.CMP && type == Type.DOUBLE) {
            throw new Report.InternalError();
        }
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
