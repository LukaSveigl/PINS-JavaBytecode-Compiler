package pins.data.btc.method;

import pins.common.logger.Loggable;
import pins.data.btc.method.instr.BtcInstr;
import pins.data.btc.var.BtcLOCAL;
import pins.data.mem.MemRelAccess;

import java.util.HashMap;
import java.util.Vector;

/**
 * JVM bytecode method.
 */
public class BtcMETHOD implements Loggable {

    public enum Flags {
        PRIVATE, PUBLIC, PROTECTED, STATIC, FINAL, ABSTRACT, SYNCHRONIZED, NATIVE, STRICT, SYNTHETIC
    }

    public enum Type {
        INT, FLOAT, LONG, DOUBLE, BOOL, STRING, ARRAY, OBJECT, VOID
    }

    /** The method name. */
    public final String name;

    /** The method flags. */
    private final Vector<Flags> flags = new Vector<Flags>();

    /** The method parameter types. */
    private final Vector<Type> pars = new Vector<Type>();

    /** The method local variable types. */
    private final Vector<Type> locs = new Vector<Type>();

    /** The method return type. */
    public final Type ret;

    /** The method local variables. */
    private final Vector<BtcLOCAL> locals = new Vector<BtcLOCAL>();

    private final HashMap<MemRelAccess, BtcLOCAL> accessesToLocals = new HashMap<MemRelAccess, BtcLOCAL>();

    /** The method instructions. */
    private final Vector<BtcInstr> instrs = new Vector<BtcInstr>();

    /** The counter of local variable slots. */
    private int localCount = 0;

    /** The count of instruction bytes. */
    private int instrCount = 0;

    /**
     * Constructs a new method.
     *
     * @param name The method name.
     * @param ret  The method return type.
     */
    public BtcMETHOD(String name, Type ret) {
        this.name = name;
        this.ret = ret;
        this.flags.add(Flags.PUBLIC);
        this.flags.add(Flags.STATIC);
    }

    /**
     * Returns the method flags.
     *
     * @return The method flags.
     */
    public Vector<Flags> flags() {
        return flags;
    }

    /**
     * Adds a method parameter.
     *
     * @param par The parameter.
     */
    public void addPar(MemRelAccess access, BtcLOCAL par) {
        switch (par.type) {
            case INT -> pars.add(Type.INT);
            case FLOAT -> pars.add(Type.FLOAT);
            case LONG -> pars.add(Type.LONG);
            case DOUBLE -> pars.add(Type.DOUBLE);
            case BOOL -> pars.add(Type.BOOL);
            case STRING -> pars.add(Type.STRING);
            case ARRAY -> pars.add(Type.ARRAY);
            case OBJECT -> pars.add(Type.OBJECT);
            case VOID -> pars.add(Type.VOID);
        }

        locals.add(par);
        localCount += par.size();
        accessesToLocals.put(access, par);
    }

    /**
     * Returns the method parameter types.
     *
     * @return The method parameter types.
     */
    public Vector<Type> parTypes() {
        return pars;
    }

    /**
     * Adds a local variable.
     *
     * @param local The local variable.
     */
    public void addLocal(MemRelAccess access, BtcLOCAL local) {
        switch (local.type) {
            case INT -> locs.add(Type.INT);
            case FLOAT -> locs.add(Type.FLOAT);
            case LONG -> locs.add(Type.LONG);
            case DOUBLE -> locs.add(Type.DOUBLE);
            case BOOL -> locs.add(Type.BOOL);
            case STRING -> locs.add(Type.STRING);
            case ARRAY -> locs.add(Type.ARRAY);
            case OBJECT -> locs.add(Type.OBJECT);
            case VOID -> locs.add(Type.VOID);
        }

        locals.add(local);
        localCount += local.size();
        accessesToLocals.put(access, local);
    }

    /**
     * Returns the local variable with the given index.
     *
     * @param index The index.
     * @return The local variable.
     */
    public BtcLOCAL getLocal(int index) {
        return locals.get(index);
    }

    public BtcLOCAL getLocal(MemRelAccess access) {
        return accessesToLocals.get(access);
    }

    /**
     * Returns the local variables.
     *
     * @return The local variables.
     */
    public Vector<BtcLOCAL> locals() {
        return locals;
    }

    public Vector<Type> locTypes() {
        return locs;
    }

    /**
     * Adds an instruction.
     *
     * @param instr The instruction.
     */
    public void addInstr(BtcInstr instr) {
        instrs.add(instr);
        instrCount += instr.size();
    }

    /**
     * Returns the instruction with the given index.
     *
     * @param index The index.
     * @return The instruction.
     */
    public BtcInstr getInstr(int index) {
        return instrs.get(index);
    }

    /**
     * Returns the instructions.
     *
     * @return The instructions.
     */
    public Vector<BtcInstr> instrs() {
        return instrs;
    }

    /**
     * Returns the count of local variable slots.
     *
     * @return The count of local variable slots.
     */
    public int localCount() {
        return localCount;
    }

    /**
     * Returns the count of instruction bytes.
     *
     * @return The count of instruction bytes.
     */
    public int instrCount() {
        return instrCount;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "METHOD[" + name + ", " + ret + "]");

        System.out.println(pfx + "  FLAGS:");
        for (Flags flag : flags)
            System.out.println(pfx + "    " + flag);

        System.out.println(pfx + "  PARS:");
        for (Type par : pars)
            System.out.println(pfx + "    " + par);

        System.out.println(pfx + "  LOCALS:");
        for (BtcLOCAL local : locals)
            local.log(pfx + "    ");

        System.out.println(pfx + "  INSTRS:");
        for (BtcInstr instr : instrs)
            instr.log(pfx + "    ");
    }

}
