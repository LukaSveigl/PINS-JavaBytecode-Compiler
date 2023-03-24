package pins.data.btc.instr.object;

import pins.data.btc.instr.BtcInstr;

import java.util.Vector;

/**
 * The new array instruction.
 *
 * Creates a new array. The size of the array is popped from the stack. The array reference is pushed back.
 */
public class BtcNEWARRAY extends BtcInstr {

    /** The type of the array. */
    public enum Type {
        T_BOOLEAN, T_CHAR, T_FLOAT, T_DOUBLE, T_BYTE, T_SHORT, T_INT, T_LONG, T_REF, T_MULTI
    }

    /** The type of the array. */
    public final Type type;

    /**
     * Constructs a new NEWARRAY instruction.
     *
     * @param type The type of the array.
     */
    public BtcNEWARRAY(Type type) {
        this.type = type;
        if (type == Type.T_REF) {
            this.opcode = BtcInstr.opcodes.get("ANEWARRAY");
        } else if (type == Type.T_MULTI) {
            this.opcode = BtcInstr.opcodes.get("MULTIANEWARRAY");
        } else {
            this.opcode = BtcInstr.opcodes.get("NEWARRAY");
        }
        //this.opcode = 0xbc;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        // The type codes start at 4.
        hex.add(type.ordinal() + 4);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "NEWARRAY " + type.toString().charAt(2));
    }

    @Override
    public String toString() {
        String instruction = BtcInstr.getInstructionFromOpcode(this.opcode);
        if (instruction.equals("NEWARRAY")) {
            return instruction + " " + type.toString().charAt(2);
        } else {
            return instruction;
        }
        //return "NEWARRAY " + type.toString().charAt(2);
    }
}
