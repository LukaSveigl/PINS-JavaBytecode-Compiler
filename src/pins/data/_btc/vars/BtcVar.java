package pins.data._btc.vars;

import pins.common.logger.Loggable;
import pins.data._btc.BtcComp;

public abstract class BtcVar implements Loggable, BtcComp {

    /**
     * The type.
     * */
    public enum Type {
        BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, REF
    }

}
