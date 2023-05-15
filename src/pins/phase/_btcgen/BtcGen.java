package pins.phase._btcgen;

import pins.data.ast.*;
import pins.data._btc.BtcClass;
import pins.data._btc.vars.BtcLocal;
import pins.data.mem.*;
import pins.data._btc.vars._BtcField;
import pins.data._btc.BtcMethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Bytecode generation.
 */
public class BtcGen implements AutoCloseable {

    /* Maps linearized code chunks to bytecode methods. */
    //private static final HashMap<LinCodeChunk, BtcMethod> btcMethods = new HashMap<LinCodeChunk, BtcMethod>(0);
    /** Maps function declarations to bytecode methods. */
    public static final HashMap<AstFunDecl, BtcMethod> btcMethods = new HashMap<AstFunDecl, BtcMethod>(0);

    /** Maps global variables to bytecode fields. */
    public static final HashMap<MemAbsAccess, _BtcField> btcFields = new HashMap<MemAbsAccess, _BtcField>(0);

    /** Maps local variables and parameters to bytecode locals. */
    public static final HashMap<MemRelAccess, BtcLocal> btcLocals = new HashMap<MemRelAccess, BtcLocal>(0);

    /** Maps locals to their corresponding method. */
    public static final HashMap<BtcMethod, HashSet<BtcLocal>> methodLocals = new HashMap<BtcMethod, HashSet<BtcLocal>>(0);

    /** The stack of the bytecode classes. */
    public static final Stack<BtcClass> btcClasses = new Stack<BtcClass>();

    /**
     * Constructs a new phase for JVM bytecode generation.
     */
    public BtcGen() {
    }

    public void close() {
    }

}
