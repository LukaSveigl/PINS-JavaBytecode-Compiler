package pins.data.btc.method.instr.arithm;

import pins.common.report.Report;
import pins.data.btc.method.instr.BtcInstr;

/**
 * The long/double/float compare instruction.
 *
 * Compares the top two values on the stack. The values must be of type float, long or double. The result of the
 * comparison is pushed back onto the stack as an int.
 */
public class BtcCMP extends BtcInstr {

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
    public BtcCMP(int index, Oper oper, Type type) {
        super(index);
        if (oper == Oper.CMP && (type == Type.DOUBLE || type == Type.FLOAT)) {
            throw new Report.InternalError();
        }
        if (oper != Oper.CMP && type == Type.LONG) {
            throw new Report.InternalError();
        }
        this.oper = oper;
        this.type = type;

        this.opcode = BtcInstr.opcodes.get(type.toString().charAt(0) + oper.toString());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + "]");
    }

}
