package pins.data.btc;

import java.util.Vector;

/**
 * A bytecode component, such as instruction, variable or field.
 */
public interface BtcComp {

    /**
     * Returns the bytecode component as a byte array.
     *
     * @return The bytecode component as a byte array.
     */
     byte[] toBytecode();

    /**
     * Returns the size of the bytecode component.
     *
     * @return The size of the bytecode component.
     */
    int getBytecodeLength();

}
