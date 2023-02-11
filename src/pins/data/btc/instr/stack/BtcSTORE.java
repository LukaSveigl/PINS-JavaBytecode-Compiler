package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The STORE instruction.
 *
 * Stores the top value on the stack into a local variable.
 */
public class BtcSTORE extends BtcInstr {

    /** The STORE instruction kind. */
    public enum Kind {
        ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
    }

    /** The STORE instruction index (points to local). */
    public final int index;

    /** The STORE instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new STORE instruction.
     *
     * @param index The STORE instruction index.
     * @param kind  The STORE instruction kind.
     */
    public BtcSTORE(Kind kind, int index) {
        this.kind = kind;
        this.index = index;
        this.opcode = switch (kind) {
            case ISTORE -> 0x36;
            case LSTORE -> 0x37;
            case FSTORE -> 0x38;
            case DSTORE -> 0x39;
            case ASTORE -> 0x3a;
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
        System.out.println(pfx + kind + " " + index);
    }

    @Override
    public String toString() {
        return kind.toString() + " " + index;
    }

}
