package pins.data.btc.flow;

import pins.data.btc.BtcInstr;

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
            case IRETURN -> 0xac;
            case LRETURN -> 0xad;
            case FRETURN -> 0xae;
            case DRETURN -> 0xaf;
            case ARETURN -> 0xb0;
            case RETURN -> 0xb1;
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
