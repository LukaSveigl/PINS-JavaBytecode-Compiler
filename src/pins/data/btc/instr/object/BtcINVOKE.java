package pins.data.btc.instr.object;

import pins.common.report.Report;
import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The method invoke instruction.
 *
 * Invokes a method.
 */
public class BtcINVOKE extends BtcInstr {

    /** The type of the method. */
    public enum Type {
        VIRTUAL, SPECIAL, STATIC, INTERFACE, DYNAMIC
    }

    /** The type of the method. */
    public final Type type;

    /** The method index in the constant pool. */
    public final int index;

    /**
     * Constructs a new method invoke instruction.
     *
     * @param type  The type of the method.
     * @param index The method index in the constant pool.
     */
    public BtcINVOKE(Type type, int index) {
        this.type = type;
        this.index = index;
        this.opcode = switch (type) {
            case VIRTUAL -> BtcInstr.opcodes.get("INVOKEVIRTUAL");
            case SPECIAL -> BtcInstr.opcodes.get("INVOKESPECIAL");
            case STATIC -> BtcInstr.opcodes.get("INVOKESTATIC");
            case INTERFACE -> BtcInstr.opcodes.get("INVOKEINTERFACE");
            case DYNAMIC -> BtcInstr.opcodes.get("INVOKEDYNAMIC");
        };
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getBytecodeLength());
        byteBuffer.put((byte) this.opcode);
        byteBuffer.putShort((short) index);
        if (type == Type.INTERFACE) {
            byteBuffer.put((byte) 1);
            byteBuffer.put((byte) 0);
        } else if (type == Type.DYNAMIC) {
            byteBuffer.put((byte) 0);
            byteBuffer.put((byte) 0);
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        switch (type) {
            case VIRTUAL, SPECIAL, STATIC -> {
                return 3;
            }
            case INTERFACE, DYNAMIC -> {
                return 5;
            }
            default -> {
                throw new Report.InternalError();
            }
        }
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + index + "]";
    }

}
