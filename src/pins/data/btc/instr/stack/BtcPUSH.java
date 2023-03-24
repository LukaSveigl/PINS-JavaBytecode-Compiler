package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The byte/short PUSH instruction.
 *
 * Pushes a value of type byte or short onto the stack. Also used for characters.
 */
public class BtcPUSH extends BtcInstr {

    /** The constant push instruction type. */
    public enum Type {
        BYTE, SHORT
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
    public BtcPUSH(long value, Type type) {
        this.value = value;
        this.type = type;
        this.opcode = switch (type) {
            case BYTE -> BtcInstr.opcodes.get("BIPUSH");
            case SHORT -> BtcInstr.opcodes.get("SIPUSH");
        };
    }

    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        switch (type) {
            case BYTE -> {
                hex.add((int) value);
            }
            case SHORT -> {
                hex.add((int) value >> 8);
                hex.add((int) value & 0xff);
            }
        }
        return hex;
    }

    public void log(String pfx) {
        System.out.println(pfx + type.toString().charAt(0) + "IPUSH " + value);
    }

    public String toString() {
        return type.toString().charAt(0) + "IPUSH " + value;
    }

}
