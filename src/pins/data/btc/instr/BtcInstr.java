package pins.data.btc.instr;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.util.Vector;

/**
 * Bytecode instruction.
 */
public abstract class BtcInstr implements Loggable, BtcComp {

    /** The instruction opcode. */
    protected int opcode;

}
