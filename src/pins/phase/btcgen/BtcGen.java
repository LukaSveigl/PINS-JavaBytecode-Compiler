package pins.phase.btcgen;

import pins.data.btc.BtcMethod;
import pins.data.lin.LinCodeChunk;

import java.util.HashMap;

/**
 * Bytecode generation.
 */
public class BtcGen implements AutoCloseable {

    /** Maps linearized code chunks to bytecode methods. */
    private static final HashMap<LinCodeChunk, BtcMethod> btcMethods = new HashMap<LinCodeChunk, BtcMethod>(0);

    /**
     * Constructs a new phase for JVM bytecode generation.
     */
    public BtcGen() {
    }

    public void close() {
    }

}
