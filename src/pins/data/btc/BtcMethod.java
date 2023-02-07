package pins.data.btc;

import java.util.Vector;

/**
 * A bytecode method.
 */
public class BtcMethod {

    /** The method name. */
    public final String name;

    /** The method instructions. */
    private final Vector<BtcInstr> instrs;

    /**
     * Constructs a new bytecode method.
     *
     * @param name The method name.
     */
    public BtcMethod(String name) {
        this.name = name;
        this.instrs = new Vector<>();
    }

    /**
     * Adds an instruction to the method.
     *
     * @param instr The instruction to add.
     */
    public void addInstr(BtcInstr instr) {
        instrs.add(instr);
    }

    /**
     * Returns the method instructions.
     *
     * @return The method instructions.
     */
    public Vector<BtcInstr> instrs() {
        return instrs;
    }

    /**
     * Returns the method instructions as a hex representation.
     *
     * @return The method instructions as a hex representation.
     */
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        for (BtcInstr instr : instrs) {
            hex.addAll(instr.getHexRepresentation());
        }
        return hex;
    }

    /**
     * Logs the method.
     */
    public void log(String pfx) {
        System.out.println(pfx + "Method " + name);
        for (BtcInstr instr : instrs) {
            instr.log(pfx + "  ");
        }
    }

}
