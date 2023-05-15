package pins.data._btc.instr.flow;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

/**
 * The unconditional jump instruction.
 *
 * Jumps to the specified instruction.
 */
public class BtcGOTO extends BtcInstr {

    /** The jump target represented as a method instruction index. */
    public int target;

    /**
     * Constructs a new unconditional jump instruction. The size of the target determines the opcode (GOTO and GOTO_W).
     *
     * @param target The jump target represented as a method instruction index.
     */
    public BtcGOTO(int target) {
        this.target = target;
        if (target < 0xffff) {
            this.opcode = BtcInstr.opcodes.get("GOTO");
        } else {
            this.opcode = BtcInstr.opcodes.get("GOTO_W");
        }
        //this.opcode = 0xa7;
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getBytecodeLength());
        byteBuffer.put((byte) this.opcode);
        if (this.opcode == BtcInstr.opcodes.get("GOTO")) {
            byteBuffer.putShort((short) target);
        } else {
            byteBuffer.putInt(target);
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        if (this.opcode == BtcInstr.opcodes.get("GOTO")) {
            return 3;
        } else {
            return 5;
        }
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        return BtcInstr.getInstructionFromOpcode(this.opcode) + "[" + opcode + ", " + target + "]";
    }
}
