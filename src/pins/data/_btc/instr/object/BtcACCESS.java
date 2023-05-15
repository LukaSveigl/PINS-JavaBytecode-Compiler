package pins.data._btc.instr.object;

import pins.data._btc.instr.BtcInstr;

import java.nio.ByteBuffer;

/**
 * The member access instruction.
 *
 * Loads a value from a class field.
 */
public class BtcACCESS extends BtcInstr {

    /** The access direction. */
    public enum Dir {
        GET, PUT
    }

    /** The access type. */
    public enum Type {
        STATIC, FIELD
    }

    /** The access direction. */
    public final Dir dir;

    /** The access type. */
    public final Type type;

    /** The field index in the constant pool. */
    public final int index;

    /**
     * Constructs a new member access instruction.
     *
     * @param dir   The access direction.
     * @param type  The access type.
     * @param index The field index in the constant pool.
     */
    public BtcACCESS(Dir dir, Type type, int index) {
        this.dir = dir;
        this.type = type;
        this.index = index;
        this.opcode = switch (dir) {
            case GET -> switch (type) {
                case STATIC -> BtcInstr.opcodes.get("GETSTATIC");
                case FIELD -> BtcInstr.opcodes.get("GETFIELD");
            };
            case PUT -> switch (type) {
                case STATIC -> BtcInstr.opcodes.get("PUTSTATIC");
                case FIELD -> BtcInstr.opcodes.get("PUTFIELD");
            };
        };
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put((byte) this.opcode);
        byteBuffer.putShort((short) index);
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 3;
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
