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
            case INT -> 0x4f;
            case LONG -> 0x50;
            case FLOAT -> 0x51;
            case DOUBLE -> 0x52;
            case BYTE -> 0x54;
            case CHAR -> 0x55;
            case SHORT -> 0x56;
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
