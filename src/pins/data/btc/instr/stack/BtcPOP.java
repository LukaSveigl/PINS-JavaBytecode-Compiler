package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The POP instruction.
 *
 * Pops the top value (or 2 values) from the stack.
 */
public class BtcPOP extends BtcInstr {

    /** The POP instruction kind. */
    public enum Kind {
        POP, POP2;
    }

    /** The POP instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new POP instruction.
     *
     * @param kind The POP instruction kind.
     */
    public BtcPOP(Kind kind) {
        this.kind = kind;
        this.opcode = kind == Kind.POP ? 0x57 : 0x58;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + kind);
    }

    @Override
    public String toString() {
        return kind.toString();
    }

}
