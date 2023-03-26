package pins.data.btc.instr.object;

import pins.data.btc.instr.BtcInstr;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The array load instruction.
 *
 * Loads a value from an array.
 */
public class BtcALOAD extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new array load instruction.
     *
     * @param type The type of the array.
     */
    public BtcALOAD(Type type) {
        this.type = type;
        this.opcode = switch (type) {
            case INT -> BtcInstr.opcodes.get("IALOAD");
            case LONG -> BtcInstr.opcodes.get("LALOAD");
            case FLOAT -> BtcInstr.opcodes.get("FALOAD");
            case DOUBLE -> BtcInstr.opcodes.get("DALOAD");
            case BYTE -> BtcInstr.opcodes.get("BALOAD");
            case CHAR -> BtcInstr.opcodes.get("CALOAD");
            case SHORT -> BtcInstr.opcodes.get("SALOAD");
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
        return type.toString().charAt(0) + "ALOAD[" + opcode + "]";
    }

}
