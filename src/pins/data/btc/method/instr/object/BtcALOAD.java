package pins.data.btc.method.instr.object;

import pins.data.btc.method.instr.BtcInstr;

/**
 * The array load instruction.
 *
 * Loads a value from an array.
 */
public class BtcALOAD extends BtcInstr {

    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, REF
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array load instruction.
     *
     * @param index The index of the instruction.
     * @param type  The type of the array.
     */
    public BtcALOAD(int index, Type type) {
        super(index);
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("IALOAD");
            case LONG -> BtcInstr.opcodes.get("LALOAD");
            case FLOAT -> BtcInstr.opcodes.get("FALOAD");
            case DOUBLE -> BtcInstr.opcodes.get("DALOAD");
            case BYTE -> BtcInstr.opcodes.get("BALOAD");
            case CHAR -> BtcInstr.opcodes.get("CALOAD");
            case SHORT -> BtcInstr.opcodes.get("SALOAD");
            case REF -> BtcInstr.opcodes.get("AALOAD");
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
