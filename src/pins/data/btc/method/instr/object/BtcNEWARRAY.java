package pins.data.btc.method.instr.object;

import pins.data.btc.method.instr.BtcInstr;

/**
 * A new array instruction.
 * <p>
 * Creates a new array. The size of the array is popped from the stack. The array reference is pushed back.
 */
public class BtcNEWARRAY extends BtcInstr {

    public enum Type {
        BOOLEAN, CHAR, FLOAT, DOUBLE, BYTE, SHORT, INT, LONG, REF
    }

    /** The array element type. */
    public final Type type;

    /**
     * Constructs a new NEWARRAY instruction.
     *
     * @param index The instruction index.a
     * @param type  The array element type.
     */
    public BtcNEWARRAY(int index, Type type) {
        super(index);
        this.type = type;
        this.opcode = switch (type) {
            case BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE -> BtcInstr.opcodes.get("NEWARRAY");
            case REF -> BtcInstr.opcodes.get("ANEWARRAY");
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + type + "]");
    }

}
