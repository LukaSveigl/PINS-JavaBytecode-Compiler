package pins.data._btc.instr.flow;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

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
    public int target;

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
            case EQ -> BtcInstr.opcodes.get("IFEQ");
            case NE -> BtcInstr.opcodes.get("IFNE");
            case LT -> BtcInstr.opcodes.get("IFLT");
            case GE -> BtcInstr.opcodes.get("IFGE");
            case GT -> BtcInstr.opcodes.get("IFGT");
            case LE -> BtcInstr.opcodes.get("IFLE");
        };
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put((byte) this.opcode);
        byteBuffer.putShort((short) target);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 3;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return "IF" + oper.toString() + "[" + opcode + ", " + target + "]";
    }

}
