package pins.data.btc.instr.arithmetic;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;

/**
 * The arithmetic instruction.
 *
 * Performs an arithmetic operation on the top two values on the stack (or 1 in case of NEG).
 * The result is pushed back onto the stack.
 */
public class BtcARITHM extends BtcInstr {

    /** The arithmetic instruction operation. */
    public enum Oper {
        ADD, SUB, MUL, DIV, REM, NEG
    }

    /** The arithmetic instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE
    }

    /** The arithmetic instruction operation. */
    public final Oper oper;

    /** The arithmetic instruction type. */
    public final Type type;

    /**
     * Constructs a new arithmetic instruction.
     *
     * @param oper The arithmetic instruction operation.
     * @param type The arithmetic instruction type.
     */
    public BtcARITHM(Oper oper, Type type) {
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
