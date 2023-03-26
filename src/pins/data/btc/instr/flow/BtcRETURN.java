package pins.data.btc.instr.flow;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
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
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        byteBuffer.put((byte) this.opcode);
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
        return type.toString() + "[" + opcode + "]";
    }

}
