package pins.data._btc.instr.stack;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

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
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        byteBuffer.put((byte) opcode);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 1;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return kind.toString() + "[" + opcode + "]";
    }

}
