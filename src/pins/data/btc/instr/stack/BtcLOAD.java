package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The LOAD instruction.
 *
 * Loads a value from a local variable.
 */
public class BtcLOAD extends BtcInstr {

    /** The LOAD instruction type. */
    public enum Type {
        ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
    }

    /** The LOAD instruction index (points to local). */
    public final int index;

    /** The LOAD instruction type. */
    public final Type type;

    /**
     * Constructs a new LOAD instruction.
     *
     * @param index The LOAD instruction index.
     * @param type  The LOAD instruction type.
     */
    public BtcLOAD(int index, Type type) {
        this.index = index;
        this.type = type;
        this.opcode = switch (type) {
            case ILOAD -> BtcInstr.opcodes.get("ILOAD");
            case LLOAD -> BtcInstr.opcodes.get("LLOAD");
            case FLOAD -> BtcInstr.opcodes.get("FLOAD");
            case DLOAD -> BtcInstr.opcodes.get("DLOAD");
            case ALOAD -> BtcInstr.opcodes.get("ALOAD");
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        hex.add(index);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type + " " + index);
    }

    @Override
    public String toString() {
        return type + " " + index;
    }

}
