package pins.data.btc.instr.arithmetic;

import pins.common.report.Report;
import pins.data.btc.instr.BtcInstr;

import java.util.HashMap;
import java.util.Vector;

/**
 * The cast instruction.
 *
 * Casts a value on top of the stack to a different type.
 */
public class BtcCAST extends BtcInstr {

    /** The cast instruction type. */
    public enum Type {
        INT, LONG, FLOAT, DOUBLE, BYTE, CHAR, SHORT
    }

    /** The cast instruction source type. */
    public final Type from;

    /** The cast instruction destination type. */
    public final Type to;

    /** The opcode lookup table. */
    private final HashMap<Type[], Integer> opcodes = new HashMap<Type[], Integer>();

    /**
     * Constructs a new cast instruction.
     *
     * @param from The cast instruction source type.
     * @param to   The cast instruction destination type.
     */
    public BtcCAST(Type from, Type to) {
        if (!validateTypes(from, to)) {
            throw new Report.InternalError();
        }
        this.from = from;
        this.to = to;
        populateOpcodeLookup();
        // Could be done with a switch expression, but that would be too long.
        //this.opcode = opcodes.get(new Type[]{from, to});
        this.opcode = BtcInstr.opcodes.get(from.toString().charAt(0) + "2" + to.toString().charAt(0));
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.add(opcode);
        return hex;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + from.toString().charAt(0) + "2" + to.toString().charAt(0));
    }

    @Override
    public String toString() {
        return from.toString().charAt(0) + "2" + to.toString().charAt(0);
    }

    /**
     * Validates the types of the cast instruction.
     *
     * @param from The type to cast from.
     * @param to   The type to cast to.
     * @return True if the types are valid, false otherwise.
     */
    private boolean validateTypes(Type from, Type to) {
        if (from == Type.INT) {
            if (!(to == Type.INT)) {
                return true;
            }
        }
        if (from == Type.LONG) {
            return to == Type.INT || to == Type.FLOAT || to == Type.DOUBLE;
        }
        if (from == Type.FLOAT) {
            return to == Type.INT || to == Type.LONG || to == Type.DOUBLE;
        }
        if (from == Type.DOUBLE) {
            return to == Type.INT || to == Type.LONG || to == Type.FLOAT;
        }
        // Might extend later, depending on the needs.
        return false;
    }

    /**
     * Populates the opcode lookup table.
     */
    private void populateOpcodeLookup() {
        opcodes.put(new Type[]{Type.INT, Type.LONG}, 0x85);
        opcodes.put(new Type[]{Type.INT, Type.FLOAT}, 0x86);
        opcodes.put(new Type[]{Type.INT, Type.DOUBLE}, 0x87);
        opcodes.put(new Type[]{Type.LONG, Type.INT}, 0x88);
        opcodes.put(new Type[]{Type.LONG, Type.FLOAT}, 0x89);
        opcodes.put(new Type[]{Type.LONG, Type.DOUBLE}, 0x8a);
        opcodes.put(new Type[]{Type.FLOAT, Type.INT}, 0x8b);
        opcodes.put(new Type[]{Type.FLOAT, Type.LONG}, 0x8c);
        opcodes.put(new Type[]{Type.FLOAT, Type.DOUBLE}, 0x8d);
        opcodes.put(new Type[]{Type.DOUBLE, Type.INT}, 0x8e);
        opcodes.put(new Type[]{Type.DOUBLE, Type.LONG}, 0x8f);
        opcodes.put(new Type[]{Type.DOUBLE, Type.FLOAT}, 0x90);
        opcodes.put(new Type[]{Type.INT, Type.BYTE}, 0x91);
        opcodes.put(new Type[]{Type.INT, Type.CHAR}, 0x92);
        opcodes.put(new Type[]{Type.INT, Type.SHORT}, 0x93);
    }

}
