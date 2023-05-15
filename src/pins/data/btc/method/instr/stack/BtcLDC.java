package pins.data.btc.method.instr.stack;

import pins.common.report.Report;
import pins.data.btc.method.instr.BtcInstr;

/**
 * The LDC instruction.
 * <p>
 * Pushes an item from the run-time constant pool onto the stack.
 */
public class BtcLDC extends BtcInstr {

    public enum Type {
        DEFAULT, LONG
    }

    /** The value to be loaded onto the stack. */
    public final long value;

    /** The instruction type. */
    public final Type type;

    /**
     * Constructs a new LDC instruction.
     *
     * @param index The instruction index.
     * @param value The value to be loaded onto the stack.
     * @param type  The instruction type.
     */
    public BtcLDC(int index, long value, Type type) {
        super(index);
        if (type == Type.DEFAULT) {
            if (value > Integer.MAX_VALUE) {
                throw new Report.InternalError();
            }
        }

        this.value = value;
        this.type = type;
        // This will be adjusted in bytecode conversion, as we do not know the constant pool index now.
        if (type == BtcLDC.Type.DEFAULT) {
            if (value <= 0xff) {
                this.opcode = BtcInstr.opcodes.get("LDC");
            } else {
                this.opcode = BtcInstr.opcodes.get("LDC_W");
            }
        } else {
            this.opcode = BtcInstr.opcodes.get("LDC2_W");
        }
    }

    @Override
    public int size() {
        if (this.opcode == BtcInstr.opcodes.get("LDC")) {
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + value + "]");
    }

}
