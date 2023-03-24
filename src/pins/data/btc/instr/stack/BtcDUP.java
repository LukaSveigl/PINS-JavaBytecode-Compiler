package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The DUP instruction.
 *
 * Duplicates the value on top of the stack.
 */
public class BtcDUP extends BtcInstr {

    /** The DUP instruction kind. */
    public enum Kind {
        DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2
    }

    /** The DUP instruction kind. */
    public final Kind kind;

    /**
     * Constructs a new DUP instruction.
     *
     * @param kind The DUP instruction kind.
     */
    public BtcDUP(Kind kind) {
        this.kind = kind;
        this.opcode = switch (kind) {
            case DUP -> BtcInstr.opcodes.get("DUP");
            case DUP_X1 -> BtcInstr.opcodes.get("DUP_X1");
            case DUP_X2 -> BtcInstr.opcodes.get("DUP_X2");
            case DUP2 -> BtcInstr.opcodes.get("DUP2");
            case DUP2_X1 -> BtcInstr.opcodes.get("DUP2_X1");
            case DUP2_X2 -> BtcInstr.opcodes.get("DUP2_X2");
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
        System.out.println(pfx + kind);
    }

    @Override
    public String toString() {
        return kind.toString();
    }

}
