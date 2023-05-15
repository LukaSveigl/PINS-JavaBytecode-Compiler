package pins.data.btc.method.instr.stack;

import pins.common.report.Report;
import pins.data.btc.method.instr.BtcInstr;

/**
 * The constant instruction.
 * <p>
 * Pushes a constant value of type int, float, long, double or Object onto the stack.
 */
public class BtcCONST extends BtcInstr {

    public enum Type {
        INT, LONG, FLOAT, DOUBLE, ARR, VOID
    }

    /** The constant value. */
    public final long value;

    /** The constant type. */
    public final Type type;

    /**
     * Constructs a new constant instruction.
     *
     * @param index The index of the instruction.
     * @param value The constant value.
     * @param type  The constant type.
     */
    public BtcCONST(int index, long value, Type type) {
        super(index);
        this.value = value;
        this.type = type;

        switch (type) {
            case INT -> {
                if (value == -1) {
                    this.opcode = BtcInstr.opcodes.get("ICONST_M1");
                } else if (value > -1 && value <= 5) {
                    this.opcode = BtcInstr.opcodes.get("ICONST_" + value);
                } else {
                    throw new Report.InternalError();
                }
            }
            case LONG -> {
                if (value >= 0 && value <= 1) {
                    this.opcode = BtcInstr.opcodes.get("LCONST_" + value);
                } else {
                    throw new Report.InternalError();
                }
            }
            case FLOAT -> {
                if (value >= 0 && value <= 2) {
                    this.opcode = BtcInstr.opcodes.get("FCONST_" + value);
                } else {
                    throw new Report.InternalError();
                }
            }
            case DOUBLE -> {
                if (value >= 0 && value <= 2) {
                    this.opcode = BtcInstr.opcodes.get("DCONST_" + value);
                } else {
                    throw new Report.InternalError();
                }
            }
            case VOID -> {
                this.opcode = BtcInstr.opcodes.get("ACONST_NULL");
            }
        }
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + value + "]");
    }

}
