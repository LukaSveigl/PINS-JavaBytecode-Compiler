package pins.data.btc.instr.stack;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The LDC instruction.
 *
 * Pushes an item from the run-time constant pool onto the stack.
 */
public class BtcLDC extends BtcInstr {

    /** The item run-time constant pool index. */
    public final int index;

    /**
     * Constructs a new LDC instruction.
     *
     * @param index The item run-time constant pool index.
     */
    public BtcLDC(int index) {
        this.index = index;
        // Choose the opcode based on the index value. If the index is in the range 0-255, use the
        // LDC instruction. Otherwise, use the LDC_W instruction.
        if (index <= 0xff) {
            this.opcode = 0x12;
        } else {
            this.opcode = 0x13;
        }
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        if (opcode == 0x13) {
            hex.add(index >> 8);
        }
        hex.add(index & 0xff);

        // hex.add(index);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + this);
    }

    @Override
    public String toString() {
        if (opcode == 0x12) {
            return "LDC " + index;
        } else {
            return "LDC_W " + index;
        }
    }

}
