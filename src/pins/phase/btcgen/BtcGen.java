package pins.phase.btcgen;

import pins.data.btc.BtcCLASS;

import java.util.Stack;

/**
 * Bytecode generation.
 */
public class BtcGen implements AutoCloseable {

    /** The stack of bytecode classes. The topmost class is the one currently being generated. */
    public static Stack<BtcCLASS> btcClasses = new Stack<>();

    /**
     * Constructs a new phase for JVM bytecode generation.
     */
    public BtcGen() {
    }

    public void close() {
    }

}
