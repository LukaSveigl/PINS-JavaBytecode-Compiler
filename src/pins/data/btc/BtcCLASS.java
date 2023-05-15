package pins.data.btc;

import pins.common.logger.Loggable;
import pins.data.ast.AstVarDecl;
import pins.data.btc.method.BtcMETHOD;
import pins.data.btc.var.BtcFIELD;

import java.util.HashMap;
import java.util.Vector;

/**
 * JVM bytecode class.
 */
public class BtcCLASS implements Loggable {

    public enum Flags {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, INTERFACE, ENUM, ANNOTATION, SYNTHETIC
    }

    /** The class name. */
    public final String name;

    /** The class destination file name. */
    public final String dstName;

    /** The class flags. */
    private final Vector<Flags> flags = new Vector<Flags>();

    /** The class fields. */
    private final HashMap<AstVarDecl, BtcFIELD> fields = new HashMap<>();

    /** The class methods. */
    private final Vector<BtcMETHOD> methods = new Vector<>();

    /**
     * Constructs a new class.
     *
     * @param name The class name.
     */
    public BtcCLASS(String name, String dstFileName) {
        this.name = name;
        this.dstName = dstFileName;
        this.flags.add(Flags.PUBLIC);
        this.flags.add(Flags.SYNTHETIC);
    }

    /**
     * Returns the class flags.
     *
     * @return The class flags.
     */
    public Vector<Flags> flags() {
        return flags;
    }

    /**
     * Adds a field to the class.
     *
     * @param varDecl The AST variable declaration.
     * @param btcFIELD The JVM bytecode field.
     */
    public void addField(AstVarDecl varDecl, BtcFIELD btcFIELD) {
        fields.put(varDecl, btcFIELD);
    }

    /**
     * Returns the field with the given AST variable declaration.
     *
     * @param varDecl The AST variable declaration.
     * @return The JVM bytecode field.
     */
    public BtcFIELD getField(AstVarDecl varDecl) {
        return fields.get(varDecl);
    }

    /**
     * Returns the class fields.
     *
     * @return The class fields.
     */
    public HashMap<AstVarDecl, BtcFIELD> fields() {
        return fields;
    }

    /**
     * Adds a method to the class.
     *
     * @param btcMETHOD The JVM bytecode method.
     */
    public void addMethod(BtcMETHOD btcMETHOD) {
        methods.add(btcMETHOD);
    }

    /**
     * Returns the method of the class with the specified index.
     *
     * @return The method of the class with the specified index.
     */
    public BtcMETHOD getMethod(int index) {
        return methods.get(index);
    }

    /**
     * Returns the method of the class with the specified name.
     *
     * @param name The method name.
     * @return The method of the class with the specified name.
     */
    public BtcMETHOD getMethod(String name) {
        for (BtcMETHOD method : methods) {
            if (method.name.equals(name)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Returns the class methods.
     *
     * @return The class methods.
     */
    public Vector<BtcMETHOD> methods() {
        return methods;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "CLASS[" + name + "]");
        System.out.println(pfx + "  FIELDS:");
        for (BtcFIELD btcFIELD : fields.values()) {
            btcFIELD.log(pfx + "    ");
        }

        System.out.println(pfx + "  METHODS:");
        for (BtcMETHOD btcMETHOD : methods) {
            btcMETHOD.log(pfx + "    ");
        }
    }
}
