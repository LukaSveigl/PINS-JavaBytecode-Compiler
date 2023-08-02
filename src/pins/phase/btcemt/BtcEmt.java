package pins.phase.btcemt;

import pins.data.btc.BtcCLASS;
import pins.data.btc.var.BtcFIELD;
import pins.data.emt.EmtClassFile;
import pins.data.emt.constp.EmtFieldrefInfo;

import java.util.HashMap;

/**
 * Bytecode emission.
 */
public class BtcEmt implements AutoCloseable {

    /** Class files that compose the source file. */
    public static HashMap<BtcCLASS, EmtClassFile> classFiles = new HashMap<>();

    /** Maps the IMC fields to their constant pool representations. */
    public static HashMap<BtcFIELD, EmtFieldrefInfo> fieldRefs = new HashMap<>();

    /** Maps the constant pool fields to their constant pool indices. */
    public static HashMap<EmtFieldrefInfo, Integer> fieldRefConstPoolIndices = new HashMap<>();

    /**
     * Constructs a new phase for JVM bytecode emission.
     */
    public BtcEmt() {
    }

    public void close() {
    }
    
}
