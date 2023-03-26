package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
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
        this.opcode = kind == Kind.POP ? BtcInstr.opcodes.get("POP") : BtcInstr.opcodes.get("POP2");
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
