package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The byte/short PUSH instruction.
 *
 * Pushes a value of type byte or short onto the stack. Also used for characters.
 */
public class BtcPUSH extends BtcInstr {

    /** The constant push instruction type. */
    public enum Type {
        BYTE, SHORT
    }

    /** The constant push value. */
    public final long value;

    /** The constant push type. */
    public final Type type;

    /**
     * Constructs a new constant push instruction.
     *
     * @param value The constant push value.
     * @param type  The constant push type.
     */
    public BtcPUSH(long value, Type type) {
        this.value = value;
        this.type = type;
        this.opcode = switch (type) {
            case BYTE -> BtcInstr.opcodes.get("BIPUSH");
            case SHORT -> BtcInstr.opcodes.get("SIPUSH");
        };
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getBytecodeLength());
        byteBuffer.put((byte) opcode);
        if (type == Type.BYTE) {
            byteBuffer.put((byte) value);
        } else {
            byteBuffer.putShort((short) value);
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        if (type == Type.BYTE) {
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return type.toString().charAt(0) + "IPUSH[" + opcode + ", " + value + "]";
    }

}
