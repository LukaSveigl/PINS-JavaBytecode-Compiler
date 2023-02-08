package pins.data.btc.arithmetic;

import pins.common.report.Report;
import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The long/double/float compare instruction.
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
        LONG, DOUBLE, FLOAT
    }

    /** The long/double compare instruction operation. */
    public final Oper oper;

    /** The long/double compare instruction type. */
    public final Type type;

    /**
     * Constructs a new long/double/float compare instruction.
     *
     * @param oper The long/double/float compare instruction operation.
     * @param type The long/double/float compare instruction type.
     */
    public BtcCMP(Oper oper, Type type) {
        if (oper == Oper.CMP && (type == Type.DOUBLE || type == Type.FLOAT)) {
            throw new Report.InternalError();
        }
        if (oper != Oper.CMP && type == Type.LONG) {
            throw new Report.InternalError();
        }
        this.oper = oper;
        this.type = type;
        int opcode = 0x94; // lcmp
        opcode += switch (oper) {
            case CMP -> 0; // 0x94
            case CMPL -> 1; // 0x95 or 0x97
            case CMPG -> 2; // 0x96 or 0x98
        };
        opcode += switch (type) {
            case LONG, FLOAT -> 0;
            case DOUBLE -> 2;
        };
        this.opcode = opcode;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        return hex;
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
