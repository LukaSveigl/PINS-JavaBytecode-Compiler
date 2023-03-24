package pins.data.btc.instr.flow;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The return instruction.
 *
 * Returns from the current method.
 */
public class BtcRETURN extends BtcInstr {

    /** The return type. */
    public enum Type {
        IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN
    }

    /** The return type. */
    public final Type type;

    /**
     * Constructs a new return instruction.
     *
     * @param type The return type.
     */
    public BtcRETURN(Type type) {
        this.type = type;
        this.opcode = switch (type) {
            case IRETURN -> BtcInstr.opcodes.get("IRETURN");
            case LRETURN -> BtcInstr.opcodes.get("LRETURN");
            case FRETURN -> BtcInstr.opcodes.get("FRETURN");
            case DRETURN -> BtcInstr.opcodes.get("DRETURN");
            case ARETURN -> BtcInstr.opcodes.get("ARETURN");
            case RETURN -> BtcInstr.opcodes.get("RETURN");
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
        System.out.println(pfx + type);
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
