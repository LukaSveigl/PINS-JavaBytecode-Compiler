package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;

/**
 * The LDC instruction.
 *
 * Pushes an item from the run-time constant pool onto the stack.
 */
public class BtcLDC extends BtcInstr {

    /** The instruction type. */
    public enum Type {
        DEFAULT, LONG
    }

    /** The item run-time constant pool index. */
    public final int index;

    /** The instruction type. */
    public final Type type;

    /**
     * Constructs a new LDC instruction.
     *
     * @param index The item run-time constant pool index.
     */
    public BtcLDC(int index, Type type) {
        this.index = index;
        this.type = type;

        /*if (type == Type.DEFAULT) {
            // Choose the opcode based on the index value. If the index is in the range 0-255, use the
            // LDC instruction. Otherwise, use the LDC_W instruction.
            if (index <= 0xff) {
                this.opcode = 0x12; // LDC
            } else {
                this.opcode = 0x13; // LDC_W
            }
        }
        else {
            this.opcode = 0x14; // LDC2_W
        }*/

        if (type == Type.DEFAULT) {
            if (index <= 0xff) {
                this.opcode = BtcInstr.opcodes.get("LDC");
            } else {
                this.opcode = BtcInstr.opcodes.get("LDC_W");
            }
        } else {
            this.opcode = BtcInstr.opcodes.get("LDC2_W");
        }
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getBytecodeLength());
        byteBuffer.put((byte) opcode);
        if (this.opcode == BtcInstr.opcodes.get("LDC")) {
            byteBuffer.put((byte) index);
        } else {
            byteBuffer.putShort((short) index);
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        if (this.opcode == BtcInstr.opcodes.get("LDC")) {
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
        return BtcInstr.getInstructionFromOpcode(this.opcode) + "[" + opcode + ", " +  index + "]";
    }

}
