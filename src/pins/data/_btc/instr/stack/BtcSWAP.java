package pins.data._btc.instr.stack;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

/**
 * The SWAP instruction.
 *
 * Swaps the top two values on the stack. Must not be used unless the top two values are both values of
 * a category 1 computational type (not long or double).
 */
public class BtcSWAP extends BtcInstr {

    /**
     * Constructs a new SWAP instruction.
     */
    public BtcSWAP() {
        this.opcode = BtcInstr.opcodes.get("SWAP");
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
        return "SWAP[" + opcode + "]";
    }
}
