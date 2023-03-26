package pins.data.btc.vars;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

public abstract class BtcVar implements Loggable, BtcComp {

    /**
     * The type.
     * */
    public enum Type {
        BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, REF
    }

}
