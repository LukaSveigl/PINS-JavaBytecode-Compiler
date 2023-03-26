package pins.data.btc;

import pins.common.logger.Loggable;
import pins.data.btc.instr.BtcInstr;
import pins.data.btc.vars.BtcLocal;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * A bytecode method.
 */
public class BtcMethod implements Loggable, BtcComp {

    public enum Type {
        CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, REF, VOID
    }

    /** The method name. */
    public final String name;

    /** The method type. */
    public final Type type;

    /** The method parameters. */
    private final Vector<BtcLocal> pars;

    /** The method instructions. */
    private final Vector<BtcInstr> instrs;

    /**
     * Constructs a new bytecode method.
     *
     * @param name The method name.
     */
    public BtcMethod(String name, Type type) {
        this.name = name;
        this.type = type;
        this.instrs = new Vector<>();
        this.pars = new Vector<>();
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
     * Adds multiple instructions to the method.
     *
     * @param instrs The instructions to add.
     */
    public void addInstrs(Vector<BtcInstr> instrs) {
        this.instrs.addAll(instrs);
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
     * Adds a parameter to the method.
     *
     * @param par The parameter to add.
     */
    public void addPar(BtcLocal par) {
        pars.add(par);
    }

    /**
     * Adds multiple parameters to the method.
     *
     * @param pars The parameters to add.
     */
    public void addPars(Vector<BtcLocal> pars) {
        this.pars.addAll(pars);
    }

    /**
     * Returns the method parameters.
     *
     * @return The method parameters.
     */
    public Vector<BtcLocal> pars() {
        return pars;
    }

    @Override
    public byte[] toBytecode() {
        int size = 0;
        for (BtcInstr instr : instrs) {
            size += instr.toBytecode().length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        for (BtcInstr instr : instrs) {
            byteBuffer.put(instr.toBytecode());
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 0;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type + " " + name);
        for (BtcLocal par : pars) {
            par.log(pfx + "  ");
        }
        for (BtcInstr instr : instrs) {
            instr.log(pfx + "  ");
        }
    }

}
