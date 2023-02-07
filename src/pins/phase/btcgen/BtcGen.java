package pins.phase.btcgen;

import pins.data.btc.BtcInstr;
import pins.data.lin.LinCodeChunk;

import java.util.HashMap;

/**
 * Bytecode generation.
 */
public class BtcGen implements AutoCloseable {

    /** Maps linearized code chunks to bytecode instructions. */
    private static HashMap<LinCodeChunk, BtcInstr> btcInst = new HashMap<LinCodeChunk, BtcInstr>(0);

    /**
     * Constructs a new phase for JVM bytecode generation.
     */
    public BtcGen() {
    }

    public void close() {
    }

}
