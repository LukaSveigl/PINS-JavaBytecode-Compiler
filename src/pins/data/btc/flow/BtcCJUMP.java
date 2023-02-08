package pins.data.btc.flow;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The integer conditional jump instruction.
 *
 * Jumps to the specified instruction if the top value on the stack is 0.
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
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
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
