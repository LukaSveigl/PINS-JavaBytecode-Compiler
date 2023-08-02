package pins.phase.btcgen;

import pins.data.ast.AstDecl;
import pins.data.ast.AstFunDecl;
import pins.data.btc.BtcCLASS;
import pins.data.btc.method.BtcMETHOD;
import pins.data.btc.var.BtcLOCAL;

import java.util.HashMap;
import java.util.Stack;

/**
 * Bytecode generation.
 */
public class BtcGen implements AutoCloseable {

    /** The stack of bytecode classes. The topmost class is the one currently being generated. */
    public static Stack<BtcCLASS> btcClasses = new Stack<>();

    /** Maps the AST function declarations to their bytecode equivalents. */
    public static final HashMap<AstFunDecl, BtcMETHOD> btcMethods = new HashMap<>();

    /** Maps the AST variable declarations to their bytecode equivalents. */
    public static final HashMap<AstDecl, BtcLOCAL> btcLocals = new HashMap<>();

    /**
     * Constructs a new phase for JVM bytecode generation.
     */
    public BtcGen() {
    }

    public void close() {
    }

}
