package pins.data.btc.method.instr.object;

import pins.data.btc.method.instr.BtcInstr;
import pins.data.btc.var.BtcFIELD;

/**
 * The field access instruction.
 * <p>
 * Loads a value from a class field.
 */
public class BtcACCESS extends BtcInstr {

    public enum Dir {
        GET, PUT
    }

    public enum Type {
        STATIC, FIELD
    }

    /** The access direction. */
    public final Dir dir;

    /** The access type. */
    public final Type type;

    /** The field. */
    public final BtcFIELD field;

    /**
     * Constructs a new field access instruction.
     *
     * @param index The instruction index.
     * @param dir   The access direction.
     * @param field The field.
     */
    public BtcACCESS(int index, Dir dir, BtcFIELD field) {
        super(index);
        this.dir = dir;

        Type accessType = null;

        for (BtcFIELD.Flags flag : field.flags()) {
            if (flag == BtcFIELD.Flags.STATIC) {
                accessType = Type.STATIC;
                break;
            }
        }
        if (accessType == null) {
            accessType = Type.FIELD;
        }

        this.type = accessType;
        this.field = field;
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
    public int size() {
        return 3;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + "]");
        field.log(pfx + "    ");
    }

}
