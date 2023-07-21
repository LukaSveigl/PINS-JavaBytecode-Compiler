package pins.data.btc.method.instr.object;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The array store instruction.
 *
 * Stores a value into an array.
 */
public class BtcASTORE extends BtcInstr {

    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, REF
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array store instruction.
     *
     * @param index The index of the instruction.
     * @param type  The type of the array.
     */
    public BtcASTORE(int index, Type type) {
        super(index);
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("IASTORE");
            case LONG -> BtcInstr.opcodes.get("LASTORE");
            case FLOAT -> BtcInstr.opcodes.get("FASTORE");
            case DOUBLE -> BtcInstr.opcodes.get("DASTORE");
            case BYTE -> BtcInstr.opcodes.get("BASTORE");
            case CHAR -> BtcInstr.opcodes.get("CASTORE");
            case SHORT -> BtcInstr.opcodes.get("SASTORE");
            case REF -> BtcInstr.opcodes.get("ASTORE");
        };
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
