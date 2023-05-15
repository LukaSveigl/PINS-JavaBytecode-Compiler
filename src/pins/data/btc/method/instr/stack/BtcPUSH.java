package pins.data.btc.method.instr.stack;

import pins.common.report.Report;
import pins.data.btc.method.instr.BtcInstr;

/**
 * The byte/short PUSH instruction.
 * <p>
 * Pushes a value of type byte or short onto the stack. Also used for characters.
 */
public class BtcPUSH extends BtcInstr {

    public enum Type {
        BYTE, SHORT
    }

    /** The value to push. */
    public final int value;

    /** The type of the push instruction. */
    public final Type type;

    /**
     * Constructs a new constant push instruction.
     *
     * @param index The index of the instruction.
     * @param value The value to push.
     * @param type  The type of the push instruction.
     */
    public BtcPUSH(int index, int value, Type type) {
        super(index);

        if (type == Type.BYTE) {
            if (value > Byte.MAX_VALUE) {
                throw new Report.InternalError();
            }
        } else if (type == Type.SHORT) {
            if (value > Short.MAX_VALUE) {
                throw new Report.InternalError();
            }
        }

        this.value = value;
        this.type = type;
        this.opcode = type == Type.BYTE ? BtcInstr.opcodes.get("BIPUSH") : BtcInstr.opcodes.get("SIPUSH");
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
