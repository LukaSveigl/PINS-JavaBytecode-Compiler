package pins.data.btc.flow;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The integer conditional jump instruction.
 *
 * Jumps to the specified instruction if the top 2 values on the stack match the condition. Both values are popped.
 */
public class BtcCJUMP extends BtcInstr {

    /** The jump condition. */
    public enum Oper {
        EQ, NE, LT, LE, GT, GE
    }

    /** The jump condition. */
    public final Oper oper;

    /** The jump target represented as a method line number. */
    public final int target;

    /**
     * Constructs a new conditional jump instruction.
     *
     * @param oper   The jump condition.
     * @param target The jump target represented as a method line number.
     */
    public BtcCJUMP(Oper oper, int target) {
        this.oper = oper;
        this.target = target;
        this.opcode = switch (oper) {
            case EQ -> 0x9f;
            case NE -> 0xa0;
            case LT -> 0xa1;
            case GE -> 0xa2;
            case GT -> 0xa3;
            case LE -> 0xa4;
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        hex.add(target);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "IF_ICMP" + oper.toString() + " " + target);
    }

    @Override
    public String toString() {
        return "IF_ICMP" + oper.toString() + " " + target;
    }

}
