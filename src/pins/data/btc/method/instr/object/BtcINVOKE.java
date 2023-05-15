package pins.data.btc.method.instr.object;

import pins.common.report.Report;
import pins.data.btc.method.instr.BtcInstr;

/**
 * The method invoke instruction.
 * <p>
 * Invokes a method.
 */
public class BtcINVOKE extends BtcInstr {

    public enum Type {
        VIRTUAL, SPECIAL, STATIC, INTERFACE, DYNAMIC
    }

    /** The type of the method. */
    public final Type type;

    /** The method name. */
    public final String name;

    /**
     * Constructs a new method invoke instruction.
     *
     * @param index The method index in the constant pool.
     * @param type  The type of the method.
     * @param name  The method name.
     */
    public BtcINVOKE(int index, Type type, String name) {
        super(index);
        this.type = type;
        this.name = name;
        this.opcode = switch (type) {
            case VIRTUAL -> BtcInstr.opcodes.get("INVOKEVIRTUAL");
            case SPECIAL -> BtcInstr.opcodes.get("INVOKESPECIAL");
            case STATIC -> BtcInstr.opcodes.get("INVOKESTATIC");
            case INTERFACE -> BtcInstr.opcodes.get("INVOKEINTERFACE");
            case DYNAMIC -> BtcInstr.opcodes.get("INVOKEDYNAMIC");
        };
    }

    @Override
    public int size() {
        switch (type) {
            case VIRTUAL, SPECIAL, STATIC -> {
                return 3;
            }
            case INTERFACE, DYNAMIC -> {
                return 5;
            }
            default -> {
                throw new Report.InternalError();
            }
        }
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + index + " " + BtcInstr.getInstructionFromOpcode(opcode) + "[" + opcode + ", " + name + "]");
    }

}
