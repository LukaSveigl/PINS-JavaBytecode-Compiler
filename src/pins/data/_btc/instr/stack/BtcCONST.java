package pins.data._btc.instr.stack;

import pins.common.report.Report;
import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

/**
 * The constant instruction.
 *
 * Pushes a constant value of type int, float, long, double or Object onto the stack.
 */
public class BtcCONST extends BtcInstr {

    /** The constant push instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE, ARR, VOID
    }

    /** The constant push value. */
    public final long value;

    /** The constant push type. */
    public final Type type;

    /**
     * Constructs a new constant push instruction.
     *
     * @param value The constant push value.
     * @param type  The constant push type.
     */
    public BtcCONST(long value, Type type) {
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
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        byteBuffer.put((byte) opcode);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 1;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        // TODO: Figure out what the fuck is going on here.
        return BtcInstr.getInstructionFromOpcode(this.opcode) + "[" + opcode + "]";
    }

}
