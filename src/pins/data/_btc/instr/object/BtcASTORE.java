package pins.data._btc.instr.object;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

/**
 * The array store instruction.
 *
 * Stores a value into an array.
 */
public class BtcASTORE extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array store instruction.
     *
     * @param type The type of the array.
     */
    public BtcASTORE(Type type) {
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("IASTORE");
            case LONG -> BtcInstr.opcodes.get("LASTORE");
            case FLOAT -> BtcInstr.opcodes.get("FASTORE");
            case DOUBLE -> BtcInstr.opcodes.get("DASTORE");
            case BYTE -> BtcInstr.opcodes.get("BASTORE");
            case CHAR -> BtcInstr.opcodes.get("CASTORE");
            case SHORT -> BtcInstr.opcodes.get("SASTORE");
        };
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
        return type.toString().charAt(0) + "ASTORE[" + opcode + "]";
    }

}
