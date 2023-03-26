package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The LOAD instruction.
 *
 * Loads a value from a local variable.
 */
public class BtcLOAD extends BtcInstr {

    /** The LOAD instruction type. */
    public enum Type {
        ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
    }

    /** The LOAD instruction index (points to local). */
    public final int index;

    /** The LOAD instruction type. */
    public final Type type;

    /**
     * Constructs a new LOAD instruction.
     *
     * @param index The LOAD instruction index.
     * @param type  The LOAD instruction type.
     */
    public BtcLOAD(int index, Type type) {
        this.index = index;
        this.type = type;
        this.opcode = switch (type) {
            case ILOAD -> BtcInstr.opcodes.get("ILOAD");
            case LLOAD -> BtcInstr.opcodes.get("LLOAD");
            case FLOAD -> BtcInstr.opcodes.get("FLOAD");
            case DLOAD -> BtcInstr.opcodes.get("DLOAD");
            case ALOAD -> BtcInstr.opcodes.get("ALOAD");
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
        return type + "[" + opcode + ", " + index + "]";
    }

}
