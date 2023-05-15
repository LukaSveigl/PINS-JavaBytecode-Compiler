package pins.data.btc.method.instr.arithm;

import pins.common.report.Report;
import pins.data.btc.method.instr.BtcInstr;

/**
 * The cast instruction.
 *
 * Casts a value on top of the stack to a different type.
 */
public class BtcCAST extends BtcInstr {

    public enum Type {
        INT, LONG, FLOAT, DOUBLE, BYTE, CHAR, SHORT
    }

    /** The cast instruction source type. */
    public final Type from;

    /** The cast instruction destination type. */
    public final Type to;

    public BtcCAST(int index, Type from, Type to) {
        super(index);
        if (!validateTypes(from, to)) {
            throw new Report.InternalError();
        }
        this.from = from;
        this.to = to;
        this.opcode = BtcInstr.opcodes.get(from.toString().charAt(0) + "2" + to.toString().charAt(0));
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
            if (!(to == Type.INT)) {
                return true;
            }
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

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + from.toString().charAt(0) + "2" + to.toString().charAt(0) + "[" + opcode + ", " + index + "]");
    }

}
