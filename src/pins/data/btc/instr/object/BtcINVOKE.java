package pins.data.btc.instr.object;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The method invoke instruction.
 *
 * Invokes a method.
 */
public class BtcINVOKE extends BtcInstr {

    /** The type of the method. */
    public enum Type {
        VIRTUAL, SPECIAL, STATIC, INTERFACE, DYNAMIC
    }

    /** The type of the method. */
    public final Type type;

    /** The method index in the constant pool. */
    public final int index;

    /**
     * Constructs a new method invoke instruction.
     *
     * @param type  The type of the method.
     * @param index The method index in the constant pool.
     */
    public BtcINVOKE(Type type, int index) {
        this.type = type;
        this.index = index;
        this.opcode = switch (type) {
            case VIRTUAL -> BtcInstr.opcodes.get("INVOKEVIRTUAL");
            case SPECIAL -> BtcInstr.opcodes.get("INVOKESPECIAL");
            case STATIC -> BtcInstr.opcodes.get("INVOKESTATIC");
            case INTERFACE -> BtcInstr.opcodes.get("INVOKEINTERFACE");
            case DYNAMIC -> BtcInstr.opcodes.get("INVOKEDYNAMIC");
        };
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        //hex.add(index);
        hex.add(index >> 8);
        hex.add(index & 0xff);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "INVOKE" +  type + " " + index);
    }

    @Override
    public String toString() {
        return "INVOKE" + type + " " + index;
    }

}
