package pins.data.btc.instr.arithmetic;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The bitwise instruction.
 *
 * Performs a bitwise operation on the top two values on the stack. The result is pushed back onto the stack.
 */
public class BtcBITWISE extends BtcInstr {

    /** The bitwise instruction operation. */
    public enum Oper {
        AND, OR, XOR, SHL, SHR, USHR
    }

    /** The bitwise instruction type. */
    public enum Type {
        INT, LONG
    }

    /** The bitwise instruction operation. */
    public final Oper oper;

    /** The bitwise instruction type. */
    public final Type type;

    /**
     * Constructs a new bitwise instruction.
     *
     * @param oper The bitwise instruction operation.
     * @param type The bitwise instruction type.
     */
    public BtcBITWISE(Oper oper, Type type) {
        this.oper = oper;
        this.type = type;
        this.opcode = BtcInstr.opcodes.get(type.toString().charAt(0) + oper.toString());
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
        return type.toString().charAt(0) + oper.toString() + "[" + opcode + "]";
    }

}
