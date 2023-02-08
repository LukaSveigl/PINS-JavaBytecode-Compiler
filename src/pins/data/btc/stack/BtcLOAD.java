package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

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
            case ILOAD -> 0x15;
            case LLOAD -> 0x16;
            case FLOAD -> 0x17;
            case DLOAD -> 0x18;
            case ALOAD -> 0x19;
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
