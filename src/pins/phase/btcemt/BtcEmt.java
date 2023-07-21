package pins.phase.btcemt;

import pins.data.btc.BtcCLASS;
import pins.data.btc.method.BtcMETHOD;
import pins.data.btc.var.BtcFIELD;
import pins.data.emt.EmtClassFile;
import pins.data.emt.constp.EmtFieldrefInfo;
import pins.data.emt.constp.EmtMethodrefInfo;

import java.util.HashMap;

/**
 * Bytecode emission.
 */
public class BtcEmt implements AutoCloseable {

    public static HashMap<BtcCLASS, EmtClassFile> classFiles = new HashMap<>();

    public static HashMap<BtcFIELD, EmtFieldrefInfo> fieldRefs = new HashMap<>();

    public static HashMap<BtcMETHOD, EmtMethodrefInfo> methodRefs = new HashMap<>();

    public static HashMap<EmtFieldrefInfo, Integer> fieldRefConstPoolIndices = new HashMap<>();

    /**
     * Constructs a new phase for JVM bytecode emission.
     */
    public BtcEmt() {
    }

    public void close() {
    }
    
}
