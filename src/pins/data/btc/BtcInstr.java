package pins.data.btc;

import pins.common.logger.Loggable;

import java.util.Vector;

/**
 * Bytecode instruction.
 */
public abstract class BtcInstr implements Loggable {

    /**
     * Returns the hexadecimal representation of the instruction.
     *
     * @return The hexadecimal representation of the instruction.
     */
    public abstract Vector<Integer> getHexRepresentation();

}
