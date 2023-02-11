package pins.data.btc;

import java.util.Vector;

/**
 * A bytecode component, such as instruction, variable or field.
 */
public interface BtcComp {

    /**
     * Returns the hexadecimal representation of the component.
     *
     * @return The hexadecimal representation of the component.
     */
     Vector<Integer> getHexRepresentation();

}
