package pins.data._btc.instr.stack;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

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
            case ISTORE -> BtcInstr.opcodes.get("ISTORE");
            case LSTORE -> BtcInstr.opcodes.get("LSTORE");
            case FSTORE -> BtcInstr.opcodes.get("FSTORE");
            case DSTORE -> BtcInstr.opcodes.get("DSTORE");
            case ASTORE -> BtcInstr.opcodes.get("ASTORE");
        };
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.put((byte) opcode);
        byteBuffer.put((byte) index);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return kind.toString() + "[" + opcode + ", " + index + "]";
    }

}
