package pins.data.btc.instr.object;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The array store instruction.
 *
 * Stores a value into an array.
 */
public class BtcASTORE extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array store instruction.
     *
     * @param type The type of the array.
     */
    public BtcASTORE(Type type) {
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("IASTORE");
            case LONG -> BtcInstr.opcodes.get("LASTORE");
            case FLOAT -> BtcInstr.opcodes.get("FASTORE");
            case DOUBLE -> BtcInstr.opcodes.get("DASTORE");
            case BYTE -> BtcInstr.opcodes.get("BASTORE");
            case CHAR -> BtcInstr.opcodes.get("CASTORE");
            case SHORT -> BtcInstr.opcodes.get("SASTORE");
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + "ASTORE");
    }

    @Override
    public String toString() {
        return type.toString().charAt(0) + "ASTORE";
    }

}
